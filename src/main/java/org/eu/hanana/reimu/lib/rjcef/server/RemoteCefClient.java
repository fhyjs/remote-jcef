package org.eu.hanana.reimu.lib.rjcef.server;

import org.eu.hanana.reimu.lib.rjcef.common.BufUtil;
import org.eu.hanana.reimu.lib.rjcef.common.CallbackResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class RemoteCefClient {
    public final String uuid;
    public final RemoteCefApp app;
    public final Map<String,RemoteCefBrowser> browserMap = new HashMap<>();
    public RemoteCefClient(String uuid, RemoteCefApp remoteCefApp){
        this.uuid=uuid;
        this.app=remoteCefApp;
        app.remoteCefClientMap.put(uuid,this);
    }
    public RemoteCefBrowser createBrowser(String url){
        var bb = app.client.alloc().directBuffer();
        BufUtil.writeString(app.remoteCommands.CREATE_BROWSER,bb);
        BufUtil.writeString(uuid,bb);
        BufUtil.writeString(url,bb);
        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },app);
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
        String s = BufUtil.readString(cr.get().result);
        RemoteCefBrowser remoteCefBrowser = new RemoteCefBrowser(s, this);
        browserMap.put(s,remoteCefBrowser);
        return remoteCefBrowser;
    }
    public void destroy(){
        app.remoteCefClientMap.remove(uuid);
        var bb = app.client.alloc().directBuffer();
        BufUtil.writeString(app.remoteCommands.CLIENT_DSETROY,bb);
        BufUtil.writeString(uuid,bb);
        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, app.client, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },app);
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
}
