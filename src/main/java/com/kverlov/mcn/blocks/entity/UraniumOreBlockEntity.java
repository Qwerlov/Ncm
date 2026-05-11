package com.kverlov.mcn.blocks.entity;

import com.kverlov.mcn.core.RegistryBus;
import com.kverlov.mcn.radiation.RadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UraniumOreBlockEntity extends BlockEntity {
    public UraniumOreBlockEntity(BlockPos pos, BlockState state) {
        super(RegistryBus.URANIUM_ORE_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            RadiationManager.INSTANCE.addSource(worldPosition, 1000.0);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            RadiationManager.INSTANCE.removeSource(worldPosition);
        }
    }
}
