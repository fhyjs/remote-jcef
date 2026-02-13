package org.eu.hanana.reimu.lib.rjcef.test;


import org.eu.hanana.reimu.lib.rjcef.server.RemoteCefApp;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        RemoteCefApp remoteCefApp = new RemoteCefApp();
        remoteCefApp.start();

        remoteCefApp.createApp();
        var client = remoteCefApp.createClient();
        var browser = client.createBrowser("https://baidu.com");
        browser.createImmediately();
        browser.resize(1920,1080);
        remoteCefApp.getProcess().waitFor();
    }
}
