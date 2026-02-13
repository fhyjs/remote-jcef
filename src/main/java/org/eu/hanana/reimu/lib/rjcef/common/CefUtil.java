package org.eu.hanana.reimu.lib.rjcef.common;

import org.cef.handler.*;

import java.io.IOException;
import java.net.ServerSocket;

public class CefUtil {
    public static int getRandomPort() {
        try (ServerSocket socket = new ServerSocket(0)) { // 0 表示随机可用端口
            return socket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
            return 9222; // 失败回退
        }

    }
}
