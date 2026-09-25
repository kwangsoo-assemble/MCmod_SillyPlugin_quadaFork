package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.SillySettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.font.EmojiContainer;
import org.figuramc.figura.font.EmojiUnicodeLookup;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = EmojiContainer.class, remap = false)
public abstract class EmojiContainerMixin {
    @Unique
    private static final Map<String, String[]> silly$whitelist = new HashMap<>() {{
        put("symbol", new String[] {"x", "volume_3"});
    }};

    @Shadow
    @Final
    private String blacklist;

    @Shadow
    @Final
    public String name;

    @Shadow
    @Final
    private EmojiUnicodeLookup lookup;

    @Shadow
    @Final
    private ResourceLocation font;

    @Unique
    private String silly$blacklistWhitelist(String blacklist) {
        var whitelist = silly$whitelist.get(this.name);
        if (whitelist != null) {
            for (var emoji : whitelist) {
                var unicode = this.lookup.getUnicode(emoji);
                if (unicode != null)
                    blacklist = blacklist.replace(unicode, "");
            }
        }
        return blacklist;
    }

    @WrapMethod(method = "blacklist")
    public Component silly$yesSymbols4U(Component text, Operation<Component> original) {
        if (!SillySettings.LOOSEN_NB4U.getBool()) return original.call(text);
        if (blacklist.isBlank())
            return text;
        return TextUtils.replaceInText(text, "[" + silly$blacklistWhitelist(blacklist) + "]", TextUtils.UNKNOWN, (s, style) -> style.getFont().equals(this.font), Integer.MAX_VALUE);

    }
}
