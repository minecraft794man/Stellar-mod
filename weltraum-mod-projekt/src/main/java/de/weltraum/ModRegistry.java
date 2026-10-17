package de.weltraum;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(WeltraumMod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WeltraumMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WeltraumMod.MODID);
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, WeltraumMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WeltraumMod.MODID);

    // ---- Block: Orbitallaser ----
    public static final DeferredBlock<OrbitalLaserBlock> ORBITAL_LASER = BLOCKS.registerBlock(
            "orbital_laser",
            OrbitalLaserBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                    .sound(SoundType.METAL).lightLevel(state -> 12));

    public static final Supplier<BlockEntityType<OrbitalLaserBlockEntity>> LASER_BE = BLOCK_ENTITIES.register(
            "orbital_laser",
            () -> BlockEntityType.Builder.of(OrbitalLaserBlockEntity::new, ORBITAL_LASER.get()).build(null));

    // ---- Raumanzug ----
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SPACESUIT = ARMOR_MATERIALS.register(
            "spacesuit",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 2);
                        map.put(ArmorItem.Type.LEGGINGS, 5);
                        map.put(ArmorItem.Type.CHESTPLATE, 6);
                        map.put(ArmorItem.Type.HELMET, 2);
                        map.put(ArmorItem.Type.BODY, 5);
                    }),
                    12,
                    SoundEvents.ARMOR_EQUIP_IRON,
                    () -> Ingredient.of(Items.IRON_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(WeltraumMod.MODID, "spacesuit"))),
                    0.0F,
                    0.0F));

    // ---- Items ----
    public static final DeferredItem<Item> ROCKET = ITEMS.register("rocket",
            () -> new RocketItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ORBITAL_LASER_ITEM = ITEMS.register("orbital_laser",
            () -> new BlockItem(ORBITAL_LASER.get(), new Item.Properties()));

    public static final DeferredItem<Item> SPACESUIT_HELMET = ITEMS.register("spacesuit_helmet",
            () -> new ArmorItem(SPACESUIT, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(30))));
    public static final DeferredItem<Item> SPACESUIT_CHESTPLATE = ITEMS.register("spacesuit_chestplate",
            () -> new ArmorItem(SPACESUIT, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(30))));
    public static final DeferredItem<Item> SPACESUIT_LEGGINGS = ITEMS.register("spacesuit_leggings",
            () -> new ArmorItem(SPACESUIT, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(30))));
    public static final DeferredItem<Item> SPACESUIT_BOOTS = ITEMS.register("spacesuit_boots",
            () -> new ArmorItem(SPACESUIT, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(30))));

    // ---- Kreativ-Tab ----
    public static final Supplier<CreativeModeTab> TAB = TABS.register("weltraum",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.weltraum"))
                    .icon(() -> new ItemStack(ROCKET.get()))
                    .displayItems((params, output) -> {
                        output.accept(ROCKET.get());
                        output.accept(ORBITAL_LASER_ITEM.get());
                        output.accept(SPACESUIT_HELMET.get());
                        output.accept(SPACESUIT_CHESTPLATE.get());
                        output.accept(SPACESUIT_LEGGINGS.get());
                        output.accept(SPACESUIT_BOOTS.get());
                    })
                    .build());

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ARMOR_MATERIALS.register(modBus);
        TABS.register(modBus);
    }
}
