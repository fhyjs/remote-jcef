package org.eu.hanana.reimu.lib.rjcef.test;

import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefRequestContext;
import org.eu.hanana.reimu.lib.rjcef.client.CefBrowserMC;
import org.eu.hanana.reimu.lib.rjcef.client.CefNettyClient;
import org.eu.hanana.reimu.lib.rjcef.client.CelInstaller;
import org.eu.hanana.reimu.lib.rjcef.server.NettyTcpServer;
import org.eu.hanana.reimu.lib.rjcef.server.RemoteCefApp;

import java.io.IOException;

public class StartHeadlessCef {
    public static void main(String[] args) throws UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {
        RemoteCefApp remoteCefApp = new RemoteCefApp();
        remoteCefApp.start();


        var cefApp = CelInstaller.getBuilder().build();
        CefClient client = cefApp.createClient();
        //var browser = new CefBrowserMC(client,"https://baidu.com",true, CefRequestContext.getGlobalContext(),new CefNettyClient(51401));
        //browser.wasResized_(1920,1808);
        //browser.createImmediately();;
    }
}
