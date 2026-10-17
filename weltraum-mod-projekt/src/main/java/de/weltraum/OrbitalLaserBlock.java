package de.weltraum;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/** Rechtsklick im Weltraum: laedt den Laser auf und schiesst dann auf die Erde (gleiche X/Z-Koordinate). */
public class OrbitalLaserBlock extends Block implements EntityBlock {

    public OrbitalLaserBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OrbitalLaserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModRegistry.LASER_BE.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (lvl, pos, st, be) ->
                ((OrbitalLaserBlockEntity) be).serverTick((ServerLevel) lvl);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Planet planet = Planet.byDimension(level.dimension());
        if (planet == null || planet == Planet.EARTH) {
            player.displayClientMessage(Component.literal("Der Orbitallaser funktioniert nur im Weltraum."), true);
            return InteractionResult.CONSUME;
        }
        if (level.getBlockEntity(pos) instanceof OrbitalLaserBlockEntity laser) {
            if (!laser.startCharge((ServerLevel) level)) {
                player.displayClientMessage(Component.literal("Der Laser laedt bereits auf."), true);
            }
        }
        return InteractionResult.CONSUME;
    }
}
