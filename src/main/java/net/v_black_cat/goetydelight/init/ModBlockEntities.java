package net.v_black_cat.goetydelight.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.CustomDollBlockEntity;
import net.v_black_cat.goetydelight.block.NightStoveBlockEntity;
import net.v_black_cat.goetydelight.block.RenderBlockEntity;
import net.v_black_cat.goetydelight.block.ShadeStoveBlockEntity;
import net.v_black_cat.goetydelight.block.CursedIngotPotBlockEntity;
import net.v_black_cat.goetydelight.events.DollRegisterEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ModBlockEntities {
    public static final DeferredRegister<
            BlockEntityType<
                    ?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GoetyDelight.MODID);

    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<
                    NightStoveBlockEntity>> NIGHT_STOVE_BE = BLOCK_ENTITIES.register("night_stove", () -> BlockEntityType.Builder.of(
            NightStoveBlockEntity::new,
            ModBlocks.NIGHT_STOVE.get()
    ).build(null));

    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<
                    ShadeStoveBlockEntity>> SHADE_STOVE_BE = BLOCK_ENTITIES.register("shade_stove", () -> BlockEntityType.Builder.of(
            ShadeStoveBlockEntity::new,
            ModBlocks.SHADE_STOVE.get()
    ).build(null));
    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<
                    CursedIngotPotBlockEntity>> CURSED_INGOT_POT_BE = BLOCK_ENTITIES.register("cursed_ingot_pot", () -> BlockEntityType.Builder.of(
            CursedIngotPotBlockEntity::new,
            ModBlocks.CURSED_INGOT_POT.get()
    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CustomDollBlockEntity>> DOLL_BLOCK =
            BLOCK_ENTITIES.register("custom_doll", () -> {
                // 这个 lambda 会在方块注册完成后执行
                // 此时所有方块都已经注册到 BuiltInRegistries.BLOCK 中
                Block[] validBlocks = getAllDollBlocksFromRegistry();
                return BlockEntityType.Builder.of(
                        CustomDollBlockEntity::new,
                        validBlocks
                ).build(null);
            });

    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<
                    RenderBlockEntity>> RENDER_BLOCK = BLOCK_ENTITIES.register("render_block", () -> BlockEntityType.Builder.of(
            RenderBlockEntity::new,
            ModBlocks.RENDER_BLOCK.get()
    ).build(null));

    private static Block[] getAllDollBlocksFromRegistry() {
        List<Block> blocks = new ArrayList<>();

        for (String dollName : DollRegisterEventHandler.SPECIAL_DOLL_NAMES) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, dollName);
            Block block = BuiltInRegistries.BLOCK.get(id);
            if (block != null) {
                blocks.add(block);
            }
        }

        // 也添加 CUSTOM_DOLL 如果存在
        Block customDoll = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "custom_doll")
        );
        if (customDoll != null && !blocks.contains(customDoll)) {
            blocks.add(customDoll);
        }

        return blocks.toArray(new Block[0]);
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
