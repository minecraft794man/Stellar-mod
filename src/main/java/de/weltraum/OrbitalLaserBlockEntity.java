package de.weltraum;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class OrbitalLaserBlockEntity extends BlockEntity {
    /** 10 Sekunden aufladen. */
    public static final int CHARGE_TICKS = 200;
    /** Radius des Schusses in Bloecken. */
    public static final int RADIUS = 12;

    private boolean charging = false;
    private int charge = 0;

    public OrbitalLaserBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.LASER_BE.get(), pos, state);
    }

    public boolean startCharge(ServerLevel level) {
        if (charging) {
            return false;
        }
        charging = true;
        charge = 0;
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("ACHTUNG: Orbitallaser laedt auf! Einschlag in 10 s bei X "
                        + worldPosition.getX() + ", Z " + worldPosition.getZ() + " (Oberwelt), Radius " + RADIUS),
                false);
        level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0F, 0.5F);
        return true;
    }

    public void serverTick(ServerLevel level) {
        if (!charging) {
            return;
        }
        charge++;

        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 1.2;
        double z = worldPosition.getZ() + 0.5;

        int amount = 1 + charge / 20;
        level.sendParticles(ParticleTypes.END_ROD, x, y, z, amount, 0.5, 0.5, 0.5, 0.02);
        if (charge % 40 == 0) {
            float pitch = 0.5F + 1.5F * charge / CHARGE_TICKS;
            level.playSound(null, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 2.0F, pitch);
        }

        if (charge >= CHARGE_TICKS) {
            fire(level);
            charging = false;
            charge = 0;
        }
    }

    private void fire(ServerLevel level) {
        int x = worldPosition.getX();
        int z = worldPosition.getZ();

        level.playSound(null, worldPosition, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 5.0F, 0.6F);

        // Strahl im Weltraum: vom Block nach unten
        for (int i = 0; i < 70; i++) {
            LaserStrikes.particles(level, ParticleTypes.END_ROD, x + 0.5, worldPosition.getY() - i * 2.0, z + 0.5, 12, 0.3);
        }

        // Einschlag in der Oberwelt
        LaserStrikes.start(level.getServer().overworld(), x, z, RADIUS);
    }
}
