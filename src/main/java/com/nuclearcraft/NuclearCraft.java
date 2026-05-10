package com.nuclearcraft;

import com.nuclearcraft.core.RegistryBus;
import com.nuclearcraft.core.ModSetup;
import com.nuclearcraft.core.Config;
import com.nuclearcraft.core.ClientConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NuclearCraft.MODID)
public class NuclearCraft {
    public static final String MODID = "nuclearcraft";
    public static final Logger LOGGER = LogManager.getLogger();

    public NuclearCraft() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        RegistryBus.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModSetup.init();
    }
}
