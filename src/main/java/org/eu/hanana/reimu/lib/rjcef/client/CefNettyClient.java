package org.eu.hanana.reimu.lib.rjcef.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.eu.hanana.reimu.lib.rjcef.common.BufUtil;
import org.eu.hanana.reimu.lib.rjcef.common.ICefRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class CefNettyClient   {
    private final String host;
    private final int port;
    private final ClientMain main;
    Channel channel;
    private EventLoopGroup group;
    public int getPort() {
        return port;
    }

    public CefNettyClient(String host, int port, ClientMain clientMain) {
        this.port=port;
        this.host=host;
        this.main=clientMain;
    }
    public CefNettyClient(int p,ClientMain clientMain){
        this("127.0.0.1",p,clientMain);
    }
    public boolean connected = false;
    public void start() throws InterruptedException {
        group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                                100 * 1024 * 1024, // 最大帧长度
                                0,                 // 长度字段偏移 0
                                4,                 // 长度字段长度 4
                                0,                 // 长度调整 = 0
                                4                  // initialBytesToStrip = 0，不丢掉长度字段
                        ));
                        // 先加 outbound handler，修补所有 writeAndFlush
                        ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                if (msg instanceof ByteBuf buf) {
                                    int len = buf.readableBytes();
                                    ByteBuf frame = ctx.alloc().directBuffer(4 + len);
                                    frame.writeInt(len);
                                    frame.writeBytes(buf, buf.readerIndex(), len); // 这里还是一次复制，但避免多次 buffer 分配
                                    super.write(ctx, frame, promise);
                                    buf.release();
                                } else {
                                    super.write(ctx, msg, promise);
                                }
                            }
                        });
                        ch.pipeline().addLast(main);
                    }
                });

        ChannelFuture future = bootstrap.connect(host, port).sync();
        channel = future.channel();
    }
    public void stop() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}
