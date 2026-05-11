package com.kverlov.mcn.radiation;

import net.minecraft.core.BlockPos;

public class RadiationSource {
    public final BlockPos pos;
    public double activity;

    public RadiationSource(BlockPos pos, double activity) {
        this.pos = pos;
        this.activity = activity;
    }
}
