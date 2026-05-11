package com.kverlov.mcn.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.kverlov.mcn.NcmMod;

@Mod.EventBusSubscriber(modid = NcmMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
        public final ForgeConfigSpec.DoubleValue minDoseForEffect;
        public final ForgeConfigSpec.DoubleValue doseThresholdNausea;
        public final ForgeConfigSpec.DoubleValue doseThresholdWeakness;
        public final ForgeConfigSpec.DoubleValue doseThresholdDamage;
        public final ForgeConfigSpec.DoubleValue doseThresholdBlindness;
        public final ForgeConfigSpec.DoubleValue doseThresholdDeath;
        public final ForgeConfigSpec.DoubleValue airAttenuation;
        public final ForgeConfigSpec.BooleanValue enableRaytracing;
        public final ForgeConfigSpec.DoubleValue defaultBlockAttenuation;
        // Параметры заражения блоков
        public final ForgeConfigSpec.DoubleValue blockSourceThreshold;
        public final ForgeConfigSpec.DoubleValue blockSourceActivity;

        Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Radiation settings").push("radiation");
            radiationCellSize = builder
                    .comment("Size of a radiation cell in blocks (default: 4)")
                    .defineInRange("cell_size", 4, 1, 16);
            minDoseForEffect = builder
                    .comment("Minimum dose rate (mkSv/h) to be considered (default: 1.0)")
                    .defineInRange("min_dose_for_effect", 1.0, 0.0, 100.0);
            doseThresholdNausea = builder
                    .comment("Accumulated dose (mSv) to start nausea (default: 100)")
                    .defineInRange("nausea_threshold", 100.0, 0.0, 10000.0);
            doseThresholdWeakness = builder
                    .comment("Accumulated dose (mSv) to start weakness (default: 200)")
                    .defineInRange("weakness_threshold", 200.0, 0.0, 10000.0);
            doseThresholdDamage = builder
                    .comment("Accumulated dose (mSv) to start taking damage (default: 500)")
                    .defineInRange("damage_threshold", 500.0, 0.0, 10000.0);
            doseThresholdBlindness = builder
                    .comment("Accumulated dose (mSv) to start blindness (default: 1000)")
                    .defineInRange("blindness_threshold", 1000.0, 0.0, 10000.0);
            doseThresholdDeath = builder
                    .comment("Accumulated dose (mSv) to instantly die (default: 3000)")
                    .defineInRange("death_threshold", 3000.0, 0.0, 100000.0);
            airAttenuation = builder
                    .comment("Air attenuation factor per block (default: 0.98, 1.0 = no loss)")
                    .defineInRange("air_attenuation", 0.98, 0.0, 1.0);
            enableRaytracing = builder
                    .comment("Enable block raytracing for radiation (default: true)")
                    .define("enable_raytracing", true);
            defaultBlockAttenuation = builder
                    .comment("Default attenuation for blocks not covered by tags (default: 0.8)")
                    .defineInRange("default_block_attenuation", 0.8, 0.0, 1.0);
            builder.comment("Block contamination settings").push("contamination");
            blockSourceThreshold = builder
                    .comment("Accumulated dose (mSv) after which a block becomes a secondary source (default: 1000)")
                    .defineInRange("block_source_threshold", 1000.0, 0.0, 1000000.0);
            blockSourceActivity = builder
                    .comment("Activity (Bq) of a contaminated block when it becomes a source (default: 1.0, so it won't chain)")
                    .defineInRange("block_source_activity", 1.0, 0.0, 1000000.0);
            builder.pop();
        }
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {}
}
