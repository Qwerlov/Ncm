package com.nuclearcraft.core;

import com.nuclearcraft.NuclearCraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = NuclearCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SERVER = new Server(builder);
        SERVER_SPEC = builder.build();
    }

    public static class Server {
        public final ForgeConfigSpec.IntValue radiationCellSize;
        public final ForgeConfigSpec.DoubleValue meltdownTemperature;

        Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Radiation settings").push("radiation");
            radiationCellSize = builder
                    .comment("Size of a radiation cell in blocks (default: 4)")
                    .defineInRange("cell_size", 4, 1, 16);
            builder.pop();

            builder.comment("Reactor settings").push("reactor");
            meltdownTemperature = builder
                    .comment("Temperature in Celsius at which meltdown starts")
                    .defineInRange("meltdown_temp", 2800.0, 500.0, 5000.0);
            builder.pop();
        }
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        NuclearCraft.LOGGER.info("NuclearCraft config loaded.");
    }
}
