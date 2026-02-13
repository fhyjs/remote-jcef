package org.eu.hanana.reimu.lib.rjcef.test;


import org.cef.CefApp;
import org.eu.hanana.reimu.lib.rjcef.server.RemoteCefApp;

import java.lang.reflect.Field;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        RemoteCefApp remoteCefApp = new RemoteCefApp();
        remoteCefApp.start();

        remoteCefApp.createApp();
        var client = remoteCefApp.createClient();
        var browser = client.createBrowser("https://baidu.com");
        browser.createImmediately();
        browser.resize(1920,1080);
        CefRenderStandaloneLwjglWr renderStandaloneLwjglWr=new CefRenderStandaloneLwjglWr(true);
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
