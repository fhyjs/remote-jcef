package org.eu.hanana.reimu.lib.rjcef.client;

import io.netty.buffer.ByteBuf;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import org.cef.CefApp;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCommandLine;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;
import org.eu.hanana.reimu.lib.rjcef.common.BufUtil;
import org.eu.hanana.reimu.lib.rjcef.common.CallbackResult;

import java.util.concurrent.atomic.AtomicReference;

public class NetCefAppHandler extends MavenCefAppHandlerAdapter {
    private final ClientMain client;

    public NetCefAppHandler(ClientMain clientMain){
        this.client=clientMain;
    }

    @Override
    public boolean onAlreadyRunningAppRelaunch(CefCommandLine command_line, String current_directory) {
        return super.onAlreadyRunningAppRelaunch(command_line, current_directory);
    }

    @Override
    public boolean onBeforeTerminate() {
        return super.onBeforeTerminate();
    }

    @Override
    public void onContextInitialized() {
        super.onContextInitialized();
        var bb = client.cnc.channel.alloc().directBuffer();
        BufUtil.writeString(client.remoteCommands.APPHANDLER_onContextInitialized,bb);
        client.cnc.channel.writeAndFlush(bb);
        client.netCefSchemeHandlerFactoryMap.forEach((s, customSchemeCfg) -> {
            System.out.println("registerSchemeHandlerFactory "+s);
            client.cefApp.registerSchemeHandlerFactory(s, "", customSchemeCfg);
        });
    }

    @Override
    public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
        super.onRegisterCustomSchemes(registrar);
        var bb = client.cnc.channel.alloc().directBuffer();
        BufUtil.writeString(client.remoteCommands.APPHANDLER_onRegisterCustomSchemes,bb);
        client.cnc.channel.writeAndFlush(bb);

        client.customSchemeCfgMap.forEach((s, customSchemeCfg) -> {
            System.out.println("addCustomScheme "+s);
            registrar.addCustomScheme(s,customSchemeCfg.isStandard,customSchemeCfg.isLocal,customSchemeCfg.isDisplayIsolated,customSchemeCfg.isSecure,customSchemeCfg.isCorsEnabled,customSchemeCfg.isCspBypassing,customSchemeCfg.isFetchEnabled);
        });
    }

    @Override
    public void onScheduleMessagePumpWork(long delay_ms) {
        super.onScheduleMessagePumpWork(delay_ms);
    }

    @Override
    public void stateHasChanged(CefApp.CefAppState state) {
        super.stateHasChanged(state);
        var bb = client.cnc.channel.alloc().directBuffer();
        BufUtil.writeString(client.remoteCommands.APPHANDLER_stateHasChanged,bb);

        BufUtil.writeString(state.name(),bb);

        client.cnc.channel.writeAndFlush(bb);
    }

}
