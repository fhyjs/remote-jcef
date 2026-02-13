package org.eu.hanana.reimu.lib.rjcef.client;

import me.friwi.jcefmaven.CefAppBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cef.CefSettings;
import org.eu.hanana.reimu.lib.rjcef.common.CefUtil;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class CelInstaller {
    private static final Logger log = LogManager.getLogger(CelInstaller.class);
    private static int DEBUG_PORT;
    public static File CEF_INSTALLATION_DIR = new File("./jcef");
    public static CefAppBuilder getBuilder(){
        var b = new CefAppBuilder();
        b.setInstallDir(CEF_INSTALLATION_DIR);
        try {
            b.getCefSettings().cache_path = Files.createTempDirectory("cef_cache").toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        b.setMirrors(List.of(
                "https://maven.aliyun.com/repository/public/me/friwi/jcef-natives-{platform}/{tag}/jcef-natives-{platform}-{tag}.jar",
                "https://repo.maven.apache.org/maven2/me/friwi/jcef-natives-{platform}/{tag}/jcef-natives-{platform}-{tag}.jar",
                "https://github.com/jcefmaven/jcefmaven/releases/download/{mvn_version}/jcef-natives-{platform}-{tag}.jar"
        ));
        b.addJcefArgs("--remote-allow-origins=*");
        b.getCefSettings().remote_debugging_port= CefUtil.getRandomPort();
        b.getCefSettings().log_severity= CefSettings.LogSeverity.LOGSEVERITY_VERBOSE;
        //b.getCefSettings().log_severity
        DEBUG_PORT=b.getCefSettings().remote_debugging_port;
        log.info("JCEF DEBUG PORT : {}",b.getCefSettings().remote_debugging_port);
        return b;
    }

    public static int getDebugPort() {
        return DEBUG_PORT;
    }
}

