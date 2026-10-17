package de.weltraum;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Rechtsklick: zum gewaehlten Ziel fliegen. Shift + Rechtsklick: Ziel wechseln.
 */
public class RocketItem extends Item {
    private static final String DEST = "weltraum_dest";
    private static final String EARTH_X = "weltraum_ex";
    private static final String EARTH_Z = "weltraum_ez";

    public RocketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.success(stack);
        }

        Planet current = Planet.byDimension(level.dimension());
        if (current == null) {
            sp.displayClientMessage(Component.literal("Von hier aus kann die Rakete nicht starten."), true);
            return InteractionResultHolder.fail(stack);
        }

        CompoundTag data = sp.getPersistentData();
        Planet dest = Planet.byId(data.getString(DEST));
        if (dest == null || dest == current) {
            dest = cycle(current, current);
        }

        if (player.isShiftKeyDown()) {
            dest = cycle(dest, current);
            data.putString(DEST, dest.id());
            sp.displayClientMessage(Component.literal("Ziel: " + dest.displayName()
                    + "  (Shift + Rechtsklick: wechseln, Rechtsklick: starten)").withStyle(ChatFormatting.AQUA), true);
            return InteractionResultHolder.success(stack);
        }

        travel(sp, current, dest);
        player.getCooldowns().addCooldown(this, 60);
        return InteractionResultHolder.success(stack);
    }

    private static Planet cycle(Planet from, Planet current) {
        Planet p = from.next();
        while (p == current) {
            p = p.next();
        }
        return p;
    }

    private static void travel(ServerPlayer sp, Planet from, Planet to) {
        ServerLevel target = sp.server.getLevel(to.dimension());
        if (target == null) {
            sp.displayClientMessage(Component.literal("Ziel nicht gefunden (Dimension fehlt)."), true);
            return;
        }

        if (from == Planet.EARTH) {
            sp.getPersistentData().putInt(EARTH_X, sp.getBlockX());
            sp.getPersistentData().putInt(EARTH_Z, sp.getBlockZ());
        }

        // Start-Effekt
        ServerLevel source = sp.serverLevel();
        source.sendParticles(ParticleTypes.FLAME, sp.getX(), sp.getY(), sp.getZ(), 80, 0.6, 0.2, 0.6, 0.15);
        source.sendParticles(ParticleTypes.LARGE_SMOKE, sp.getX(), sp.getY(), sp.getZ(), 60, 0.8, 0.2, 0.8, 0.05);
        source.playSound(null, sp.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 3.0F, 0.6F);

        Vec3 pos;
        if (to == Planet.EARTH) {
            int x;
            int z;
            CompoundTag data = sp.getPersistentData();
            if (data.contains(EARTH_X)) {
                x = data.getInt(EARTH_X);
                z = data.getInt(EARTH_Z);
            } else {
                BlockPos spawn = target.getSharedSpawnPos();
                x = spawn.getX();
                z = spawn.getZ();
            }
            target.getChunk(x >> 4, z >> 4);
            int y = target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 1;
            pos = new Vec3(x + 0.5, y, z + 0.5);
        } else {
            pos = to.spawn();
            target.getChunk(((int) pos.x) >> 4, ((int) pos.z) >> 4);
            if (to == Planet.ORBIT) {
                buildPlatform(target);
            }
        }

        sp.teleportTo(target, pos.x, pos.y, pos.z, sp.getYRot(), sp.getXRot());
        sp.fallDistance = 0;

        sp.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 15));
        sp.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal(to.displayName()).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)));
    }

    /** Kleine Landeplattform im Orbit, falls dort noch nichts steht. */
    private static void buildPlatform(ServerLevel orbit) {
        BlockPos center = new BlockPos(0, 100, 0);
        if (!orbit.getBlockState(center).isAir()) {
            return;
        }
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                orbit.setBlock(center.offset(x, 0, z), Blocks.SMOOTH_BASALT.defaultBlockState(), 3);
            }
        }
        for (int sx : new int[] {-5, 5}) {
            for (int sz : new int[] {-5, 5}) {
                orbit.setBlock(center.offset(sx, 0, sz), Blocks.SEA_LANTERN.defaultBlockState(), 3);
            }
        }
    }
}
