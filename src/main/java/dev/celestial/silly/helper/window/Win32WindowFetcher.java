package dev.celestial.silly.helper.window;


import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.ptr.IntByReference;

import java.io.File;

public class Win32WindowFetcher implements ActiveWindowFetcher {
    private static final User32 user32 = User32.INSTANCE;
    private static final Kernel32 kernel32 = Kernel32.INSTANCE;
    private static final Psapi psapi = Psapi.INSTANCE;

    @Override
    public WindowInfo getWindow() {
        var hwnd = user32.GetForegroundWindow();
        if (hwnd == null)
            return ActiveWindowFetcher.NULL;

        var pidRef = new IntByReference();
        user32.GetWindowThreadProcessId(hwnd, pidRef);
        var pid = pidRef.getValue();

        int PROCESS_QUERY_INFORMATION = 0x0400;
        int PROCESS_VM_READ = 0x0010;
        var hProcess = kernel32.OpenProcess(PROCESS_QUERY_INFORMATION + PROCESS_VM_READ, false, pid);

        var bufSize = 128;
        var buffer = new char[bufSize];

        var length = psapi.GetModuleFileNameExW(hProcess, null, buffer, bufSize);
        kernel32.CloseHandle(hProcess);

        var s = new String(buffer, 0, length);

        buffer = new char[bufSize];
        length = user32.GetWindowText(hwnd, buffer, 128);
        var title = new String(buffer, 0, length);
        return new WindowInfo(title, new File(s).getName(), s);
    }
}
