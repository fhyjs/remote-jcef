package org.eu.hanana.reimu.lib.rjcef.common;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class CallbackResult {
    public final boolean success;
    public final ByteBuf result;
    public final Throwable throwable;

    public CallbackResult(ByteBuf byteBuf){
        this.success= byteBuf.readBoolean();
        if (success){
            this.throwable=null;
            this.result=byteBuf.readBytes(byteBuf.readableBytes());
        }else {
            this.result=null;
            try {
                this.throwable=BufUtil.readThrowable(byteBuf);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String toString() {
        return "CallbackResult[success=%s]".formatted(success);
    }
}
