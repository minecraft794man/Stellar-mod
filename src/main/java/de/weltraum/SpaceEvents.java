package de.weltraum;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Schwerkraft und Sauerstoff auf den Himmelskoerpern. */
@EventBusSubscriber(modid = WeltraumMod.MODID)
public class SpaceEvents {
    /** 20 Sekunden Sauerstoff ohne Raumanzug. */
    private static final int MAX_OXYGEN = 20 * 20;
    private static final Map<UUID, Integer> OXYGEN = new HashMap<>();

    private static final ResourceLocation GRAVITY_ID =
            ResourceLocation.fromNamespaceAndPath(WeltraumMod.MODID, "low_gravity");
    private static final ResourceLocation FALL_ID =
            ResourceLocation.fromNamespaceAndPath(WeltraumMod.MODID, "soft_landing");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Planet planet = Planet.byDimension(player.level().dimension());
        applyGravity(player, planet);
        handleOxygen(player, planet);
    }

    private static void applyGravity(ServerPlayer player, Planet planet) {
        AttributeInstance gravity = player.getAttribute(Attributes.GRAVITY);
        AttributeInstance fall = player.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (gravity == null || fall == null) {
            return;
        }

        if (planet == null || planet.gravity() >= 1.0) {
            if (gravity.getModifier(GRAVITY_ID) != null) gravity.removeModifier(GRAVITY_ID);
            if (fall.getModifier(FALL_ID) != null) fall.removeModifier(FALL_ID);
            return;
        }

        double amount = planet.gravity() - 1.0;
        AttributeModifier current = gravity.getModifier(GRAVITY_ID);
        if (current == null || current.amount() != amount) {
            gravity.removeModifier(GRAVITY_ID);
            gravity.addTransientModifier(
                    new AttributeModifier(GRAVITY_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (fall.getModifier(FALL_ID) == null) {
            fall.addTransientModifier(
                    new AttributeModifier(FALL_ID, 1000.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static boolean hasSuit(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModRegistry.SPACESUIT_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModRegistry.SPACESUIT_CHESTPLATE.get());
    }

    private static void handleOxygen(ServerPlayer player, Planet planet) {
        UUID id = player.getUUID();
        int oxygen = OXYGEN.getOrDefault(id, MAX_OXYGEN);

        boolean needsAir = planet != null && planet.isAirless() && !player.isCreative() && !player.isSpectator();
        if (!needsAir) {
            OXYGEN.put(id, Math.min(MAX_OXYGEN, oxygen + 10));
            return;
        }

        boolean suited = hasSuit(player);
        if (suited) {
            oxygen = Math.min(MAX_OXYGEN, oxygen + 10);
        } else {
            oxygen = Math.max(0, oxygen - 1);
            if (oxygen == 0 && player.tickCount % 20 == 0) {
                player.hurt(player.damageSources().drown(), 2.0F);
            }
        }
        OXYGEN.put(id, oxygen);

        if (player.tickCount % 10 == 0) {
            if (suited) {
                player.displayClientMessage(
                        Component.literal("Raumanzug aktiv").withStyle(ChatFormatting.GREEN), true);
            } else {
                int seconds = oxygen / 20;
                player.displayClientMessage(
                        Component.literal("Sauerstoff: " + seconds + " s - Raumanzug (Helm + Brustpanzer) anziehen!")
                                .withStyle(seconds <= 5 ? ChatFormatting.RED : ChatFormatting.YELLOW),
                        true);
            }
        }
    }
}
