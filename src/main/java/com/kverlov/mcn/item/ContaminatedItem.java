package com.kverlov.mcn.item;

import com.kverlov.mcn.radiation.RadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ContaminatedItem extends Item {
    public static final String TAG_DOSE = "radiation_dose";

    public ContaminatedItem(Properties props) {
        super(props);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stackItem, ItemEntity entity) {
        Level level = entity.level();
        if (!level.isClientSide && stackItem.hasTag() && stackItem.getTag().contains(TAG_DOSE)) {
            double dose = stackItem.getTag().getDouble(TAG_DOSE);
            BlockPos pos = entity.blockPosition();
            // Регистрируем предмет как временный источник в мире (если ещё не зарегистрирован)
            RadiationManager.INSTANCE.addSource(pos, dose);
            // Удалим источник через некоторое время, когда предмет подберут или он исчезнет
            // Для простоты просто добавим его каждый тик (addSource перезаписывает)
        }
        return false;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = super.getName(stack);
        if (stack.hasTag() && stack.getTag().contains(TAG_DOSE)) {
            double dose = stack.getTag().getDouble(TAG_DOSE);
            return baseName.copy().append(" (" + String.format("%.0f", dose) + " мЗв)");
        }
        return baseName;
    }

    /** Округляет дозу до ближайшего шага (по умолчанию 100) */
    public static double roundDose(double dose) {
        return Math.round(dose / 100.0) * 100.0;
    }
}
