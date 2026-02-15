package org.eu.hanana.reimu.lib.rjcef.test;


import org.cef.CefApp;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCallback;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefLoadHandler;
import org.cef.handler.CefResourceHandler;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.eu.hanana.reimu.lib.rjcef.server.RemoteCefApp;
import org.eu.hanana.reimu.lib.rjcef.server.RemoteCefAppHandlerAdapter;
import org.eu.hanana.reimu.lib.rjcef.server.RemoteCefSchemeHandlerFactory;
import org.eu.hanana.reimu.lib.rjcef.server.RemoteCefSchemeRegistrar;

import java.io.InputStream;
import java.lang.reflect.Field;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CefRenderStandaloneLwjglWr renderStandaloneLwjglWr=new CefRenderStandaloneLwjglWr(true);
        RemoteCefApp remoteCefApp = new RemoteCefApp(){
            @Override
            protected void setConnected(boolean connected) {
                super.setConnected(connected);
                if (!connected){
                    renderStandaloneLwjglWr.addTask(renderStandaloneLwjglWr::destroy);
                }
            }
        };
        remoteCefApp.start();

        remoteCefApp.remoteCefAppHandlerAdapter=new RemoteCefAppHandlerAdapter(){
            @Override
            public void onRegisterCustomSchemes(RemoteCefSchemeRegistrar registrar) {
                super.onRegisterCustomSchemes(registrar);
                registrar.addCustomScheme("rjcef",
                        true,            // isStandard
                        false,             // isLocal
                        false,            // isDisplayIsolated
                        true,            // isSecure
                        true,             // isCorsEnabled
                        true,            // isCspBypassing
                        true              // is_fetch_enabled
                );
                registrar.addRequestProcessor("rjcef",new RemoteCefSchemeHandlerFactory(){
                    @Override
                    public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
                        return new CefResourceHandlerAdapter() {
                            private InputStream stream;

                            @Override
                            public boolean processRequest(CefRequest request, CefCallback callback) {
                                try {
                                    // 去掉协议前缀 classpath://
                                    String path = request.getURL().substring("rjcef://".length());
                                    if (path.contains("?")){
                                        path=path.split("\\?")[0];
                                    }
                                    stream = getClass().getClassLoader().getResourceAsStream(path);
                                    callback.Continue();
                                    return true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            }

                            @Override
                            public void getResponseHeaders(CefResponse response, IntRef responseLength,
                                                           StringRef redirectUrl) {
                                response.setMimeType("text/html");
                                if (stream != null) {
                                    try {
                                        responseLength.set(stream.available());
                                    } catch (Exception e) {
                                        responseLength.set(0);
                                    }
                                    response.setStatus(200);
                                } else {
                                    response.setStatus(404);
                                    responseLength.set(0);
                                }
                            }

                            @Override
                            public boolean readResponse(byte[] dataOut, int bytesToRead, IntRef bytesRead,
                                                        CefCallback callback) {
                                try {
                                    if (stream == null) return false;
                                    int len = stream.read(dataOut, 0, bytesToRead);
                                    if (len == -1) return false;
                                    bytesRead.set(len);
                                    return true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            }

                            @Override
                            public void cancel() {
                                try {
                                    if (stream != null) stream.close();
                                } catch (Exception ignored) {}
                            }
                        };
                    }
                });
            }
        };

        remoteCefApp.createApp();
        var client = remoteCefApp.createClient();
        var browser = client.createBrowser("rjcef://rjcef/index.html");
        browser.createImmediately();
        browser.resize(1920,1080);
        browser.cefRenderer=renderStandaloneLwjglWr;
        renderStandaloneLwjglWr.cefBrowserMC=browser;
        renderStandaloneLwjglWr.addTask(()->{
            browser.reload();;
            //browserMC.openDevTools();
        });
        new Thread(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            browser.doClose();
            client.destroy();
            remoteCefApp.destroy();
            System.exit(0);
        });
        renderStandaloneLwjglWr.run();
    }
}
