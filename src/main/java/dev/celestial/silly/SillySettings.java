package dev.celestial.silly;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.figuramc.figura.config.ConfigType;

public class SillySettings {
    public static void classload(){}
    private static ConfigType.Category category(String name) {
        return new ConfigType.Category(name) {{
            this.name = Component.literal("\uE000")
                    .withStyle(Style.EMPTY.withFont(SillyUtil.location("figura", "emoji_silly"))
                    .withColor(SillyPlugin.COLOR.getRGB()))
                    .append(Component.literal(" ").withStyle(Style.EMPTY.withFont(Style.DEFAULT_FONT)))
                    .append(this.name.copy().withStyle(Style.EMPTY.withFont(Style.DEFAULT_FONT)));
        }};
    }
    public static ConfigType.Category MAIN_CATEGORY = category("sillyplugin");

    public static SillySetting SILLY_ON_HOST = new SillyBooleanSetting("silly_on_host", true);
    public static SillySetting SILLY_ON_NONHOST = new SillyBooleanSetting("silly_on_nonhost", true);
    public static SillySetting CHEATS = new SillyBooleanSetting("cheats", true);
    public static SillySetting DEV_LOGS;

    static {
        if (SillyUtil.DEV_MODE)
            DEV_LOGS = new SillyBooleanSetting("dev_logs", true);
    }

    /**
     * Keybind for the wardrobe permissions tab "reload all" button.
     * Figura ships RELOAD_BUTTON, but that only reloads your own avatar - this drops every
     * loaded avatar so they are all fetched again. Unbound by default.
     */
    public static ConfigType.KeybindConfig RELOAD_ALL_BUTTON =
            new ConfigType.KeybindConfig("reload_all_button", MAIN_CATEGORY, "key.keyboard.unknown");

    public static ConfigType.Category TWEAKS_CATEGORY = category("sillyplugin_tweaks");
    public static SillySetting TOAST_ON_STEREO = new SillyBooleanSetting("toast_on_stereo", false, TWEAKS_CATEGORY);
    public static SillySetting LENIENT_MODE = new SillyBooleanSetting("lenient_mode", false, TWEAKS_CATEGORY);
    public static SillySetting LOOSEN_NB4U = new SillyBooleanSetting("loosen_nb4u", false, TWEAKS_CATEGORY);


    public static class SillySetting {
        public String name;

        public SillySetting(String name) {
            this.name = name;
        }

        public Boolean getBool() {
            throw new RuntimeException("Cannot get a setting of type " + this.getClass().getSimpleName() + " as a boolean!");
        }
        public void setBool(Boolean val) {
            throw new RuntimeException("Cannot set a setting of type " + this.getClass().getSimpleName() + " as a boolean!");
        }
        public String getString() {
            throw new RuntimeException("Cannot get a setting of type " + this.getClass().getSimpleName() + " as a string!");
        }
        public void setString(String val) {
            throw new RuntimeException("Cannot set a setting of type " + this.getClass().getSimpleName() + " as a string!");
        }
    }

    public static class SillyBooleanSetting extends SillySetting {
        private final ConfigType.BoolConfig figconf;
        public SillyBooleanSetting(String name, Boolean def, ConfigType.Category category) {
            super(name);
            figconf = new ConfigType.BoolConfig(name, category, def);
        }

        public SillyBooleanSetting(String name, Boolean def) {
            this(name, def, MAIN_CATEGORY);
        }

        @Override
        public Boolean getBool() {
            return figconf.value;
        }

        @Override
        public void setBool(Boolean val) {
            figconf.setValue(String.valueOf(val));
        }
    }

}
