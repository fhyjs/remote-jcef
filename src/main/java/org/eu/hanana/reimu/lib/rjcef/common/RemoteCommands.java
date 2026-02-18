package org.eu.hanana.reimu.lib.rjcef.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class RemoteCommands {



    private final CallbackRegister cr;
    public Map<String, Function<Tuple<ByteBuf, ChannelHandlerContext>,ByteBuf>> processor = new HashMap<>();
    public final String CLIENT_DSETROY = "cli_des";
    public final String BROWSER_RESIZE = "bro_resize";
    public final String BROWSER_mouseInteracted = "BROWSER_mouseInteracted";
    public final String BROWSER_keyTyped = "BROWSER_keyTyped";
    public final String BROWSER_keyEventByKeyCode = "BROWSER_keyEventByKeyCode";
    public final String BROWSER_doClose = "BROWSER_doClose";
    public final String BROWSER_mouseScrolled = "BROWSER_mouseScrolled";
    public final String BROWSER_mouseMoved = "BROWSER_mouseMoved";
    public final String BROWSER_getTitle = "BROWSER_getTitle";
    public final String BROWSER_canGoBack = "BROWSER_mcanGoBack";
    public final String BROWSER_canGoForward = "BROWSER_canGoForward";
    public final String BROWSER_isLoading = "BROWSER_isLoading";
    public final String BROWSER_reload = "BROWSER_reload";
    public final String BROWSER_loadURL = "BROWSER_loadURL";
    public final String BROWSER_executeJavaScript = "BROWSER_executeJavaScript";
    public final String BROWSER_getURL = "BROWSER_getURL";
    public final String BROWSER_goBack = "BROWSER_goBack";
    public final String BROWSER_goForward = "BROWSER_goForward";
    public final String BROWSER_onTitleChange = "BROWSER_onTitleChange";
    public final String APPHANDLER_onContextInitialized = "APPHANDLER_onContextInitialized";
    public final String APPHANDLER_onRegisterCustomSchemes = "APPHANDLER_onRegisterCustomSchemes";
    public final String APPHANDLER_stateHasChanged = "APPHANDLER_stateHasChanged";
    public final String APP_addCustomScheme = "APP_addCustomScheme";
    public final String CefResourceHandlerAdapter_processRequest = "CefResourceHandlerAdapter_processRequest";
    public final String CefResourceHandlerAdapter_finalize = "CefResourceHandlerAdapter_finalize";
    public final String CefResourceHandlerAdapter_cancel = "CefResourceHandlerAdapter_cancel";
    public final String CefResourceHandlerAdapter_readResponse = "CefResourceHandlerAdapter_readResponse";
    public final String CefResourceHandlerAdapter_getResponseHeaders = "CefResourceHandlerAdapter_getResponseHeaders";
    public final String APP_addRequestProcessor = "APP_addRequestProcessor";
    public final String NetCefSchemeHandlerFactory_create = "NetCefSchemeHandlerFactory_create";
    public final String CREATE_APP = "ct_app";
    public final String CREATE_BROWSER = "ct_bro";
    public final String CREATE_BROWSER_IMMEDIATELY = "ct_bro_imm";
    public final String CREATE_CLIENT = "ct_cli";
    public final String BROWSER_ONPAINT = "bro_onp";
    public final String BROWSER_onJsAlert = "bro_onJsAlert";
    public final String CONFIRM_START = "cf_star";
    public RemoteCommands(CallbackRegister register){
        this.cr=register;
        processor.put("withcallback",tuple -> {
            var uuid = BufUtil.readString(tuple.a());
            String cmd = BufUtil.readString(tuple.a());
            System.out.println("withcallback: cmd: "+ cmd);
            ByteBuf byteBuf = tuple.b().alloc().directBuffer();
            BufUtil.writeString("callback",byteBuf);
            BufUtil.writeString(uuid,byteBuf);

            try {
                ByteBuf apply = processor.get(cmd).apply(new Tuple<>(tuple.a(), tuple.b()));
                if (apply==null){
                    apply= Unpooled.buffer(0);
                }
                byteBuf.writeBoolean(true);
                //byteBuf.writeInt(apply.readableBytes());
                byteBuf.writeBytes(apply, apply.readerIndex(), apply.readableBytes());
                apply.release();
            } catch (Throwable e) {
                byteBuf.writeBoolean(false);
                try {
                    BufUtil.writeThrowable(byteBuf,e);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            tuple.b().writeAndFlush(byteBuf);
            return null;
        });
        processor.put("callback",tuple -> {
            var uuid = BufUtil.readString(tuple.a());
            var callback = cr.getCallback(uuid);
            cr.removeCallback(uuid);
            callback.accept(tuple);
            return null;
        });
    }

    public void regHandler(String c, Function<Tuple<ByteBuf, ChannelHandlerContext>,ByteBuf> p){
        processor.put(c,p);
    }
}
