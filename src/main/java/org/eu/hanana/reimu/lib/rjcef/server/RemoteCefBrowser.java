package org.eu.hanana.reimu.lib.rjcef.server;

import org.cef.CefClient;
import org.cef.browser.*;
import org.cef.callback.CefPdfPrintCallback;
import org.cef.callback.CefRunFileDialogCallback;
import org.cef.callback.CefStringVisitor;
import org.cef.handler.CefDialogHandler;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefWindowHandler;
import org.cef.misc.CefPdfPrintSettings;
import org.cef.network.CefRequest;
import org.eu.hanana.reimu.lib.rjcef.common.BufUtil;
import org.eu.hanana.reimu.lib.rjcef.common.CallbackResult;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class RemoteCefBrowser implements CefBrowser {
    public final String uuid;
    public final RemoteCefClient client;

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
        return false;
    }

    @Override
    public void goBack() {

    }

    @Override
    public boolean canGoForward() {
        return false;
    }

    @Override
    public void goForward() {

    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public void reload() {

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

    }

    @Override
    public void executeJavaScript(String code, String url, int line) {

    }

    @Override
    public String getURL() {
        return "";
    }

    @Override
    public void close(boolean force) {

    }

    @Override
    public void setCloseAllowed() {

    }

    @Override
    public boolean doClose() {
        return false;
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
        return null;
    }

    @Override
    public void setWindowlessFrameRate(int frameRate) {

    }

    @Override
    public CompletableFuture<Integer> getWindowlessFrameRate() {
        return null;
    }
}
