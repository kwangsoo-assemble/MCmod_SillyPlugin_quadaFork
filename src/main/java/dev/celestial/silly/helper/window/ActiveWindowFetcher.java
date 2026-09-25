package dev.celestial.silly.helper.window;

import com.sun.jna.Platform;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;

import java.util.HashMap;
import java.util.Map;

public interface ActiveWindowFetcher {
    WindowInfo NULL = new WindowInfo(null, null, null);

    Map<String, Class<? extends ActiveWindowFetcher>> FETCHERS = new HashMap<>() {{
        put("hyprland", HyprlandWindowFetcher.class);
        put("hypr", HyprlandWindowFetcher.class);
        put("niri", NiriWindowFetcher.class);
        put("null", NullWindowFetcher.class);
        put("win32", Win32WindowFetcher.class);
        put("windows", Win32WindowFetcher.class);
        put("x11", X11WindowFetcher.class);
    }};

    public WindowInfo getWindow();

    public static ActiveWindowFetcher findFetcher() {
        if (Platform.isWindows())
            return new Win32WindowFetcher();
        if (Platform.isLinux()) {
            // either x11, wayland, or secret third thing.
            // wayland has about a million permutations,
            // and i cannot hope to cover them all.
            // i'll just be focusing on what i use, and
            // contributors can PR what works for them.
            // i'll still implement for X11 for completeness’s
            // sake.
            var type = System.getenv("XDG_SESSION_TYPE");
            if ("wayland".equals(type)) {
                // yeowch this is gonna be fun.
                if ("niri".equals(System.getenv("XDG_CURRENT_DESKTOP"))) {
                    return new NiriWindowFetcher();
                }
                if ("Hyprland".equals(System.getenv("XDG_CURRENT_DESKTOP"))) {
                    return new HyprlandWindowFetcher();
                }
            } else if ("x11".equals(type)) {
                return new X11WindowFetcher();
            }
        }

        return new NullWindowFetcher();
    }

    public record WindowInfo(@Nullable String title, @Nullable String executable, @Nullable String path) {
        public LuaTable toTable() {
            var table = new LuaTable();
            if (title != null)
                table.set("title", title);
            if (executable != null)
                table.set("executable", executable);
            if (path != null)
                table.set("path", path);
            return table;
        }
    }

}
