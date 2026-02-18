package org.eu.hanana.reimu.lib.rjcef.client;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cef.CefBrowserSettings;
import org.cef.CefClient;
import org.cef.browser.*;
import org.cef.callback.CefDragData;
import org.cef.callback.CefJSDialogCallback;
import org.cef.handler.CefJSDialogHandler;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefScreenInfo;
import org.cef.misc.BoolRef;

import org.eu.hanana.reimu.lib.rjcef.common.IBrowser;
import org.eu.hanana.reimu.lib.rjcef.common.ICefRenderer;
import org.eu.hanana.reimu.lib.rjcef.common.KeyCodeUtil;
import sun.misc.Unsafe;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;

/**
 * CefBrowserMC: windowless/off-screen browser
 */
public class CefBrowserMC  extends CefBrowser_N implements CefRenderHandler , Closeable, IBrowser {
    private final long window_handler = this.hashCode();
    private static final Logger log = LogManager.getLogger(CefBrowserMC.class);
    private final ICefRenderer renderer_;
    public final Map<String,CefJSDialogCallback> jsAlertCalllbacks = new HashMap<>();
    private boolean justCreated_ = false;
    private final Rectangle browser_rect_ = new Rectangle(0, 0, 1, 1);
    private final Point screenPoint_ = new Point(0, 0);
    private final boolean isTransparent_;
    private final Component dc_ = new Component(){};
    protected  String title="Loading";
    @Setter
    public String uuid;
    public CefBrowserMC(CefClient client, String url, boolean transparent, CefRequestContext context, ICefRenderer renderer) {
        this(client, url, transparent, context, renderer, null, null,new CefBrowserSettings());
    }

    public CefBrowserMC(CefClient client, String url, boolean transparent, CefRequestContext context, ICefRenderer renderer, CefBrowserMC parent, Point inspectAt,CefBrowserSettings settings) {
        super(client, url, context, parent, inspectAt,settings);
        this.isTransparent_ = transparent;
        this.renderer_ = renderer;
    }

    @Override
    public void createImmediately() {
        this.justCreated_ = true;
        setCloseAllowed();
        this.createBrowserIfRequired(false);
    }

    public String getTitle() {
        return title;
    }

    @Override
    public synchronized void onBeforeClose() {
        super.onBeforeClose();
        renderer_.destroy();
    }

    @Override
    public Component getUIComponent() {
        return this.dc_;
    }

    @Override
    public CefRenderHandler getRenderHandler() {
        return this;
    }


    @Override
    public synchronized void openDevTools(Point inspectAt) {
        new Thread(()->createDevToolsBrowser(client_,null,CefRequestContext.getGlobalContext(),null,inspectAt).createImmediately()).start();
    }

    public ICefRenderer getRenderer_() {
        return renderer_;
    }

    @Override
    protected CefBrowserMC createDevToolsBrowser(CefClient client, String url, CefRequestContext context, CefBrowser_N parent, Point inspectAt) {
       return null;
    }

    protected long getWindowHandle() {
        return window_handler;
    }

    @Override
    public Rectangle getViewRect(CefBrowser browser) {
        return this.browser_rect_;
    }

    @Override
    public Point getScreenPoint(CefBrowser browser, Point viewPoint) {
        Point screenPoint = new Point(this.screenPoint_);
        screenPoint.translate(viewPoint.x, viewPoint.y);
        return screenPoint;
    }

    @Override
    public void onPopupShow(CefBrowser browser, boolean show) {
        if (!show) {
            this.renderer_.onPopupClosed();
            this.invalidate();
        }
    }

    @Override
    public void onPopupSize(CefBrowser browser, Rectangle size) {
        this.renderer_.onPopupSize(size);
    }

    @Override
    public void close()   {
        this.close(true);
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        MouseEvent ev = new MouseEvent(dc_, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, (int) mouseX, (int) mouseY, 1, true);
        sendMouseEvent(ev);
    }

