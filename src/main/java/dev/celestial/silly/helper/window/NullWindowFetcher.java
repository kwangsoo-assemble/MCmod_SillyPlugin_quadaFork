package dev.celestial.silly.helper.window;

public class NullWindowFetcher implements ActiveWindowFetcher {
    @Override
    public WindowInfo getWindow() {
        return ActiveWindowFetcher.NULL;
    }
}
