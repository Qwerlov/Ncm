package com.kverlov.mcn.blocks;

import com.kverlov.mcn.blocks.entity.UraniumOreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class UraniumOreBlock extends Block implements EntityBlock {

    public UraniumOreBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UraniumOreBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        // Сначала вызываем стандартную логику
        super.onRemove(state, level, pos, newState, moved);
        // Если блок заменили на другой и это не та же руда, удаляем источник радиации
        if (!state.is(newState.getBlock())) {
            // BlockEntity удалится и вызовет setRemoved, где мы убираем источник
        }
    }
}
