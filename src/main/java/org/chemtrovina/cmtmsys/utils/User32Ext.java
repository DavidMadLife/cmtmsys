package org.chemtrovina.cmtmsys.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;

public interface User32Ext extends Library {
    User32Ext INSTANCE = Native.load("user32", User32Ext.class);

    int MessageBoxW(HWND hWnd, String text, String caption, int type);
}
