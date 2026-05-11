package com.kverlov.mcn.command;

import com.kverlov.mcn.config.Config;
import com.kverlov.mcn.radiation.PlayerRadiationData;
import com.kverlov.mcn.radiation.RadiationManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class NcmCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ncm")
                .then(Commands.literal("cellsize")
                    .then(Commands.argument("size", IntegerArgumentType.integer(1, 16))
                        .executes(ctx -> {
                            int newSize = IntegerArgumentType.getInteger(ctx, "size");
                            Config.SERVER.radiationCellSize.set(newSize);
                            ServerLevel level = ctx.getSource().getLevel();
                            if (level != null) {
                                RadiationManager.INSTANCE.reinitialize(level);
                            }
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("§aРазмер ячейки радиации изменён на " + newSize), true);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("airattenuation")
                    .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                        .executes(ctx -> {
                            double newValue = DoubleArgumentType.getDouble(ctx, "value");
                            Config.SERVER.airAttenuation.set(newValue);
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("§aОслабление воздуха изменено на " + String.format("%.2f", newValue)), true);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("memory")
                    .executes(ctx -> {
                        Runtime runtime = Runtime.getRuntime();
                        long used = runtime.totalMemory() - runtime.freeMemory();
                        long max = runtime.maxMemory();
                        long total = runtime.totalMemory();
                        String msg = String.format("§b[Память] Использовано: %d MB / Всего: %d MB (Макс: %d MB)",
                                used / 1024 / 1024,
                                total / 1024 / 1024,
                                max / 1024 / 1024);
                        ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
                        return 1;
                    })
                )
                .then(Commands.literal("toggleeffects")
                    .executes(ctx -> {
                        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                            player.getCapability(PlayerRadiationData.CAPABILITY).ifPresent(data -> {
                                boolean newState = !data.areEffectsDisabled();
                                data.setEffectsDisabled(newState);
                                String status = newState ? "Эффекты радиации §cотключены" : "Эффекты радиации §aвключены";
                                ctx.getSource().sendSuccess(() -> Component.literal(status), true);
                            });
                        }
                        return 1;
                    })
                )
        );
    }
}
