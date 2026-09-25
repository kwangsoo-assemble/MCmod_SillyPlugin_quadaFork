package dev.celestial.silly.helper.window;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.celestial.silly.SillyUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public abstract class JsonWindowFetcher implements ActiveWindowFetcher {
    public abstract @Nullable String getJsonString();
    public @Nullable JsonObject getJson() {
        try {
            var output = getJsonString();
            if (output == null) return null;
            var reader = JsonParser.parseString(output);
            if (reader.isJsonObject()) {
                return reader.getAsJsonObject();
            }
            return null;
        } catch (Exception e) {
            SillyUtil.Devlog("JsonWindowFetcher: Failed to get active window.\n{}", e.getCause());
            return null;
        }
    }


    @Override
    public WindowInfo getWindow() {
        JsonObject obj = getJson();
        if (obj != null) {
            var title = obj.get("title").getAsString();
            var pid = obj.get("pid").getAsLong();
            String executable = ActiveWindowFetcher.NULL.executable();
            String path = ActiveWindowFetcher.NULL.path();

            var cmd = ProcessHandle.of(pid).flatMap(handle -> handle.info().command());
            if (cmd.isPresent()) {
                var file = new File(cmd.get());
                executable = file.getName();
                path = file.getAbsolutePath();
            }

            return new WindowInfo(title, executable, path);
        } else {
            return ActiveWindowFetcher.NULL;
        }
    }
}
