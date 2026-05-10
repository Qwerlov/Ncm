package com.nuclearcraft.core;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue showDosimeterHud;
        public final ForgeConfigSpec.DoubleValue cherenkovBrightness;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Visual settings").push("visual");
            showDosimeterHud = builder
                    .comment("Show dosimeter HUD when holding a dosimeter")
                    .define("show_dosimeter_hud", true);
            cherenkovBrightness = builder
                    .comment("Brightness of Cherenkov radiation glow (0.0 to 1.0)")
                    .defineInRange("cherenkov_brightness", 0.7, 0.0, 1.0);
            builder.pop();
        }
    }
}
