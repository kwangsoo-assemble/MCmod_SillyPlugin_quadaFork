package dev.celestial.silly.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SillyBlockContainer {
    public BlockState state;
    @Nullable
    public BlockEntity entity;
    public BlockPos coordinates;
    @Nullable
    public UUID owner;

    public SillyBlockContainer(BlockPos pos, BlockState state, @Nullable UUID owner, @Nullable BlockEntity entity) {
        this.owner = owner;
        this.coordinates = pos;
        this.state = state;
        this.entity = entity;
    }

    public SillyBlockContainer(BlockPos pos, BlockState state, UUID owner) {
        this(pos, state, owner, null);
    }

    public SillyBlockContainer(BlockPos pos, BlockState state, BlockEntity entity) {
        this(pos,state,null,entity);
    }

    public SillyBlockContainer(BlockPos pos, BlockState state) {
        this(pos,state,null,null);
    }
}
