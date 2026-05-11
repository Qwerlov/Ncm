package com.kverlov.mcn;

import com.kverlov.mcn.core.RegistryBus;
import com.kverlov.mcn.core.ModSetup;
import com.kverlov.mcn.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NcmMod.MODID)
public class NcmMod {
    public static final String MODID = "ncm";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> NCM_TAB = CREATIVE_MODE_TABS.register("ncm_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ncm"))
                    .icon(() -> new ItemStack(RegistryBus.URANIUM_ORE_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(RegistryBus.URANIUM_ORE_ITEM.get());
                        output.accept(RegistryBus.DOSIMETER.get());
                    })
                    .build());

    public NcmMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        RegistryBus.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ModSetup.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
    }
}
