package org.eu.hanana.reimu.lib.rjcef.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class NettyTcpServer {

    public final int port;
    private final SimpleChannelInboundHandler<ByteBuf> handler;

    public EventLoopGroup bossGroup;
    public EventLoopGroup workerGroup;
    public Channel serverChannel;

    public NettyTcpServer(int port,SimpleChannelInboundHandler<ByteBuf> byteBufSimpleChannelInboundHandler) {
        this.port = port;
        this.handler=byteBufSimpleChannelInboundHandler;
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                                    10 * 1024 * 1024, // 最大帧长度
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
                            ch.pipeline().addLast(handler);
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            serverChannel = future.channel(); // 必须保存
            System.out.println("服务器启动成功，端口: " + port);

        } catch (Exception e) {
            e.printStackTrace();
            stop();
            throw e;
        }
    }


    public void stop() {
        System.out.println("server stopping...");

        if (serverChannel != null) {
            serverChannel.close();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        System.out.println("server stopped");
    }

}
