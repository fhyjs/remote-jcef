package org.eu.hanana.reimu.lib.rjcef.server;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RemoteCefSchemeHandlerFactory implements CefSchemeHandlerFactory {
    public final String uuid = UUID.randomUUID().toString();
    public final Map<String,CefResourceHandler> resourceHandlerMap= new HashMap<>();
    @Override
    public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
        return null;
    }
}
