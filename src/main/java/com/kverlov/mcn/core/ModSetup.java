package com.kverlov.mcn.core;

import com.kverlov.mcn.NcmMod;
import com.kverlov.mcn.command.NcmCommand;
import com.kverlov.mcn.item.ContaminatedItem;
import com.kverlov.mcn.item.DosimeterItem;
import com.kverlov.mcn.radiation.PlayerRadiationData;
import com.kverlov.mcn.radiation.PlayerRadiationDataProvider;
import com.kverlov.mcn.radiation.RadiationManager;
import com.kverlov.mcn.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod.EventBusSubscriber(modid = NcmMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSetup {
    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {}

    public static class ForgeEvents {
        @SubscribeEvent
        public void onWorldLoad(LevelEvent.Load event) {
            if (event.getLevel() instanceof ServerLevel level) {
                RadiationManager.INSTANCE.initialize(level);
            }
        }

        @SubscribeEvent
        public void onWorldUnload(LevelEvent.Unload event) {
            if (event.getLevel() instanceof ServerLevel) {
                RadiationManager.INSTANCE.removeAll();
            }
        }

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                RadiationManager.INSTANCE.tick();
            }
        }

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Player player = event.player;
            if (player.level().isClientSide) return;

            double doseRate = RadiationManager.INSTANCE.getDoseRateAt(player.position());
            if (doseRate > 0) {
                double mSvPerTick = doseRate / 3600.0 / 20.0;
                player.getCapability(PlayerRadiationData.CAPABILITY).ifPresent(data -> {
                    data.addDose(mSvPerTick);
                    if (!data.areEffectsDisabled()) {
                        applyRadiationEffects(player, data.getDose());
                    }
                });
            }
        }

        private void applyRadiationEffects(Player player, double dose) {
            if (dose > Config.SERVER.doseThresholdDeath.get()) {
                player.hurt(player.damageSources().generic(), Float.MAX_VALUE);
                return;
            }
            if (dose > Config.SERVER.doseThresholdBlindness.get()) {
                if (player.tickCount % 100 == 0) player.hurt(player.damageSources().generic(), 2.0f);
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false));
            } else if (dose > Config.SERVER.doseThresholdDamage.get()) {
                if (player.tickCount % 600 == 0) player.hurt(player.damageSources().generic(), 1.0f);
            }
            if (dose > Config.SERVER.doseThresholdWeakness.get()) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, false));
            }
            if (dose > Config.SERVER.doseThresholdNausea.get()) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, false));
            }
        }

        // ---------- Дроп заражённого предмета ----------
        @SubscribeEvent
        public void onBlockBreak(BlockEvent.BreakEvent event) {
            if (event.getLevel().isClientSide()) return;
            BlockPos pos = event.getPos();
            double dose = RadiationManager.INSTANCE.getBlockDose(pos);
            if (dose <= 0 && !RadiationManager.INSTANCE.sources.containsKey(pos)) return;

            event.setCanceled(true);
            RadiationManager.INSTANCE.clearBlockDose(pos);
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.removeBlock(pos, false);
                ItemStack contaminated = new ItemStack(RegistryBus.CONTAMINATED_ITEM.get());
                double roundedDose = ContaminatedItem.roundDose(dose);
                contaminated.getOrCreateTag().putDouble(ContaminatedItem.TAG_DOSE, roundedDose);
                Block.popResource(serverLevel, pos, contaminated);
            }
        }

        @SubscribeEvent
        public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            Player player = event.getEntity();
            if (player.getItemInHand(event.getHand()).getItem() instanceof DosimeterItem) {
                if (player.getItemInHand(event.getHand()).getOrCreateTag().getBoolean("active")) {
                    event.setCanceled(true);
                    DosimeterItem.tryMeasure(player, event.getHand());
                }
            }
        }

        @SubscribeEvent
        public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
            Player player = event.getEntity();
            if (player.getItemInHand(event.getHand()).getItem() instanceof DosimeterItem) {
                DosimeterItem.tryMeasure(player, event.getHand());
            }
        }

        @SubscribeEvent
        public void onRegisterCommands(RegisterCommandsEvent event) {
            NcmCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(new ResourceLocation(NcmMod.MODID, "radiation"),
                        new PlayerRadiationDataProvider());
            }
        }

        @SubscribeEvent
        public void onPlayerClone(PlayerEvent.Clone event) {
            event.getOriginal().reviveCaps();
            if (event.isWasDeath()) {
                event.getOriginal().getCapability(PlayerRadiationData.CAPABILITY).ifPresent(old -> {
                    event.getEntity().getCapability(PlayerRadiationData.CAPABILITY).ifPresent(newData -> {
                        newData.resetDose();
                        newData.setEffectsDisabled(old.areEffectsDisabled());
                    });
                });
            } else {
                event.getOriginal().getCapability(PlayerRadiationData.CAPABILITY).ifPresent(old ->
                        event.getEntity().getCapability(PlayerRadiationData.CAPABILITY).ifPresent(newData ->
                                newData.setDose(old.getDose()))
                );
            }
            event.getOriginal().invalidateCaps();
        }
    }
}
