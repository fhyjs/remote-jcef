package org.eu.hanana.reimu.lib.rjcef.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class RemoteCommands {


    private final CallbackRegister cr;
    public Map<String, Function<Tuple<ByteBuf, ChannelHandlerContext>,ByteBuf>> processor = new HashMap<>();

    public final String BROWSER_RESIZE = "bro_resize";
    public final String CREATE_APP = "ct_app";
    public final String CREATE_BROWSER = "ct_bro";
    public final String CREATE_BROWSER_IMMEDIATELY = "ct_bro_imm";
    public final String CREATE_CLIENT = "ct_cli";
    public final String BROWSER_ONPAINT = "bro_onp";
    public final String CONFIRM_START = "cf_star";
    public RemoteCommands(CallbackRegister register){
        this.cr=register;
        processor.put("withcallback",tuple -> {
            var uuid = BufUtil.readString(tuple.a());
            String cmd = BufUtil.readString(tuple.a());
            System.out.println("withcallback: cmd: "+ cmd);
            ByteBuf byteBuf = tuple.b().alloc().directBuffer();
            BufUtil.writeString("callback",byteBuf);
            BufUtil.writeString(uuid,byteBuf);

            try {
                ByteBuf apply = processor.get(cmd).apply(new Tuple<>(tuple.a(), tuple.b()));
                if (apply==null){
                    apply= Unpooled.buffer(0);
                }
                byteBuf.writeBoolean(true);
                //byteBuf.writeInt(apply.readableBytes());
                byteBuf.writeBytes(apply, apply.readerIndex(), apply.readableBytes());
            } catch (Throwable e) {
                byteBuf.writeBoolean(false);
                try {
                    BufUtil.writeThrowable(byteBuf,e);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            tuple.b().writeAndFlush(byteBuf);
            return null;
        });
        processor.put("callback",tuple -> {
            var uuid = BufUtil.readString(tuple.a());
            var callback = cr.getCallback(uuid);
            cr.removeCallback(uuid);
            callback.accept(tuple);
            return null;
        });
    }

    public void regHandler(String c, Function<Tuple<ByteBuf, ChannelHandlerContext>,ByteBuf> p){
        processor.put(c,p);
    }
}
