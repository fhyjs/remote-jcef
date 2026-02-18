package org.eu.hanana.reimu.lib.rjcef.common;

import org.cef.callback.CefJSDialogCallback;
import org.cef.handler.CefJSDialogHandler;
import org.cef.misc.BoolRef;
import org.eu.hanana.reimu.lib.rjcef.client.CefBrowserMC;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.eu.hanana.reimu.lib.rjcef.common.IBrowser.restoreRect;


public interface ICefRenderer {
    default void onJsAlert(IBrowser browser, String originUrl, CefJSDialogHandler.JSDialogType dialogType, String messageText, String defaultPromptText, CefJSDialogCallback callback, BoolRef suppressMessage){
        suppressMessage.set(false);
        callback.Continue(true,"hello RJCEF");
    }
    void render(double x1, double y1, double x2, double y2);

    void destroy();

    void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender);
    default void onPaint(BufferedImage bufferedImage){};
    default void onPaint(boolean popup,
                         Rectangle[] dirtyRects,
                         List<ByteBuffer> buffers,
                         int width,
                         int height,
                         boolean completeReRender) {

        int fullSize = width * height * 4;

        ByteBuffer canvas = ByteBuffer.allocateDirect(fullSize);

        for (int i = 0; i < dirtyRects.length; i++) {

            Rectangle rect = dirtyRects[i];
            ByteBuffer src = buffers.get(i);

            restoreRect(src, rect, canvas, width);
        }

        // 调用旧实现
        onPaint(popup, dirtyRects, canvas, width, height,completeReRender);
    }

    void onPopupSize(Rectangle var1);

    void onPopupClosed();

    default void onTitleChange(IBrowser cefBrowserMC, String title){}

    CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution);
}