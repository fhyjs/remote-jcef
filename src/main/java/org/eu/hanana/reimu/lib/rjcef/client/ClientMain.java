package org.eu.hanana.reimu.lib.rjcef.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefRequestContext;
import org.eu.hanana.reimu.lib.rjcef.common.BufUtil;
import org.eu.hanana.reimu.lib.rjcef.common.CallbackRegister;
import org.eu.hanana.reimu.lib.rjcef.common.RemoteCommands;
import org.eu.hanana.reimu.lib.rjcef.common.Tuple;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ClientMain extends SimpleChannelInboundHandler<ByteBuf> implements CallbackRegister {
    public static ClientMain INSTANCE;
    public  int port;
    public  CefApp cefApp;
    public  CefNettyClient cnc;
    public  RemoteCommands remoteCommands = new RemoteCommands(this);
    public Map<String, CefClient> cefClientMap= new HashMap<>();
    public Map<String, Map<String,CefBrowserMC>> browserMCHashMap= new HashMap<>();//client's uuid
    public Map<String, Consumer<Tuple<ByteBuf, ChannelHandlerContext>>> callbacks = new HashMap<>();
    public static void main(String[] args) throws UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {
        INSTANCE= new ClientMain(args);
    }
    public ClientMain(String[] args) throws InterruptedException, UnsupportedPlatformException, CefInitializationException, IOException {
        port= Integer.parseInt(args[0]);
        System.out.println("Client Starting at port : "+port);
        cnc=new CefNettyClient(port,this);
        remoteCommands.regHandler(remoteCommands.CONFIRM_START, byteBuf -> {
            System.out.println("connected");
            cnc.connected=true;
            return null;
        });
        remoteCommands.regHandler(remoteCommands.CREATE_APP, byteBuf -> {
            System.out.println("CREATE_APP");
            try {
                cefApp=CelInstaller.getBuilder().build();
            } catch (IOException | CefInitializationException | InterruptedException | UnsupportedPlatformException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
        remoteCommands.regHandler(remoteCommands.CREATE_CLIENT, tuple -> {
            System.out.println("CREATE_CLIENT");
            String uuid = UUID.randomUUID().toString();
            cefClientMap.put(uuid,cefApp.createClient());
            browserMCHashMap.put(uuid,new HashMap<>());
            ByteBuf byteBuf = tuple.b().alloc().directBuffer();
            BufUtil.writeString(uuid,byteBuf);
            System.out.println(uuid);
            return byteBuf;
        });
        remoteCommands.regHandler(remoteCommands.CREATE_BROWSER, tuple -> {
            System.out.println("CREATE_BROWSER");
            String uuidClient = BufUtil.readString(tuple.a());
            String url = BufUtil.readString(tuple.a());
            String uuid = UUID.randomUUID().toString();
            System.out.println(uuidClient);
            browserMCHashMap.get(uuidClient).put(uuid,new CefBrowserMC(cefClientMap.get(uuidClient),url,true, CefRequestContext.getGlobalContext(),new ClientRender(uuid,uuidClient,this)));
            ByteBuf byteBuf = tuple.b().alloc().directBuffer();
            BufUtil.writeString(uuid,byteBuf);
            return byteBuf;
        });
        remoteCommands.regHandler(remoteCommands.CREATE_BROWSER_IMMEDIATELY, tuple -> {
            System.out.println("CREATE_BROWSER_IMMEDIATELY");
            String uuidClient = BufUtil.readString(tuple.a());
            String uuid = BufUtil.readString(tuple.a());
            browserMCHashMap.get(uuidClient).get(uuid).createImmediately();
            return null;
        });
        remoteCommands.regHandler(remoteCommands.BROWSER_RESIZE, tuple -> {
            System.out.println("BROWSER_RESIZE");
            String uuidClient = BufUtil.readString(tuple.a());
            String uuid = BufUtil.readString(tuple.a());
            browserMCHashMap.get(uuidClient).get(uuid).wasResized_(tuple.a().readInt(),tuple.a().readInt());
            return null;
        });
        remoteCommands.regHandler(remoteCommands.CLIENT_DSETROY, tuple -> {
            System.out.println("CLIENT_DSETROY");
            String uuidClient = BufUtil.readString(tuple.a());
            browserMCHashMap.remove(uuidClient);
            cefClientMap.get(uuidClient).dispose();
            return null;
        });
        remoteCommands.regHandler(remoteCommands.BROWSER_mouseInteracted, tuple -> {
            String uuidClient = BufUtil.readString(tuple.a());
            String uuid = BufUtil.readString(tuple.a());
            browserMCHashMap.get(uuidClient).get(uuid).mouseInteracted(tuple.a().readInt(),tuple.a().readInt(),tuple.a().readInt(),tuple.a().readInt(),tuple.a().readBoolean(),tuple.a().readInt());
            return null;
        });
        remoteCommands.regHandler(remoteCommands.BROWSER_mouseMoved, tuple -> {
            String uuidClient = BufUtil.readString(tuple.a());
            String uuid = BufUtil.readString(tuple.a());
            browserMCHashMap.get(uuidClient).get(uuid).mouseMoved(tuple.a().readInt(),tuple.a().readInt(),tuple.a().readInt());
            return null;
        });
        remoteCommands.regHandler(remoteCommands.BROWSER_keyTyped, tuple -> {
            String uuidClient = BufUtil.readString(tuple.a());
            String uuid = BufUtil.readString(tuple.a());
            browserMCHashMap.get(uuidClient).get(uuid).keyTyped(tuple.a().readChar(),tuple.a().readInt());
            return null;
        });
        remoteCommands.regHandler(remoteCommands.BROWSER_keyEventByKeyCode, tuple -> {
            String uuidClient = BufUtil.readString(tuple.a());
            String uuid = BufUtil.readString(tuple.a());
            browserMCHashMap.get(uuidClient).get(uuid).keyEventByKeyCode(tuple.a().readInt(),tuple.a().readInt(),tuple.a().readInt(),tuple.a().readBoolean());
            return null;
        });
        remoteCommands.regHandler(remoteCommands.BROWSER_mouseScrolled, tuple -> {
            String uuidClient = BufUtil.readString(tuple.a());
            String uuid = BufUtil.readString(tuple.a());
            browserMCHashMap.get(uuidClient).get(uuid).mouseScrolled(tuple.a().readInt(),tuple.a().readInt(),tuple.a().readInt(),tuple.a().readInt(),tuple.a().readInt());
            return null;
        });
        remoteCommands.regHandler(remoteCommands.BROWSER_doClose, tuple -> {
            String uuidClient = BufUtil.readString(tuple.a());
            String uuid = BufUtil.readString(tuple.a());
            browserMCHashMap.get(uuidClient).get(uuid).close();
            browserMCHashMap.get(uuidClient).remove(uuid);
            return null;
        });
        cnc.start();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("channel Active");
        var bb = ctx.alloc().directBuffer();
        BufUtil.writeString(remoteCommands.CONFIRM_START,bb);
        ctx.writeAndFlush(bb);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("Disconnect");
        if (cefApp!=null)
            cefApp.dispose();
        System.exit(1);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        try {
            String cmd = BufUtil.readString(msg);
            //System.out.println("cmd: "+ cmd);
            remoteCommands.processor.get(cmd).apply(new Tuple<>(msg,ctx));
        }catch (Exception e){
            e.printStackTrace();
        }
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
}
