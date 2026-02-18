package org.eu.hanana.reimu.lib.rjcef.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import org.cef.callback.CefJSDialogCallback;
import org.cef.handler.CefJSDialogHandler;
import org.cef.misc.BoolRef;
import org.eu.hanana.reimu.lib.rjcef.common.BufUtil;
import org.eu.hanana.reimu.lib.rjcef.common.IBrowser;
import org.eu.hanana.reimu.lib.rjcef.common.ICefRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ClientRender implements ICefRenderer {
    private final String uuid;
    private final ClientMain client;
    private final String clientUuid;

    public ClientRender(String uuid, String uuidClient, ClientMain clientMain) {
        this.uuid=uuid;
        this.client=clientMain;
        this.clientUuid=uuidClient;
    }

    @Override
    public void onJsAlert(IBrowser browser, String originUrl, CefJSDialogHandler.JSDialogType dialogType, String messageText, String defaultPromptText, CefJSDialogCallback callback, BoolRef suppressMessage) {
        ByteBuf headerBuf = client.cnc.channel.alloc().buffer();
        BufUtil.writeString(client.remoteCommands.BROWSER_onJsAlert, headerBuf);
        BufUtil.writeString(clientUuid, headerBuf);
        BufUtil.writeString(uuid, headerBuf);

        var uuidAlert = UUID.randomUUID().toString();
        BufUtil.writeString(uuidAlert, headerBuf);

        BufUtil.writeString(originUrl, headerBuf);
        BufUtil.writeEnum(dialogType, headerBuf);
        BufUtil.writeString(messageText,headerBuf);
        BufUtil.writeString(defaultPromptText,headerBuf);

        ((CefBrowserMC) browser).jsAlertCalllbacks.put(uuidAlert,callback);
        client.cnc.channel.writeAndFlush(headerBuf);
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
        BufUtil.writeString(clientUuid, headerBuf);
        BufUtil.writeString(uuid, headerBuf);
        headerBuf.writeBoolean(popup);
        BufUtil.writeRectangles(dirtyRects, headerBuf);
        headerBuf.writeInt(width);
        headerBuf.writeInt(height);
        headerBuf.writeBoolean(completeReRender);

        composite.addComponent(true, headerBuf);

        // 发送每个 dirty rect 数据
        // framebuffer 每行字节数
        final int stride = width * 4;

        for (Rectangle rect : dirtyRects) {

            final int rowBytes = rect.width * 4;
            final int rectBytes = rect.height * rowBytes;

            ByteBuf rectBuf = client.cnc.channel.alloc()
                    .directBuffer(rectBytes);

            // 只 duplicate 一次
            ByteBuffer src = buffer.duplicate();

            // 起始偏移
            int start = rect.y * stride + rect.x * 4;

            for (int row = 0; row < rect.height; row++) {

                int offset = start + row * stride;

                src.clear().position(offset).limit(offset + rowBytes);
                rectBuf.writeBytes(src);

                rectBuf.writeBytes(src);
            }

            composite.addComponent(true, rectBuf);
        }

        client.cnc.channel.writeAndFlush(composite);
    }

    @Override
    public void onTitleChange(IBrowser cefBrowserMC, String title) {
        ICefRenderer.super.onTitleChange(cefBrowserMC, title);
        ByteBuf headerBuf = client.cnc.channel.alloc().buffer();
        BufUtil.writeString(client.remoteCommands.BROWSER_onTitleChange, headerBuf);
        BufUtil.writeString(clientUuid, headerBuf);
        BufUtil.writeString(uuid, headerBuf);
        BufUtil.writeString(title, headerBuf);
        client.cnc.channel.writeAndFlush(headerBuf);
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
