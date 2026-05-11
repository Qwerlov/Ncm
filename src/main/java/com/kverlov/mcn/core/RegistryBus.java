package com.kverlov.mcn.core;

import com.kverlov.mcn.NcmMod;
import com.kverlov.mcn.blocks.UraniumOreBlock;
import com.kverlov.mcn.blocks.entity.UraniumOreBlockEntity;
import com.kverlov.mcn.item.ContaminatedItem;
import com.kverlov.mcn.item.DosimeterItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RegistryBus {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, NcmMod.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NcmMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, NcmMod.MODID);

    // Урановая руда
    public static final RegistryObject<Block> URANIUM_ORE = BLOCKS.register("uranium_ore",
            () -> new UraniumOreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Item> URANIUM_ORE_ITEM = ITEMS.register("uranium_ore",
            () -> new BlockItem(URANIUM_ORE.get(), new Item.Properties()));

    // BlockEntity для урановой руды
    public static final RegistryObject<BlockEntityType<UraniumOreBlockEntity>> URANIUM_ORE_BE =
            BLOCK_ENTITIES.register("uranium_ore_be",
                    () -> BlockEntityType.Builder.of(UraniumOreBlockEntity::new, URANIUM_ORE.get()).build(null));

    // Дозиметр
    public static final RegistryObject<Item> DOSIMETER = ITEMS.register("dosimeter",
            () -> new DosimeterItem(new Item.Properties().stacksTo(1)));

    // Заражённый предмет
    public static final RegistryObject<Item> CONTAMINATED_ITEM = ITEMS.register("contaminated_item",
            () -> new ContaminatedItem(new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }
}
