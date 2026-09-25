package dev.celestial.silly.lua.compat;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.LuaWhitelist;

@LuaWhitelist
public class BaseCompatAPI {
    public final Avatar avatar;
    public final FiguraLuaRuntime runtime;
    public BaseCompatAPI(FiguraLuaRuntime runtime) {
        this.runtime = runtime;
        this.avatar = runtime.owner;
    }
}
