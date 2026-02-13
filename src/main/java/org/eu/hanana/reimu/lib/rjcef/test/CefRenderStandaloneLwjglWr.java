package org.eu.hanana.reimu.lib.rjcef.test;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.hanana.reimu.lib.rjcef.client.CefBrowserMC;
import org.eu.hanana.reimu.lib.rjcef.common.IBrowser;
import org.eu.hanana.reimu.lib.rjcef.common.ICefRenderer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.sql.Types.NULL;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBInternalformatQuery2.GL_TEXTURE_2D;
import static org.lwjgl.opengl.EXTBGRA.GL_BGRA_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class CefRenderStandaloneLwjglWr implements ICefRenderer {
    private static final Logger log = LogManager.getLogger(CefRenderStandaloneLwjglWr.class);
    public int texture_id_ = 0;
    public IBrowser cefBrowserMC;
    private int view_width_ = 0;
    private int view_height_ = 0;
    private Rectangle popup_rect_ = new Rectangle(0, 0, 0, 0);
    private Rectangle original_popup_rect_ = new Rectangle(0, 0, 0, 0);
    private final boolean transparent;


    public CefRenderStandaloneLwjglWr(boolean transparent) {
        this.transparent=transparent;
    }

    @Override
    public void onTitleChange(CefBrowserMC cefBrowserMC, String title) {
        ICefRenderer.super.onTitleChange(cefBrowserMC, title);
        addTask(() -> glfwSetWindowTitle(window, title));
    }

    @Override
    public CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution) {
        CompletableFuture<BufferedImage> future = new CompletableFuture<>();

        // 提交任务到 GL 线程，确保读取纹理安全
        addTask(() -> {
            try {
                if (texture_id_ == 0 || view_width_ == 0 || view_height_ == 0) {
                    future.completeExceptionally(new IllegalStateException("No texture available"));
                    return;
                }

                // 绑定纹理
                glBindTexture(GL_TEXTURE_2D, texture_id_);

                // 读取像素数据
                ByteBuffer buffer = ByteBuffer.allocateDirect(view_width_ * view_height_ * 4).order(ByteOrder.nativeOrder());
                glGetTexImage(GL_TEXTURE_2D, 0, GL_BGRA_EXT, GL_UNSIGNED_BYTE, buffer);

                BufferedImage image = new BufferedImage(view_width_, view_height_, BufferedImage.TYPE_INT_ARGB);

                for (int y = 0; y < view_height_; y++) {
                    for (int x = 0; x < view_width_; x++) {
                        int i = ((view_height_ - 1 - y) * view_width_ + x) * 4;
                        int b = buffer.get(i) & 0xFF;
                        int g = buffer.get(i + 1) & 0xFF;
                        int r = buffer.get(i + 2) & 0xFF;
                        int a = buffer.get(i + 3) & 0xFF;
                        int pixel = (a << 24) | (r << 16) | (g << 8) | b;
                        image.setRGB(x, y, pixel);
                    }
                }

                glBindTexture(GL_TEXTURE_2D, 0);
                future.complete(image);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }


    @Override
    public void render(double x1, double y1, double x2, double y2) {
        var tasks = new ArrayList<>(this.tasks);
        for (Runnable task : tasks) {
            try {
                task.run();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        this.tasks.removeAll(tasks);
        //if(view_width_ == 0 || view_height_ == 0)
       //     return;
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL_BLEND);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture_id_);
        GL11.glColor4f(1f, 1f, 1f, 1f);

// 顶点坐标和纹理坐标
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0f, 0f); GL11.glVertex2f((float)x1, (float)y1);
        GL11.glTexCoord2f(1f, 0f); GL11.glVertex2f((float)x2, (float)y1);
        GL11.glTexCoord2f(1f, 1f); GL11.glVertex2f((float)x2, (float)y2);
        GL11.glTexCoord2f(0f, 1f); GL11.glVertex2f((float)x1, (float)y2);
        GL11.glEnd();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL_BLEND);
    }
    private boolean destroyed = false;
    @Override
    public void destroy() {
        if (destroyed) return;
        if (Thread.currentThread()!=thread){
            addTask(this::destroy);
            return;
        }
        destroyed=true;
        if(texture_id_ != 0) {
            glDeleteTextures(texture_id_);
            texture_id_ = 0;
        }
        cefBrowserMC.close();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        //glfwTerminate();
        //glfwSetErrorCallback(null).free();
    }

    @Override
    public void onPaint(BufferedImage bufferedImage) {
        if (destroyed) return;
        if (Thread.currentThread()!=thread){
            addTask(()->onPaint(bufferedImage));
            return;
        }
        if (texture_id_!=0){
            GL11.glDeleteTextures(texture_id_);
        }
        try {
            texture_id_=loadPNGTexture(bufferedImage);
        } catch (Exception e) {

        }
    }

    @Override
    public void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender) {
        if (destroyed) return;
        if (Thread.currentThread()!=thread){
            addTask(()->onPaint(popup, dirtyRects, buffer, width, height, completeReRender));
            return;
        }
        //if (true) return;
        if (texture_id_ == 0) {
            // 确保纹理已生成
            texture_id_ = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture_id_);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }

        if (transparent) {
            // 开启 alpha blending
            GL11.glEnable(GL_BLEND);
        }

        final int size = (width * height) << 2;
        if (size > buffer.limit()) {
            log.warn("Bad data passed to CefRenderer.onPaint() (buffer too small)");
            return;
        }

        // 绑定纹理
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture_id_);

        int oldAlignment = glGetInteger(GL_UNPACK_ALIGNMENT);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        // 如果是 popup
        if (popup && popup_rect_.width > 0 && popup_rect_.height > 0) {
            int skipPixels = 0, x = popup_rect_.x;
            int skipRows = 0, y = popup_rect_.y;
            int w = width, h = height;

            if (x < 0) { skipPixels = -x; x = 0; }
            if (y < 0) { skipRows = -y; y = 0; }
            if (x + w > view_width_) w = view_width_ - x;
            if (y + h > view_height_) h = view_height_ - y;

            glPixelStorei(GL_UNPACK_ROW_LENGTH, width);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, skipPixels);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, skipRows);
            glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, w, h, GL_BGRA_EXT, GL_UNSIGNED_BYTE, buffer);

            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        } else {
            // onPaint 更新纹理
            if (completeReRender || width != view_width_ || height != view_height_) {
                view_width_ = width;
                view_height_ = height;
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_BGRA_EXT, GL_UNSIGNED_BYTE, buffer);
            } else {
                // dirty rect 更新
                glPixelStorei(GL_UNPACK_ROW_LENGTH, width);
                for (Rectangle rect : dirtyRects) {
                    buffer.position((rect.y * width + rect.x) * 4);
                    buffer.limit(buffer.position() + rect.width * rect.height * 4);
                    glTexSubImage2D(GL_TEXTURE_2D, 0, rect.x, rect.y, rect.width, rect.height,
                            GL_BGRA_EXT, GL_UNSIGNED_BYTE, buffer.slice());
                    buffer.clear();
                }
                glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            }
        }

        // 恢复像素对齐
        glPixelStorei(GL_UNPACK_ALIGNMENT, oldAlignment);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    @Override
    public void onPopupSize(Rectangle rect) {
        if(rect.width <= 0 || rect.height <= 0)
            return;
        original_popup_rect_ = rect;
        popup_rect_ = getPopupRectInWebView(original_popup_rect_);
    }

    @SuppressWarnings("removal")
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }
    protected Rectangle getPopupRectInWebView(Rectangle rc) {
        // if x or y are negative, move them to 0.
        if(rc.x < 0)
            rc.x = 0;
        if(rc.y < 0)
            rc.y = 0;
        // if popup goes outside the view, try to reposition origin
        if(rc.x + rc.width > view_width_)
            rc.x = view_width_ - rc.width;
        if(rc.y + rc.height > view_height_)
            rc.y = view_height_ - rc.height;
        // if x or y became negative, move them to 0 again.
        if(rc.x < 0)
            rc.x = 0;
        if(rc.y < 0)
            rc.y = 0;
        return rc;
    }
    @Override
    public void onPopupClosed() {
        popup_rect_.setBounds(0, 0, 0, 0);
        original_popup_rect_.setBounds(0, 0, 0, 0);
    }
    public long window;

    public void run() {
        init();
        loop();
        destroy();
    }
    private boolean leftMouseDown = false;
    private static boolean init=false;
    private Thread thread;
    private void init() {
        if (!init) {
            init=true;
            GLFWErrorCallback.createPrint(System.err).set();
            if (!glfwInit())
                throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(1000, 1000, "LWJGL Window", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        thread=Thread.currentThread();
        // 居中窗口
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // vsync

        glfwShowWindow(window);

        var tasks = new ArrayList<>(this.tasks);
        for (Runnable task : tasks) {
            task.run();
        }
        this.tasks.removeAll(tasks);
        cefBrowserMC.wasResized_(1000,1000);
        glfwSetFramebufferSizeCallback(window, (win, width, height) -> {
            //System.out.println("Framebuffer size: " + width + " x " + height);

            // 例如你的 CEF 浏览器：
            if (width<=0||height<=0) return;
            cefBrowserMC.wasResized_(width, height);
            // 更新你的渲染器纹理大小
        });
        glfwSetScrollCallback(window, (win, xOffset, yOffset) -> {
            if (cefBrowserMC == null) return;
            int mouseX=0,mouseY=0;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                DoubleBuffer px = stack.mallocDouble(1);
                DoubleBuffer py = stack.mallocDouble(1);
                glfwGetCursorPos(window, px, py);
                mouseX = (int) px.get(0);
                mouseY = (int) py.get(0);
            }
            cefBrowserMC.mouseScrolled((int) mouseX, (int) mouseY,0, (int) yOffset,1);
        });
        glfwSetCharCallback(window,(window1, codepoint) -> {
            cefBrowserMC.keyTyped((char) codepoint,0);
        });
        glfwSetKeyCallback(window,(window1, key, scancode, action, mods) -> {
            if (key==GLFW_KEY_F12&&action==GLFW_PRESS){
                cefBrowserMC.openDevTools();
            }
            cefBrowserMC.keyEventByKeyCode(key,scancode,0,action==GLFW_PRESS);
        });
        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (cefBrowserMC == null) return;
            int mouseX=0,mouseY=0;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                DoubleBuffer px = stack.mallocDouble(1);
                DoubleBuffer py = stack.mallocDouble(1);
                glfwGetCursorPos(window, px, py);
                mouseX = (int) px.get(0);
                mouseY = (int) py.get(0);
            }

            switch (button) {
                case GLFW_MOUSE_BUTTON_LEFT -> {
                    if (action == GLFW_PRESS) {
                        leftMouseDown=true;
                        cefBrowserMC.mouseInteracted(mouseX, mouseY, 0,0,true,1);
                    } else if (action == GLFW_RELEASE) {
                        leftMouseDown=false;
                        cefBrowserMC.mouseInteracted(mouseX, mouseY, 0,0,false,1);
                    }
                }
                case GLFW_MOUSE_BUTTON_RIGHT -> {
                    if (action == GLFW_PRESS) {
                        cefBrowserMC.mouseInteracted(mouseX, mouseY, 0,1,true,1);
                    } else if (action == GLFW_RELEASE) {
                        cefBrowserMC.mouseInteracted(mouseX, mouseY, 0,1,false,1);
                    }
                }
                case GLFW_MOUSE_BUTTON_MIDDLE -> {
                    if (action == GLFW_PRESS) {
                        cefBrowserMC.mouseInteracted(mouseX, mouseY, 0,2,true,1);
                    } else if (action == GLFW_RELEASE) {
                        cefBrowserMC.mouseInteracted(mouseX, mouseY, 0,2,false,1);
                    }
                }
            }
        });

