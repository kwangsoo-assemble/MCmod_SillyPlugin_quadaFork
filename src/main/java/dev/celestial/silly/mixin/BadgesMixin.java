package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.celestial.silly.SillySettings;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Badges.class, remap = false)
public class BadgesMixin {
    @WrapMethod(method = "noBadges4U")
    private static Component silly$fix_the_evil(Component text, Operation<Component> original) {
        if (!SillySettings.LOOSEN_NB4U.getBool()) return original.call(text);
        return TextUtils.replaceInText(text, "[★☆❤文✒\uD83D\uDDFF]", TextUtils.UNKNOWN, (s, style) -> style.getFont().equals(Badges.FONT) || style.getFont().equals(UIHelper.UI_FONT), Integer.MAX_VALUE);
    }
}
