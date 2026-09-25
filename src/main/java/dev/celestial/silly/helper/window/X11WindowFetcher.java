package dev.celestial.silly.helper.window;

import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import dev.celestial.silly.SillyUtil;

import java.io.File;

public class X11WindowFetcher implements ActiveWindowFetcher {
    private static final X11 x11 = X11.INSTANCE;

    @Override
    public WindowInfo getWindow() {
        var display = x11.XOpenDisplay(null);
        var root = x11.XDefaultRootWindow(display);
        var property = x11.XInternAtom(display, "_NET_ACTIVE_WINDOW", false);

        var typeReturn = new X11.AtomByReference();
        var formatReturn = new IntByReference();
        var nitemsReturn = new NativeLongByReference();
        var bytesLeft = new NativeLongByReference();
        var data = new PointerByReference();

        var res = x11.XGetWindowProperty(
                display,
                root,
                property,
                new NativeLong(0),
                new NativeLong(1),
                false,
                X11.XA_WINDOW,
                typeReturn,
                formatReturn,
                nitemsReturn,
                bytesLeft,
                data
        );
        SillyUtil.Devlog("X11WinFetch: XGetWindowProperty:1 → {} (THE NUMBERS MASON, WHAT DO THEY MEAN???)", res);

        var activeWindowId = data.getValue().getNativeLong(0).longValue();
        x11.XFree(data.getValue());
        SillyUtil.Devlog("X11WinFetch: Window ID: {}", activeWindowId);
        if (activeWindowId == 0) {
            x11.XCloseDisplay(display);
            // invalid window. we'd crash otherwise.
            // gotta love a X Error of failed request:  BadWindow (invalid Window parameter)
            //  Major opcode of failed request:  20 (X_GetProperty)
            //  Resource id in failed request:  0x0
            //  Serial number of failed request:  9
            //  Current serial number in output stream:  9
            return ActiveWindowFetcher.NULL;
        }

        X11.Window win = new X11.Window(activeWindowId);
        var tprop = new X11.XTextProperty();
        x11.XGetWMName(display, win, tprop);
        var title = tprop.value;

        var pidAtom = x11.XInternAtom(display, "_NET_WM_PID", false);
        Long pid;

        var res2 = x11.XGetWindowProperty(
            display,
            win,
            pidAtom,
            new NativeLong(0),
            new NativeLong(1),
            false,
            X11.XA_CARDINAL,
            typeReturn,
            formatReturn,
            nitemsReturn,
            bytesLeft,
            data
        );

        SillyUtil.Devlog("X11WinFetch: XGetWindowProperty:2 → {} (THE NUMBERS MASON, WHAT DO THEY MEAN???)", res2);


        if (data.getValue() != null) {
            pid = data.getValue().getNativeLong(0).longValue();
        } else {
            SillyUtil.Devlog("X11WinFetch: Window did not set its _NET_WM_PID prop (evil)");
            pid = null;
        }
        x11.XFree(data.getValue());
        x11.XCloseDisplay(display);

        String executable = ActiveWindowFetcher.NULL.executable();
        String path = ActiveWindowFetcher.NULL.path();
        if (pid != null) {
            var cmd = ProcessHandle.of(pid).flatMap(handle -> handle.info().command());

            if (cmd.isPresent()) {
                var file = new File(cmd.get());
                executable = file.getName();
                path = file.getAbsolutePath();
            };
        }

        return new WindowInfo(title, executable, path);
    }
}
