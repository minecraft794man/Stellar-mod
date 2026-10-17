package de.weltraum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Der Einschlag in der Oberwelt: schneidet die ganze Saeule vom Himmel bis zum Void weg
 * (ein paar Schichten pro Tick, damit das Spiel nicht ruckelt) und toetet alles darin.
 */
@EventBusSubscriber(modid = WeltraumMod.MODID)
public class LaserStrikes {
    private static final int LAYERS_PER_TICK = 12;
    private static final List<Strike> ACTIVE = new ArrayList<>();

    public static void start(ServerLevel level, int x, int z, int radius) {
        ACTIVE.add(new Strike(level, x, z, radius));
    }

    /** Partikel, die auch auf grosse Entfernung sichtbar sind. */
    public static void particles(ServerLevel level, ParticleOptions type, double x, double y, double z, int count, double spread) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(x, y, z) < 200.0 * 200.0) {
                level.sendParticles(player, type, true, x, y, z, count, spread, 0.0, spread, 0.0);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        Iterator<Strike> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Strike strike = it.next();
            if (strike.tick()) {
                strike.finish();
                it.remove();
            }
        }
    }

    private static final class Strike {
        private final ServerLevel level;
        private final int x;
        private final int z;
        private final int radius;
        private int y;

        Strike(ServerLevel level, int x, int z, int radius) {
            this.level = level;
            this.x = x;
            this.z = z;
            this.radius = radius;
            this.y = level.getMaxBuildHeight() - 1;

            for (int cx = (x - radius) >> 4; cx <= (x + radius) >> 4; cx++) {
                for (int cz = (z - radius) >> 4; cz <= (z + radius) >> 4; cz++) {
                    level.setChunkForced(cx, cz, true);
                }
            }
            level.playSound(null, new BlockPos(x, level.getSeaLevel() + 20, z),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 200.0F, 0.5F);
        }

        /** @return true, wenn die ganze Saeule abgetragen ist. */
        boolean tick() {
            int min = level.getMinBuildHeight();
            for (int i = 0; i < LAYERS_PER_TICK && y >= min; i++, y--) {
                carveLayer(y);
            }

            // Strahl und Funken
            for (int k = 0; k < 5; k++) {
                double beamY = Math.min(level.getMaxBuildHeight(), y + 20 + k * 40);
                particles(level, ParticleTypes.END_ROD, x + 0.5, beamY, z + 0.5, 20, radius * 0.4);
            }
            particles(level, ParticleTypes.FLAME, x + 0.5, y, z + 0.5, 30, radius * 0.6);

            killEntities();
            return y < min;
        }

        private void carveLayer(int layerY) {
            BlockState air = Blocks.AIR.defaultBlockState();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        pos.set(x + dx, layerY, z + dz);
                        if (!level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, air, 2);
                        }
                    }
                }
            }
        }

        private void killEntities() {
            AABB box = new AABB(x - radius, level.getMinBuildHeight(), z - radius,
                    x + radius + 1, level.getMaxBuildHeight(), z + radius + 1);
            double cx = x + 0.5;
            double cz = z + 0.5;
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> Mth.square(e.getX() - cx) + Mth.square(e.getZ() - cz) <= (double) radius * radius)) {
                if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
                    continue;
                }
                entity.kill();
            }
        }

        void finish() {
            for (int cx = (x - radius) >> 4; cx <= (x + radius) >> 4; cx++) {
                for (int cz = (z - radius) >> 4; cz <= (z + radius) >> 4; cz++) {
                    level.setChunkForced(cx, cz, false);
                }
            }
        }
    }
}
