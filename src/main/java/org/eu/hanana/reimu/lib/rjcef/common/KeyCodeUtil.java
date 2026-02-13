package org.eu.hanana.reimu.lib.rjcef.common;

import java.awt.event.KeyEvent;

public class KeyCodeUtil {

    /**
     * 将 AWT KeyEvent keyCode 转换为字符
     * @param keyCode AWT KeyEvent.VK_*
     * @param shift 是否按下 Shift
     * @return 对应字符，如果无对应字符则返回 KeyEvent.CHAR_UNDEFINED
     */
    public static char keyCodeToChar(int keyCode, boolean shift) {
        // 字母
        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            char c = (char) ('a' + (keyCode - KeyEvent.VK_A));
            return shift ? Character.toUpperCase(c) : c;
        }
        // 数字键 0-9
        if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
            char c = (char) ('0' + (keyCode - KeyEvent.VK_0));
            if (shift) {
                switch (c) {
                    case '1': return '!';
                    case '2': return '@';
                    case '3': return '#';
                    case '4': return '$';
                    case '5': return '%';
                    case '6': return '^';
                    case '7': return '&';
                    case '8': return '*';
                    case '9': return '(';
                    case '0': return ')';
                }
            }
            return c;
        }
        // 小键盘数字
        if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9) {
            return (char) ('0' + (keyCode - KeyEvent.VK_NUMPAD0));
        }
        // 符号键
        switch (keyCode) {
            case KeyEvent.VK_MINUS: return shift ? '_' : '-';
            case KeyEvent.VK_EQUALS: return shift ? '+' : '=';
            case KeyEvent.VK_BACK_SLASH: return shift ? '|' : '\\';
            case KeyEvent.VK_SLASH: return shift ? '?' : '/';
            case KeyEvent.VK_SEMICOLON: return shift ? ':' : ';';
            case KeyEvent.VK_COMMA: return shift ? '<' : ',';
            case KeyEvent.VK_PERIOD: return shift ? '>' : '.';
            case KeyEvent.VK_QUOTE: return shift ? '"' : '\'';
            case KeyEvent.VK_OPEN_BRACKET: return shift ? '{' : '[';
            case KeyEvent.VK_CLOSE_BRACKET: return shift ? '}' : ']';
            case KeyEvent.VK_BACK_SPACE: return '\b';
            case KeyEvent.VK_ENTER: return '\n';
            case KeyEvent.VK_TAB: return '\t';
            case KeyEvent.VK_SPACE: return ' ';
        }
        // 其他不可打印键
        return KeyEvent.CHAR_UNDEFINED;
    }

    // 测试
    public static void main(String[] args) {
        int[] testKeys = {
            KeyEvent.VK_A, KeyEvent.VK_Z, KeyEvent.VK_1, KeyEvent.VK_0,
            KeyEvent.VK_MINUS, KeyEvent.VK_EQUALS, KeyEvent.VK_SLASH,
            KeyEvent.VK_BACK_SPACE, KeyEvent.VK_ENTER, KeyEvent.VK_SPACE
        };

        for (int key : testKeys) {
            System.out.println("KeyCode " + key + " → char: " + keyCodeToChar(key, false)
                    + " | shift: " + keyCodeToChar(key, true));
        }
    }
}