// 鼠标移动
        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (cefBrowserMC == null) return;
            cefBrowserMC.mouseMoved((int) xpos, (int) ypos, 0);
            if (leftMouseDown){
                cefBrowserMC.mouseDragged(xpos,ypos,0,0,0);
            }
        });
    }

    private void exInit() {
        try {
            texture_id_ = GL11.glGenTextures();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        GL11.glBindTexture(GL_TEXTURE_2D, texture_id_);
        GL11.glBindTexture(GL_TEXTURE_2D, texture_id_);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GL11.glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void loop() {
        if (destroyed) return;
        GL.createCapabilities();
        exInit();
        GL11.glClearColor(1f, 1f, 1f, 1.0f);

        while (!glfwWindowShouldClose(window)&&!destroyed) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            try (MemoryStack stack = stackPush()) {
                IntBuffer pWidth = stack.mallocInt(1);
                IntBuffer pHeight = stack.mallocInt(1);
                glfwGetFramebufferSize(window, pWidth, pHeight);
                var view_width_ = pWidth.get(0);
                var view_height_ = pHeight.get(0);
                GL11.glViewport(0,0,view_width_,view_height_);
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadIdentity();
                GL11.glOrtho(0, view_width_, view_height_, 0, -1, 1);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();
                cefBrowserMC.mcefUpdate();
                render(0,0,view_width_,view_height_);
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
    public static int loadPNGTexture(BufferedImage image) throws Exception {
        int width = image.getWidth();
        int height = image.getHeight();

        // 创建 ByteBuffer 存储 RGBA 数据
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());

        for (int y = 0; y < height; y++) { // 从上到下
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                buffer.put((byte) (pixel & 0xFF));         // B
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
            }
        }
        buffer.flip();

        // 生成 OpenGL 纹理
        int textureID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        // 上传纹理数据
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0,
                GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buffer);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return textureID;
    }
    public static void main(String[] args) throws Exception {

        new CefRenderStandaloneLwjglWr(false).run();
    }

    public void addTask(Runnable task) {
        tasks.add(task);
    }
    List<Runnable> tasks = new ArrayList<>();
}
