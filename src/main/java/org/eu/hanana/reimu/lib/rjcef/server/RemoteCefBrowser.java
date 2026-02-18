package org.eu.hanana.reimu.lib.rjcef.server;

import org.cef.CefClient;
import org.cef.browser.*;
import org.cef.callback.CefJSDialogCallback;
import org.cef.callback.CefPdfPrintCallback;
import org.cef.callback.CefRunFileDialogCallback;
import org.cef.callback.CefStringVisitor;
import org.cef.handler.CefDialogHandler;
import org.cef.handler.CefJSDialogHandler;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefWindowHandler;
import org.cef.misc.BoolRef;
import org.cef.misc.CefPdfPrintSettings;
import org.cef.network.CefRequest;
import org.eu.hanana.reimu.lib.rjcef.client.CefBrowserMC;
import org.eu.hanana.reimu.lib.rjcef.common.BufUtil;
import org.eu.hanana.reimu.lib.rjcef.common.CallbackResult;
import org.eu.hanana.reimu.lib.rjcef.common.IBrowser;
import org.eu.hanana.reimu.lib.rjcef.common.ICefRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class RemoteCefBrowser implements CefBrowser, IBrowser {
    public final String uuid;
    public final RemoteCefClient client;
    public ICefRenderer cefRenderer = new ICefRenderer() {
        @Override
        public void render(double x1, double y1, double x2, double y2) {

        }

        @Override
        public void destroy() {

        }

        @Override
        public void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender) {

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
    };
    public RemoteCefBrowser(String uuid,RemoteCefClient cefClient){
        this.uuid=uuid;
        this.client=cefClient;
    }

    @Override
    public void createImmediately() {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.CREATE_BROWSER_IMMEDIATELY,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);
        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getTitle() {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_getTitle,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);


        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return BufUtil.readString(cr.get().result);
    }

    public void resize(int w,int h) {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_RESIZE,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        bb.writeInt(w);
        bb.writeInt(h);

        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public Component getUIComponent() {
        return null;
    }

    @Override
    public CefClient getClient() {
        return null;
    }

    @Override
    public CefRenderHandler getRenderHandler() {
        return null;
    }

    @Override
    public CefRequestContext getRequestContext() {
        return null;
    }

    @Override
    public CefWindowHandler getWindowHandler() {
        return null;
    }

    @Override
    public boolean canGoBack() {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_canGoBack,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);


        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return cr.get().result.readBoolean();
    }

    @Override
    public void goBack() {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_goBack,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean canGoForward() {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_canGoForward,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);


        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return cr.get().result.readBoolean();
    }

    @Override
    public void goForward() {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_goForward,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean isLoading() {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_isLoading,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);


        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return cr.get().result.readBoolean();
    }

    @Override
    public void reload() {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_reload,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return ;
    }

    @Override
    public void reloadIgnoreCache() {

    }

    @Override
    public void stopLoad() {

    }

    @Override
    public int getIdentifier() {
        return 0;
    }

    @Override
    public CefFrame getMainFrame() {
        return null;
    }

    @Override
    public CefFrame getFocusedFrame() {
        return null;
    }

    @Override
    public CefFrame getFrameByIdentifier(String identifier) {
        return null;
    }

    @Override
    public CefFrame getFrameByName(String name) {
        return null;
    }

    @Override
    public Vector<String> getFrameIdentifiers() {
        return null;
    }

    @Override
    public Vector<String> getFrameNames() {
        return null;
    }

    @Override
    public int getFrameCount() {
        return 0;
    }

    @Override
    public boolean isPopup() {
        return false;
    }

    @Override
    public boolean hasDocument() {
        return false;
    }

    @Override
    public void viewSource() {

    }

    @Override
    public void getSource(CefStringVisitor visitor) {

    }

    @Override
    public void getText(CefStringVisitor visitor) {

    }

    @Override
    public void loadRequest(CefRequest request) {

    }

    @Override
    public void loadURL(String url) {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_loadURL,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        BufUtil.writeString(url,bb);

        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return ;
    }

    @Override
    public void executeJavaScript(String code, String url, int line) {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_executeJavaScript,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        BufUtil.writeString(code,bb);
        BufUtil.writeString(url,bb);
        bb.writeInt(line);

        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return ;
    }

    @Override
    public String getURL() {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_getURL,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return BufUtil.readString(cr.get().result);
    }

    @Override
    public void close(boolean force) {
        doClose();
    }

    @Override
    public void setCloseAllowed() {

    }

    @Override
    public boolean doClose() {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_doClose,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client.app);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        cefRenderer.destroy();
        client.browserMap.remove(uuid);
        return true;
    }

    @Override
    public void onBeforeClose() {

    }

    @Override
    public void setFocus(boolean enable) {

    }

    @Override
    public void setWindowVisibility(boolean visible) {

    }

    @Override
    public double getZoomLevel() {
        return 0;
    }

    @Override
    public void setZoomLevel(double zoomLevel) {

    }

    @Override
    public void runFileDialog(CefDialogHandler.FileDialogMode mode, String title, String defaultFilePath, Vector<String> acceptFilters, int selectedAcceptFilter, CefRunFileDialogCallback callback) {

    }

    @Override
    public void startDownload(String url) {

    }

    @Override
    public void print() {

    }

    @Override
    public void printToPDF(String path, CefPdfPrintSettings settings, CefPdfPrintCallback callback) {

    }

    @Override
    public void find(String searchText, boolean forward, boolean matchCase, boolean findNext) {

    }

    @Override
    public void stopFinding(boolean clearSelection) {

    }

    @Override
    public void openDevTools() {

    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {

    }

    @Override
    public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String targetUrl, String targetFrameName) {
        return false;
    }

    @Override
    public void onJSDialog(CefBrowser browser, String originUrl, CefJSDialogHandler.JSDialogType dialogType, String messageText, String defaultPromptText, CefJSDialogCallback callback, BoolRef suppressMessage) {
        cefRenderer.onJsAlert(this,originUrl,dialogType,messageText,defaultPromptText,callback,suppressMessage);
    }

    @Override
    public void onTitleChange(CefBrowserMC cefBrowserMC, String title) {
        cefRenderer.onTitleChange(this,title);
    }

    @Override
    public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height) {

    }

    @Override
    public void mcefUpdate() {

    }

    @Override
    public void openDevTools(Point inspectAt) {

    }

    @Override
    public void closeDevTools() {

    }

    @Override
    public CefDevToolsClient getDevToolsClient() {
        return null;
    }

    @Override
    public void replaceMisspelling(String word) {

    }

    @Override
    public CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution) {
        return cefRenderer.createScreenshot(nativeResolution);
    }

    @Override
    public void wasResized_(int width, int height) {
        resize(width,height);
    }

    @Override
    public void mouseMoved(int x, int y, int mods) {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_mouseMoved,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        bb.writeInt(x);
        bb.writeInt(y);
        bb.writeInt(mods);

        client.app.client.writeAndFlush(bb);
    }

    @Override
    public void mouseInteracted(int x, int y, int mods, int btn, boolean pressed, int ccnt) {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_mouseInteracted,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        bb.writeInt(x);
        bb.writeInt(y);
        bb.writeInt(mods);
        bb.writeInt(btn);
        bb.writeBoolean(pressed);
        bb.writeInt(ccnt);

        client.app.client.writeAndFlush(bb);
    }

    @Override
    public void mouseScrolled(int x, int y, int mods, int amount, int rot) {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_mouseScrolled,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        bb.writeInt(x);
        bb.writeInt(y);
        bb.writeInt(mods);
        bb.writeInt(amount);
        bb.writeInt(rot);

        client.app.client.writeAndFlush(bb);
    }

    @Override
    public void keyTyped(char c, int mods) {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_keyTyped,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        bb.writeChar(c);
        bb.writeInt(mods);

        client.app.client.writeAndFlush(bb);
    }

    @Override
    public void keyEventByKeyCode(int keyCode, int scancode, int mods, boolean pressed) {
        var bb = client.app.client.alloc().directBuffer();
        BufUtil.writeString(client.app.remoteCommands.BROWSER_keyEventByKeyCode,bb);
        BufUtil.writeString(client.uuid,bb);
        BufUtil.writeString(uuid,bb);

        bb.writeInt(keyCode);
        bb.writeInt(scancode);
        bb.writeInt(mods);
        bb.writeBoolean(pressed);

        client.app.client.writeAndFlush(bb);
    }

    @Override
    public void close() {
        doClose();
    }

    @Override
    public void setWindowlessFrameRate(int frameRate) {

    }

    @Override
    public CompletableFuture<Integer> getWindowlessFrameRate() {
        return null;
    }
}
