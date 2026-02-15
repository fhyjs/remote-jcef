package org.eu.hanana.reimu.lib.rjcef.server;

import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import org.cef.CefApp;
import org.cef.callback.CefCommandLine;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefAppHandler;

public class RemoteCefAppHandlerAdapter implements CefAppHandler {
    @Override
    public void onBeforeCommandLineProcessing(String process_type, CefCommandLine command_line) {

    }

    @Override
    public boolean onBeforeTerminate() {
        return false;
    }

    @Override
    public void stateHasChanged(CefApp.CefAppState state) {

    }

    @Override
    public final void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
        this.onRegisterCustomSchemes(((RemoteCefSchemeRegistrar) registrar));
    }
    public void onRegisterCustomSchemes(RemoteCefSchemeRegistrar registrar) {

    }
    @Override
    public void onContextInitialized() {

    }

    @Override
    public void onScheduleMessagePumpWork(long delay_ms) {

    }

    @Override
    public boolean onAlreadyRunningAppRelaunch(CefCommandLine command_line, String current_directory) {
        return false;
    }
}
