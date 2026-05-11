package com.kverlov.mcn.item;

import com.kverlov.mcn.radiation.PlayerRadiationData;
import com.kverlov.mcn.radiation.RadiationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DosimeterItem extends Item {
    private static final int MEASURE_COOLDOWN_TICKS = 20;

    public DosimeterItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                showDebugInfo(player);
            } else {
                boolean active = !stack.getOrCreateTag().getBoolean("active");
                stack.getOrCreateTag().putBoolean("active", active);
                if (active) {
                    showRadiationInfo(player);
                    player.sendSystemMessage(Component.literal("§aДозиметр включён"));
                } else {
                    player.sendSystemMessage(Component.literal("§eДозиметр выключен"));
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static void tryMeasure(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof DosimeterItem && stack.getOrCreateTag().getBoolean("active")) {
            Level level = player.level();
            if (!level.isClientSide) {
                long currentTick = level.getGameTime();
                long lastTick = stack.getOrCreateTag().getLong("lastMeasureTick");
                if (currentTick - lastTick >= MEASURE_COOLDOWN_TICKS) {
                    stack.getOrCreateTag().putLong("lastMeasureTick", currentTick);
                    showRadiationInfo(player);
                }
            }
        }
    }

    private static void showRadiationInfo(Player player) {
        double doseRate = RadiationManager.INSTANCE.getDoseRateAt(player.position());
        double accumulated = player.getCapability(PlayerRadiationData.CAPABILITY)
                .map(PlayerRadiationData::getDose).orElse(0.0);
        player.sendSystemMessage(Component.literal(
                "§7[Мощность дозы: " + String.format("%.1f", doseRate) +
                " мкЗв/ч, Накоплено: " + String.format("%.2f", accumulated) + " мЗв]"));
    }

    private static void showDebugInfo(Player player) {
        double doseRate = RadiationManager.INSTANCE.getDoseRateAt(player.position());
        double rawDose = RadiationManager.INSTANCE.getDoseRateAtRaw(player.position());
        double attenuation = doseRate / Math.max(0.001, rawDose);
        player.sendSystemMessage(Component.literal(
                "§d[Отладка] Доза: " + String.format("%.1f", doseRate) +
                " мкЗв/ч | Сырая: " + String.format("%.1f", rawDose) +
                " мкЗв/ч | Ослабление: " + String.format("%.3f", attenuation)));
    }
}
