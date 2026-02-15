package org.eu.hanana.reimu.lib.rjcef.common;

import io.netty.channel.*;

public class GlobalWriteListener extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        promise.addListener((ChannelFuture future) -> {
            if (!future.isSuccess()) {
                Throwable cause = future.cause();
                System.err.println("Global write error: " + cause);
                // 可以统一处理，比如统计、重试或报警
            }
        });
        super.write(ctx, msg, promise); // 继续发送
    }
}