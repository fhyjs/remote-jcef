package org.eu.hanana.reimu.lib.rjcef.server;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.logging.log4j.Logger;
import org.cef.CefApp;
import org.eu.hanana.reimu.lib.rjcef.client.ClientMain;
import org.eu.hanana.reimu.lib.rjcef.common.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.eu.hanana.reimu.lib.rjcef.server.ClassPathJarBuilder.copyClassPathToJar;
import static org.eu.hanana.reimu.lib.rjcef.server.FullPackageJarBuilder.buildJarFromPackage;


public class RemoteCefApp extends SimpleChannelInboundHandler<ByteBuf> implements CallbackRegister {
    public File libsDir = new File("./jcef/jar");
    private Process process;
    public final int port = CefUtil.getRandomPort();
    public ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public RemoteCommands remoteCommands = new RemoteCommands(this);
    protected boolean connected = false;
    public NettyTcpServer serv;
    public Channel client;
    public Map<String, Consumer<Tuple<ByteBuf, ChannelHandlerContext>>> callbacks = new HashMap<>();
    public Map<String,RemoteCefClient> remoteCefClientMap = new HashMap<>();
    public Process getProcess() {
        return process;
    }

    public RemoteCefApp(){
        remoteCommands.regHandler(remoteCommands.CONFIRM_START, tuple -> {
            setConnected(true);
            var bb = tuple.b().alloc().directBuffer();
            BufUtil.writeString(remoteCommands.CONFIRM_START,bb);
            // 发送给客户端
            tuple.b().writeAndFlush(bb);
            setConnected(true);
            return null;
        });
        remoteCommands.regHandler(remoteCommands.BROWSER_ONPAINT, tuple -> {
            var uuidClient = BufUtil.readString(tuple.a());
            var uuid = BufUtil.readString(tuple.a());
            var popup = tuple.a().readBoolean();
            var rects = BufUtil.readRectangles(tuple.a());
            var w = tuple.a().readInt();
            var h = tuple.a().readInt();
            var completeReRender = tuple.a().readBoolean();
            // 读取 pixel 数据
            List<ByteBuffer> rectBuffers = new ArrayList<>(rects.length);

            for (Rectangle rect : rects) {

                int rectSize = rect.width * rect.height * 4;

                // 零拷贝读取
                ByteBuf pixelBuf = tuple.a().readSlice(rectSize);

                // 转成 NIO ByteBuffer（仍然是零拷贝）
                ByteBuffer nioBuffer = pixelBuf.nioBuffer();

                rectBuffers.add(nioBuffer);
            }
            remoteCefClientMap.get(uuidClient).browserMap.get(uuid).cefRenderer.onPaint(popup,rects,rectBuffers,w,h,completeReRender);
            return null;
        });
    }

    protected void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        clients.add(ctx.channel());
        client=ctx.channel();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        clients.remove(ctx.channel());
        setConnected(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        //System.out.println("cmd: "+ cmd);
        try{
            String cmd = BufUtil.readString(msg);
            remoteCommands.processor.get(cmd).apply(new Tuple<>(msg,ctx));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void start() throws InterruptedException {
        serv=new NettyTcpServer(port,this);
        serv.start();

        libsDir.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(new File(libsDir,"rjcef.jar"))) {
            copyClassPathToJar(RemoteCefApp.class,fos, null);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try (FileOutputStream fos = new FileOutputStream(new File(libsDir,"netty.jar"))) {
            buildJarFromPackage("io.netty", fos, null);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try (FileOutputStream fos = new FileOutputStream(new File(libsDir,"jcefmaven.jar"))) {
            copyClassPathToJar(me.friwi.jcefmaven.CefAppBuilder.class,fos, null);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try (FileOutputStream fos = new FileOutputStream(new File(libsDir,"jcef.jar"))) {
            copyClassPathToJar(CefApp.class,fos, null);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try (FileOutputStream fos = new FileOutputStream(new File(libsDir,"log4j2.jar"))) {
            copyClassPathToJar(Logger.class,fos, null);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try (FileOutputStream fos = new FileOutputStream(new File(libsDir,"gson.jar"))) {
            copyClassPathToJar(Gson.class,fos, null);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        // 启动子进程
        List<String> command = new ArrayList<>();
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        command.add(javaBin);
        command.add("-cp");
        var cps = new StringBuilder("\"");
        for (File file : Objects.requireNonNull(libsDir.listFiles((file, s) -> s.endsWith(".jar")))) {
            cps.append(file.getAbsoluteFile()).append(";");
        }
        cps.append("\"");
        command.add(cps.toString());
        command.add(ClientMain.class.getName());
        command.add(String.valueOf(port));
        // 构建 ProcessBuilder
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.inheritIO(); // 将子进程的输出、错误流直接继承父进程
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (!connected) Thread.sleep(10);

    }

    public void createApp() {
        var bb = serv.serverChannel.alloc().directBuffer();
        BufUtil.writeString(remoteCommands.CREATE_APP,bb);
        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb,client,tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },this);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
    public RemoteCefClient createClient(){
        var bb = serv.serverChannel.alloc().directBuffer();
        BufUtil.writeString(remoteCommands.CREATE_CLIENT,bb);
        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb,client,tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },this);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return new RemoteCefClient(BufUtil.readString(cr.get().result),this);
    }
    @Override
    public void registerCallback(String uuid, Consumer<Tuple<ByteBuf, ChannelHandlerContext>> callback) {
        callbacks.put(uuid,callback);
    }

    @Override
    public void removeCallback(String uuid) {
        callbacks.remove(uuid);
    }

    @Override
    public Consumer<Tuple<ByteBuf, ChannelHandlerContext>> getCallback(String uuid) {
        return callbacks.get(uuid);
    }

    public void destroy() {
        serv.stop();
    }
}
