package com.kverlov.mcn.radiation;

import com.kverlov.mcn.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RadiationTags {
    public static final TagKey<Block> ATTENUATION_LEAD = create("attenuation_lead");
    public static final TagKey<Block> ATTENUATION_METAL = create("attenuation_metal");
    public static final TagKey<Block> ATTENUATION_WATER = create("attenuation_water");
    public static final TagKey<Block> ATTENUATION_STONE = create("attenuation_stone");
    public static final TagKey<Block> ATTENUATION_DIRT = create("attenuation_dirt");
    public static final TagKey<Block> ATTENUATION_AIR = create("attenuation_air");

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation("ncm", name));
    }

    public static double getAttenuation(BlockState state) {
        if (state.is(ATTENUATION_LEAD)) return 0.05;
        if (state.is(ATTENUATION_METAL)) return 0.30;
        if (state.is(ATTENUATION_WATER)) return 0.50;
        if (state.is(ATTENUATION_STONE)) return 0.60;
        if (state.is(ATTENUATION_DIRT)) return 0.80;
        if (state.is(ATTENUATION_AIR)) return 0.99;
        // Блок не в тегах – базовый коэффициент из конфига
        return Config.SERVER.defaultBlockAttenuation.get();
    }
}
