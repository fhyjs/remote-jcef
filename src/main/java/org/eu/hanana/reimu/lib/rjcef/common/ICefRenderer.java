package org.eu.hanana.reimu.lib.rjcef.common;

import org.cef.callback.CefJSDialogCallback;
import org.cef.handler.CefJSDialogHandler;
import org.cef.misc.BoolRef;
import org.eu.hanana.reimu.lib.rjcef.client.CefBrowserMC;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;


public interface ICefRenderer {
    default void onJsAlert(CefBrowserMC browser, String originUrl, CefJSDialogHandler.JSDialogType dialogType, String messageText, String defaultPromptText, CefJSDialogCallback callback, BoolRef suppressMessage){
        callback.Continue(false,"");
    }
    void render(double x1, double y1, double x2, double y2);

    void destroy();

    void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender);
    default void onPaint(BufferedImage bufferedImage){};

    void onPopupSize(Rectangle var1);

    void onPopupClosed();

    default void onTitleChange(CefBrowserMC cefBrowserMC, String title){}

    CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution);
}