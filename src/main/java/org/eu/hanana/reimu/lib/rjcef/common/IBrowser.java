package org.eu.hanana.reimu.lib.rjcef.common;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefJSDialogCallback;
import org.cef.handler.CefJSDialogHandler;
import org.cef.misc.BoolRef;
import org.eu.hanana.reimu.lib.rjcef.client.CefBrowserMC;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IBrowser extends Closeable {
    void createImmediately();
    String getTitle();
    void onBeforeClose();
    void openDevTools();
    void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY);
    boolean onBeforePopup(CefBrowser browser, CefFrame frame, String targetUrl, String targetFrameName);
    void onJSDialog(CefBrowser browser, String originUrl, CefJSDialogHandler.JSDialogType dialogType, String messageText, String defaultPromptText, CefJSDialogCallback callback, BoolRef suppressMessage);
    void onTitleChange(CefBrowserMC cefBrowserMC, String title);
    void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height);
    default void onPaint(BufferedImage bufferedImage){};
    void mcefUpdate();
    CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution);
    void wasResized_(int width, int height);
    void mouseMoved(int x, int y, int mods);
    void mouseInteracted(int x, int y, int mods, int btn, boolean pressed, int ccnt);
    void mouseScrolled(int x, int y, int mods, int amount, int rot);
    void keyTyped(char c, int mods);
    void keyEventByKeyCode(int keyCode, int scancode, int mods, boolean pressed);

    @Override
    void close();

    static void restoreRect(ByteBuffer src,
                            Rectangle rect,
                            ByteBuffer canvas,
                            int canvasWidth) {

        int bpp = 4;
        int rowBytes = rect.width * bpp;

        for (int row = 0; row < rect.height; row++) {

            int srcPos = row * rowBytes;
            int dstPos = ((rect.y + row) * canvasWidth + rect.x) * bpp;

            ByteBuffer srcSlice = src.duplicate();
            srcSlice.position(srcPos);
            srcSlice.limit(srcPos + rowBytes);

            ByteBuffer dstSlice = canvas.duplicate();
            dstSlice.position(dstPos);

            dstSlice.put(srcSlice);
        }
    }

}
