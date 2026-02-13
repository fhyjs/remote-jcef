package org.eu.hanana.reimu.lib.rjcef.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface CallbackRegister {
    void registerCallback(String uuid, Consumer<Tuple<ByteBuf,ChannelHandlerContext>> callback);
    void removeCallback(String uuid);
    Consumer<Tuple<ByteBuf,ChannelHandlerContext>>  getCallback(String uuid);
}
