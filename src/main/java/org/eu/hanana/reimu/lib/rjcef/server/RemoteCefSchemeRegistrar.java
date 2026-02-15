package org.eu.hanana.reimu.lib.rjcef.server;

import lombok.RequiredArgsConstructor;
import org.cef.callback.CefSchemeRegistrar;
import org.eu.hanana.reimu.lib.rjcef.common.BufUtil;
import org.eu.hanana.reimu.lib.rjcef.common.CallbackResult;

import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class RemoteCefSchemeRegistrar implements CefSchemeRegistrar {
    protected final RemoteCefApp remoteCefApp;
    @Override
    public boolean addCustomScheme(String schemeName, boolean isStandard, boolean isLocal, boolean isDisplayIsolated, boolean isSecure, boolean isCorsEnabled, boolean isCspBypassing, boolean isFetchEnabled) {
        var bb = remoteCefApp.serv.serverChannel.alloc().directBuffer();
        BufUtil.writeString(remoteCefApp.remoteCommands.APP_addCustomScheme,bb);

        BufUtil.writeString(schemeName,bb);
        bb.writeBoolean(isStandard);
        bb.writeBoolean(isLocal);
        bb.writeBoolean(isDisplayIsolated);
        bb.writeBoolean(isSecure);
        bb.writeBoolean(isCorsEnabled);
        bb.writeBoolean(isCspBypassing);
        bb.writeBoolean(isFetchEnabled);

        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb,remoteCefApp.client,tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },remoteCefApp);
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
        return cr.get().result.readBoolean();
    }

    public boolean addRequestProcessor(String schemeName, RemoteCefSchemeHandlerFactory remoteCefSchemeHandlerFactory) {
        var bb = remoteCefApp.serv.serverChannel.alloc().directBuffer();
        BufUtil.writeString(remoteCefApp.remoteCommands.APP_addRequestProcessor,bb);

        BufUtil.writeString(schemeName,bb);
        BufUtil.writeString(remoteCefSchemeHandlerFactory.uuid,bb);

        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb,remoteCefApp.client,tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },remoteCefApp);
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
        remoteCefApp.remoteCefSchemeHandlerFactoryMap.put(remoteCefSchemeHandlerFactory.uuid,remoteCefSchemeHandlerFactory);
        return cr.get().result.readBoolean();
    }
}