    public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String targetUrl, String targetFrameName) {
        //browser.loadURL(targetUrl);
        return false;
    }

    public void onJSDialog(CefBrowser browser, String originUrl, CefJSDialogHandler.JSDialogType dialogType, String messageText, String defaultPromptText, CefJSDialogCallback callback, BoolRef suppressMessage) {
//        new Thread(()->{
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            callback.Continue(true,"");
//        }).start();
//        log.info("onJSDialog,{}",messageText);
        renderer_.onJsAlert(((CefBrowserMC) browser),originUrl,dialogType,messageText,defaultPromptText,callback,suppressMessage);
    }

    public void onTitleChange(CefBrowserMC cefBrowserMC, String title) {
        this.title=title;
        renderer_.onTitleChange(cefBrowserMC,title);
    }

    private static class PopupData {
        private ByteBuffer buffer;
        private int width;
        private int height;
        private Rectangle rect;   // popup 在主视图中的位置
        private boolean hasFrame;
    }
    private static class PaintData {
        private ByteBuffer buffer;
        private int width;
        private int height;
        private Rectangle[] dirtyRects;
        private boolean hasFrame;
        private boolean fullReRender;
    }

    private final PaintData paintData = new PaintData();
    private final PopupData popupData = new PopupData();

    @Override
    public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects,
                        ByteBuffer buffer, int width, int height) {
        if (popup) {
            // Popup 渲染
            synchronized (popupData) {
                final int size = (width * height) << 2;
                if (popupData.buffer == null || popupData.buffer.capacity() != size)
                    popupData.buffer = ByteBuffer.allocate(size);
                buffer.position(0);
                popupData.buffer.position(0);
                popupData.buffer.put(buffer);
                popupData.buffer.position(0);

                popupData.width = width;
                popupData.height = height;
                popupData.rect = (dirtyRects != null && dirtyRects.length > 0) ? dirtyRects[0] : new Rectangle(0,0,width,height);
                popupData.hasFrame = true;
            }
            mcefUpdate();
            return;
        }

        // 主视图渲染（原逻辑）
        final int size = (width * height) << 2;

        synchronized (paintData) {
            if (buffer.limit() > size) {
                CefBrowserMC.log.warn("Skipping MCEF browser frame, data is too heavy");
            } else {
                if (paintData.hasFrame)
                    paintData.fullReRender = true;

                if (paintData.buffer == null || size != paintData.buffer.capacity())
                    paintData.buffer = ByteBuffer.allocate(size);

                paintData.buffer.position(0);
                paintData.buffer.limit(buffer.limit());
                buffer.position(0);
                paintData.buffer.put(buffer);
                paintData.buffer.position(0);

                paintData.width = width;
                paintData.height = height;
                paintData.dirtyRects = dirtyRects;
                paintData.hasFrame = true;
            }
        }
        mcefUpdate();
    }

    @Override
    public void addOnPaintListener(Consumer<CefPaintEvent> listener) {

    }

    @Override
    public void setOnPaintListener(Consumer<CefPaintEvent> listener) {

    }

    @Override
    public void removeOnPaintListener(Consumer<CefPaintEvent> listener) {

    }

    public void mcefUpdate() {
        synchronized(paintData) {
            if(paintData.hasFrame) {
                renderer_.onPaint(false, paintData.dirtyRects, paintData.buffer, paintData.width, paintData.height, paintData.fullReRender);
                paintData.hasFrame = false;
                paintData.fullReRender = false;
            }
        }
        synchronized(popupData) {
            if(popupData.hasFrame) {
                renderer_.onPaint(true, null, popupData.buffer, popupData.width, popupData.height, false);
                popupData.hasFrame = false;
            }
        }
    }
    @Override
    public boolean onCursorChange(CefBrowser browser, int cursorType) {
        return true;
    }

    @Override
    public boolean startDragging(CefBrowser browser, CefDragData dragData, int mask, int x, int y) {
        return true;
    }

    @Override
    public void updateDragCursor(CefBrowser browser, int operation) {
    }

    private void createBrowserIfRequired(boolean hasParent) {
        long windowHandle = window_handler;
        if (this.getParentBrowser() != null) {
            windowHandle = this.getWindowHandle();
        }
        if (this.getNativeRef("CefBrowser") == 0L) {
            if (this.getParentBrowser() != null) {
                //this.createDevTools(this.getParentBrowser(), this.getClient(), windowHandle, true, this.isTransparent_, null, this.getInspectAt());
                //this.createBrowser(this.getClient(), windowHandle, this.getUrl(), true, this.isTransparent_, null, this.getRequestContext());
                log.fatal("CreateDevTools is not supported!");
            } else {
                this.createBrowser(this.getClient(), windowHandle, this.getUrl(), true, this.isTransparent_, null, this.getRequestContext());

            }
        } else if (hasParent && this.justCreated_) {
            this.notifyAfterParentChanged();
            this.setFocus(true);
            this.justCreated_ = false;
        }
    }

    private void notifyAfterParentChanged() {
        this.getClient().onAfterParentChanged(this);
    }

    @Override
    public boolean getScreenInfo(CefBrowser browser, CefScreenInfo screenInfo) {
        int depth_per_component = 8;
        int depth = 32;
        double scaleFactor_ = 1.0;
        screenInfo.Set(scaleFactor_, depth, depth_per_component, false, this.browser_rect_.getBounds(), this.browser_rect_.getBounds());
        return true;
    }

    @Override
    public CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution) {
        return renderer_.createScreenshot(nativeResolution);
    }

    @Override
    public void close(boolean force) {
        //CefRenderManager.INSTANCE.getBrowsers().remove(this);
        this.renderer_.destroy();
        super.close(force);
    }

    // these methods are fucking protected in the superclass, we need to wrap it
    public void wasResized_(int width, int height) {
        this.browser_rect_.setBounds(0, 0, width, height);
        super.wasResized(width, height);
    }

    public void mouseMoved(int x, int y, int mods) {
        MouseEvent ev = new MouseEvent(dc_, MouseEvent.MOUSE_MOVED, 0, mods, x, y, 0, false);
        sendMouseEvent(ev);

    }

    public void mouseInteracted(int x, int y, int mods, int btn, boolean pressed, int ccnt) {
        MouseEvent ev = new MouseEvent(dc_, pressed ? MouseEvent.MOUSE_PRESSED : MouseEvent.MOUSE_RELEASED, 0, mods, x, y, ccnt, false, remapMouseCode(btn));
        sendMouseEvent(ev);
    }

    private static int remapMouseCode(int kc) {
        switch (kc) {
            case 0: return 1;
            case 1: return 3;
            case 2: return 2;
            default: return 0;
        }
    }

    public void mouseScrolled(int x, int y, int mods, int amount, int rot) {
        MouseWheelEvent ev = new MouseWheelEvent(dc_, MouseEvent.MOUSE_WHEEL, 0, mods, x, y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, amount, rot);
        sendMouseWheelEvent(ev);
        System.out.println(ev);
    }

    public void keyTyped(char c, int mods) {
        KeyEvent ev = new KeyEvent(dc_, KeyEvent.KEY_TYPED, 0, mods, 0, c);
        sendKeyEvent(ev);
    }
    /**
     * fill the gap between LWJGL and AWT key codes
     * https://stackoverflow.com/questions/15313469/java-keyboard-keycodes-list/31637206
     */
    private static int remapKeycode(int kc) {
        switch(kc) {
            case GLFW_KEY_BACKSPACE: return 0x08;
            case GLFW_KEY_LEFT: return 37;
            case KeyEvent.VK_ENTER: return 0x0D;
            case KeyEvent.VK_SHIFT: return 0x10;
            case KeyEvent.VK_CONTROL: return 0x11;
            case KeyEvent.VK_ALT: return 0x12;
            case KeyEvent.VK_ESCAPE: return 0x1B;
            // 其它按键按 VK_A..VK_Z, VK_0..VK_9 映射
            default: return kc;
        }
    }

    public void keyEventByKeyCode(int keyCode, int scancode, int mods, boolean pressed) {
        System.out.printf("kc:%d,sc:%d,mod:%d,pr:%s%n", keyCode,scancode,mods, pressed);
        int cefKeyCode = remapKeycode(keyCode); // 只用 keyCode 做 CEF keyCode 映射
        char c = KeyCodeUtil.keyCodeToChar(keyCode, false);
        var ev =
                new KeyEvent(
                        dc_,
                        pressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED,
                        System.currentTimeMillis(),
                        mods,
                        cefKeyCode,
                        c==KeyEvent.CHAR_UNDEFINED?0:c,
                        KeyEvent.KEY_LOCATION_STANDARD
                );
        Unsafe theUnsafe;

// scancode = 低 8 位
        int scan = scancode & 0xFF;

        try {
            Field theUnsafe1 = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe1.setAccessible(true);
            theUnsafe= (Unsafe) theUnsafe1.get(null);
            theUnsafe.putInt(ev,theUnsafe.objectFieldOffset(ev.getClass().getDeclaredField("scancode")),scan);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(ev);
        sendKeyEvent(ev);
    }

    @SuppressWarnings("removal")
    @Override
    protected void finalize() throws Throwable {
        if(!isClosed()) {
            close(true); // NO FUCKING MEMORY LEAKS
        }
        super.finalize();
    }
}