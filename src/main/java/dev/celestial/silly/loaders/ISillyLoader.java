package dev.celestial.silly.loaders;

public interface ISillyLoader {
    public boolean isModLoaded(String mod_id);

    public String getModVersion(String mod_id);
}
