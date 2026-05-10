package com.nuclearcraft.core;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeTab {
    public static final CreativeModeTab NUCLEARCRAFT_TAB = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.nuclearcraft"))
            .icon(() -> new ItemStack(RegistryBus.EXAMPLE_BLOCK_ITEM.get()))
            .build();
}
