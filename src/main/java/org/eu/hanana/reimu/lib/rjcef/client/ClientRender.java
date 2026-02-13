package org.eu.hanana.reimu.lib.rjcef.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import org.eu.hanana.reimu.lib.rjcef.common.BufUtil;
import org.eu.hanana.reimu.lib.rjcef.common.ICefRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class ClientRender implements ICefRenderer {
    private final String uuid;
    private final ClientMain client;

    public ClientRender(String uuid, ClientMain clientMain) {
        this.uuid=uuid;
        this.client=clientMain;
    }

    @Override
    public void render(double x1, double y1, double x2, double y2) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender) {
        if (client.cnc.channel == null || !client.cnc.channel.isActive()) return;

        CompositeByteBuf composite = client.cnc.channel.alloc().compositeBuffer();

        // 先写事件类型
        ByteBuf headerBuf = client.cnc.channel.alloc().buffer();
        BufUtil.writeString(client.remoteCommands.BROWSER_ONPAINT, headerBuf);
        BufUtil.writeString(uuid, headerBuf);
        headerBuf.writeBoolean(popup);
        BufUtil.writeRectangles(dirtyRects, headerBuf);
        headerBuf.writeInt(width);
        headerBuf.writeInt(height);
        headerBuf.writeBoolean(completeReRender);

        composite.addComponent(true, headerBuf);

        // 发送每个 dirty rect 数据
        for (Rectangle rect : dirtyRects) {
            int rectSize = rect.width * rect.height * 4; // RGBA 4 bytes
            int offset = (rect.y * width + rect.x) * 4;

            ByteBuffer slice = buffer.duplicate();
            slice.position(offset);
            slice.limit(offset + rectSize);

            ByteBuf rectBuf = client.cnc.channel.alloc().directBuffer(rectSize);
            rectBuf.writeBytes(slice);
            composite.addComponent(true, rectBuf);
        }

        client.cnc.channel.writeAndFlush(composite);
    }

    @Override
    public void onPopupSize(Rectangle var1) {

    }

    @Override
    public void onPopupClosed() {

    }

    @Override
    public CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution) {
        return null;
    }
}
