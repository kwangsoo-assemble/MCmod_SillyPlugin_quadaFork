package dev.celestial.silly.lua.compat;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import dev.celestial.silly.plugin.SillyVoicechatPlugin;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;

import java.util.UUID;

import static de.maxhenkel.voicechat.voice.common.AudioUtils.sampleDb;

@LuaWhitelist
public class VoicechatCompatAPI extends BaseCompatAPI {
    public VoicechatCompatAPI(FiguraLuaRuntime runtime) {
        super(runtime);
    }
    public ClientVoicechat CLIENT = ClientManager.getClient();
    public ClientPlayerStateManager STATE = ClientManager.getPlayerStateManager();

    @LuaWhitelist
    public Boolean isMuted() {
        if (avatar.isHost)
            return STATE.isMuted();
        return false;
    }

    @LuaWhitelist
    public void setMuted(@LuaNotNil Boolean state) {
        if (!avatar.isHost) return;
        STATE.setMuted(state);
    }

    @LuaWhitelist
    public void setDeafened(@LuaNotNil Boolean state) {
        if (!avatar.isHost) return;
        STATE.setDisabled(state);
    }

    @LuaWhitelist
    public Boolean isDeafened(String uuid) {
        uuid = uuid != null ? uuid : avatar.owner.toString();
        UUID targetUUID = UUID.fromString(uuid);
        STATE.isPlayerDisabled(targetUUID);
        return false;
    }

    @LuaWhitelist
    public Boolean isDisconnected(String uuid) {
        uuid = uuid != null ? uuid : avatar.owner.toString();
        UUID targetUUID = UUID.fromString(uuid);
        return STATE.isPlayerDisconnected(targetUUID);
    }

    @LuaWhitelist
    public Boolean isTalking(String uuid) {
        uuid = uuid != null ? uuid : avatar.owner.toString();
        UUID targetUUID = UUID.fromString(uuid);
        return CLIENT.getTalkCache().isTalking(targetUUID);
    }

    private Double transformDb(Double value) {
        return (value + 127.0f) / 127.0f * 100.0f;
    }

    private double getAverageAudioLevel(short[] pcm) {
        long value = 0;

        for(short sample : pcm) {
            value += Math.abs(sample);
        }

        return sampleDb((short)(value / pcm.length));
    }

    @LuaWhitelist
    public Double getVolume(String uuid) {
        uuid = uuid != null ? uuid : avatar.owner.toString();
        UUID targetUUID = UUID.fromString(uuid);

        SillyVoicechatPlugin.SillyVoiceData data = SillyVoicechatPlugin.AUDIO.getOrDefault(targetUUID, SillyVoicechatPlugin.emptyData(targetUUID));
        var pcm = data.pcm();
        if (pcm.length == 0) return 0d;
        return transformDb(
                (System.currentTimeMillis() - data.timestamp()) >= 250L
                        ? (double)-127.0F
                        : getAverageAudioLevel(pcm)
        );
    }
}
