package dev.celestial.silly.helper.window;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class NiriWindowFetcher extends JsonWindowFetcher {
    @Override
    public @Nullable String getJsonString() {
        ProcessBuilder builder = new ProcessBuilder("/usr/bin/niri", "msg", "-j", "focused-window");
        try {
            var proc = builder.start();
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            var exit = proc.waitFor();
            if (exit != 0) return null;

            return output;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}
