package org.eu.hanana.reimu.lib.rjcef.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

public class BufUtil {
    public static ChannelFuture sendPacketWithCallback(ByteBuf byteBuf, Channel channel, Consumer<Tuple<ByteBuf, ChannelHandlerContext>> callback,CallbackRegister callbackRegister){
        ByteBuf byteBuf1 = channel.alloc().directBuffer();
        writeString("withcallback",byteBuf1);
        String uuid = UUID.randomUUID().toString();
        writeString(uuid,byteBuf1);
        byteBuf1.writeBytes(byteBuf);
        byteBuf.release();
        callbackRegister.registerCallback(uuid,callback);
        return channel.writeAndFlush(byteBuf1);
    }
    public static void writeThrowable(ByteBuf buf, Throwable t) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(t);
        }
        byte[] bytes = baos.toByteArray();
        buf.ensureWritable(4 + bytes.length); // 先保证容量够写长度 + 数据
        buf.writeInt(bytes.length);  // 先写长度
        System.out.println(bytes.length);
        buf.writeBytes(bytes);       // 再写数据
    }

    public static Throwable readThrowable(ByteBuf buf) throws IOException, ClassNotFoundException {
        int len = buf.readInt();
        System.out.println(len);
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (Throwable) ois.readObject();
        }
    }
    public static void writeString(String s, ByteBuf b){
        var ba = s.getBytes(StandardCharsets.UTF_8);
        b.writeInt(ba.length);
        b.writeBytes(ba);
    }
    public static String readString(ByteBuf b) {
        int len = b.readInt();
        byte[] bytes = new byte[len];
        b.readBytes(bytes);  // 把数据读到数组
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeRectangles(Rectangle[] dirtyRects, ByteBuf byteBuf) {
        byteBuf.writeInt(dirtyRects.length);
        for (Rectangle dirtyRect : dirtyRects) {
            byteBuf.writeInt(dirtyRect.x);
            byteBuf.writeInt(dirtyRect.y);
            byteBuf.writeInt(dirtyRect.width);
            byteBuf.writeInt(dirtyRect.height);
        }
    }
    public static Rectangle[] readRectangles(ByteBuf byteBuf) {
        int i = byteBuf.readInt();
        var dirtyRects = new Rectangle[i];
        for (int i1 = 0; i1 < i; i1++) {
            dirtyRects[i1]=new Rectangle(byteBuf.readInt(),byteBuf.readInt(),byteBuf.readInt(),byteBuf.readInt());
        }
        return dirtyRects;
    }
    /**
     * 将整个 ByteBuffer 写入 Netty ByteBuf
     * @param buffer 待写入的 ByteBuffer
     * @param byteBuf 目标 Netty ByteBuf
     */
    public static void writeByteBufferWithLength(ByteBuffer buffer, ByteBuf byteBuf) {
        int length = buffer.remaining();  // 获取 ByteBuffer 剩余长度
        byteBuf.writeInt(length);         // 先写入长度

        if (buffer.hasArray()) {
            // Heap buffer，直接写入
            byteBuf.writeBytes(buffer.array(), buffer.position(), length);
            buffer.position(buffer.limit()); // 移动 position
        } else {
            // Direct buffer，需要复制到临时数组
            byte[] temp = new byte[length];
            buffer.get(temp);
            byteBuf.writeBytes(temp);
        }
    }



    // ---------------- ByteBuf → ByteBuffer ----------------

    /**
     * 从 Netty ByteBuf 中读取到新的 ByteBuffer
     */
    public static ByteBuffer readByteBufferWithLength(ByteBuf byteBuf) {
        int length = byteBuf.readInt();        // 先读取长度
        ByteBuffer buffer = ByteBuffer.allocate(length);
        byteBuf.readBytes(buffer);
        buffer.flip();
        return buffer;
    }

}
