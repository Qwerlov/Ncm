package com.kverlov.mcn.radiation;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerRadiationData implements INBTSerializable<CompoundTag> {
    public static final Capability<PlayerRadiationData> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>(){});

    private double accumulatedDose;
    private boolean disableEffects;

    public double getDose() { return accumulatedDose; }
    public void addDose(double mSv) { this.accumulatedDose += mSv; }
    public void setDose(double mSv) { this.accumulatedDose = mSv; }
    public void resetDose() { accumulatedDose = 0; }

    public boolean areEffectsDisabled() { return disableEffects; }
    public void setEffectsDisabled(boolean disabled) { this.disableEffects = disabled; }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("dose", accumulatedDose);
        tag.putBoolean("disableEffects", disableEffects);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        accumulatedDose = nbt.getDouble("dose");
        disableEffects = nbt.getBoolean("disableEffects");
    }
}
