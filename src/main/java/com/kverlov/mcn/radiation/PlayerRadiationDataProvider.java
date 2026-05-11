package com.kverlov.mcn.radiation;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerRadiationDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final PlayerRadiationData data = new PlayerRadiationData();
    private final LazyOptional<PlayerRadiationData> optional = LazyOptional.of(() -> data);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == PlayerRadiationData.CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() { return data.serializeNBT(); }
    @Override
    public void deserializeNBT(CompoundTag nbt) { data.deserializeNBT(nbt); }
}
