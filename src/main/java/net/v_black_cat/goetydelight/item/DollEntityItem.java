package net.v_black_cat.goetydelight.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.DollEntity;
import net.v_black_cat.goetydelight.events.DollRegisterEventHandler;
import net.v_black_cat.goetydelight.init.ModBlocks;
import net.v_black_cat.goetydelight.init.ModDataComponents;
import net.v_black_cat.goetydelight.init.ModEntities;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.render.doll.DollEntityItemRender;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

import static net.v_black_cat.goetydelight.entities.DollEntity.TAG_BLOCK_STATE;
import static net.v_black_cat.goetydelight.entities.DollEntity.TAG_CUSTOM_DOLL_ID;

public class DollEntityItem extends Item {

    public DollEntityItem() {
        super(new Properties());
    }

    public static ItemStack createItemWithEntity(DollEntity entity) {
        ItemStack stack = new ItemStack(ModItems.DOLL_ITEM.get());
        saveDollEntity(stack, entity);
        return stack;
    }

    public static ItemStack createItemWithBlockState(BlockState state) {
        // 内置玩偶：方块注册名就是玩偶 id（doll_fox），一并写进去，保证「外观 + 音效/行为」都完整
        return createItemWithDoll(state, getDollIdFromBlockState(state));
    }

    /**
     * 玩偶实体物品的完整 NBT：block state（决定外观，走内置贴图）+ custom doll id（决定音效/行为）。
     * <p>此前两条路径各写一半——合成配方只写 id、创造栏只写 block state——于是：
     * 只有 id 的物品在实体渲染时会走「自定义玩偶」分支，而 {@code CustomDollLoader.getTexture(id)} 对内置玩偶返回 null
     * → 退回默认贴图（也就是"滚木"）；只有 block state 的物品又会因为缺 id 而放不下去。
     */
    public static ItemStack createItemWithDoll(BlockState state, String customDollId) {
        ItemStack stack = new ItemStack(ModItems.DOLL_ITEM.get());
        CompoundTag entityTag = new CompoundTag();
        entityTag.put(TAG_BLOCK_STATE, NbtUtils.writeBlockState(state));
        if (StringUtils.isNotBlank(customDollId)) {
            entityTag.putString(TAG_CUSTOM_DOLL_ID, customDollId);
        }
        stack.set(ModDataComponents.DOLL_ENTITY, entityTag);
        return stack;
    }

    /** 从方块状态反推玩偶 id：goetydelight:doll_fox → "doll_fox"（不是玩偶方块则返回空） */
    public static String getDollIdFromBlockState(BlockState state) {
        if (state == null || state.isAir()) {
            return StringUtils.EMPTY;
        }
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (blockId == null || !GoetyDelight.MODID.equals(blockId.getNamespace())) {
            return StringUtils.EMPTY;
        }
        return blockId.getPath();
    }

    /** 玩偶 id → 对应的玩偶方块状态（内置玩偶用；找不到返回 null） */
    public static BlockState getBlockStateFromDollId(String customDollId) {
        if (StringUtils.isBlank(customDollId)) {
            return null;
        }
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, customDollId));
        return block == Blocks.AIR ? null : block.defaultBlockState();
    }

    public static ItemStack createItemWithCustomDollId(String customDollId) {
        ItemStack stack = new ItemStack(ModItems.DOLL_ITEM.get());
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString(TAG_CUSTOM_DOLL_ID, customDollId);
        stack.set(ModDataComponents.DOLL_ENTITY, entityTag);
        return stack;
    }

    public static void saveDollEntity(ItemStack stack, DollEntity entity) {
        if (!stack.is(ModItems.DOLL_ITEM.get())) {
            return;
        }
        CompoundTag entityTag = new CompoundTag();
        entity.addAdditionalSaveData(entityTag);
        stack.set(ModDataComponents.DOLL_ENTITY, entityTag);
    }

    public static Block getBlockFromItemStack(ItemStack stack) {
        if (hasEntityData(stack)) {
            CompoundTag entityTag = stack.get(ModDataComponents.DOLL_ENTITY);
            if (entityTag != null && entityTag.contains(TAG_BLOCK_STATE)) {
                CompoundTag compound = entityTag.getCompound(TAG_BLOCK_STATE);
                HolderLookup<Block> lookup = BuiltInRegistries.BLOCK.asLookup();
                return NbtUtils.readBlockState(lookup, compound).getBlock();
            }
        }
        return Blocks.WHITE_WOOL;
    }

    public static String getCustomDollIdFromItemStack(ItemStack stack) {
        if (hasEntityData(stack)) {
            CompoundTag entityTag = stack.get(ModDataComponents.DOLL_ENTITY);
            if (entityTag != null && entityTag.contains(TAG_CUSTOM_DOLL_ID)) {
                return entityTag.getString(TAG_CUSTOM_DOLL_ID);
            }
        }
        return StringUtils.EMPTY;
    }

    public static DollEntity getDollEntity(Level level, ItemStack stack) {
        if (!stack.is(ModItems.DOLL_ITEM.get())) {
            return new DollEntity(ModEntities.DOLL_ENTITY.get(), level);
        }

        if (hasEntityData(stack)) {
            CompoundTag entityTag = stack.get(ModDataComponents.DOLL_ENTITY);
            if (entityTag != null) {
                DollEntity entity = new DollEntity(ModEntities.DOLL_ENTITY.get(), level);
                entity.load(entityTag);
                return entity;
            }
        }

        return new DollEntity(ModEntities.DOLL_ENTITY.get(), level);
    }

    public static void addCreativeTab(CreativeModeTab.Output output) {
        DollRegisterEventHandler.DOLL_BLOCKS.values().forEach(block -> {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            BlockState blockState = block.defaultBlockState();
            ItemStack stack = createItemWithBlockState(blockState);
            output.accept(stack);
        });
    }

    public static boolean hasEntityData(ItemStack stack) {
        return stack.has(ModDataComponents.DOLL_ENTITY);
    }

    public static void clearEntityData(ItemStack stack) {
        stack.remove(ModDataComponents.DOLL_ENTITY);
    }

    @Override
    @SuppressWarnings("removal")
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DollEntityItemRender render = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                Minecraft minecraft = Minecraft.getInstance();
                if (render == null) {
                    render = new DollEntityItemRender(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                }
                return render;
            }
        });
    }

    private boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos pos) {
        return !player.level().isOutsideBuildHeight(pos) && player.mayUseItemAt(pos, direction, itemStack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        return InteractionResultHolder.pass(itemStack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        if (player == null || !this.mayPlace(player, clickedFace, stack, clickedPos)) {
            return InteractionResult.FAIL;
        }

        BlockPos spawnPos = clickedPos.relative(clickedFace);
        Vec3 spawnLocation = Vec3.atBottomCenterOf(spawnPos);

        DollEntity dollEntity = getDollEntity(level, stack);

        if (StringUtils.isBlank(dollEntity.getCustomDollId())) {
            String customDollId = getCustomDollIdFromItemStack(stack);
            if (StringUtils.isBlank(customDollId)) {
                // 兜底：物品只带了 block state（创造栏 / 旧存档）→ 从方块反推玩偶 id
                customDollId = getDollIdFromBlockState(dollEntity.getDisplayBlockState());
            }
            if (StringUtils.isNotBlank(customDollId)) {
                dollEntity.setCustomDollId(customDollId);
            } else {
                return InteractionResult.FAIL;
            }
        }

        if (dollEntity.getDisplayBlockState().isAir()) {
            // 物品只带了 id（老配方产物 / 自定义玩偶）→ 用对应玩偶方块补齐外观；补不上才退回通用 custom_doll
            BlockState derived = getBlockStateFromDollId(dollEntity.getCustomDollId());
            dollEntity.setDisplayBlockState(derived != null ? derived : ModBlocks.CUSTOM_DOLL.get().defaultBlockState());
        }

        dollEntity.setPos(spawnLocation.x, spawnLocation.y, spawnLocation.z);
        dollEntity.setYRot(player.getYRot() - 180);

        if (dollEntity.canSurvives()) {
            if (!level.isClientSide) {
                dollEntity.playSound(SoundEvents.WOOL_PLACE, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, dollEntity.position());
                level.addFreshEntity(dollEntity);
            }
            stack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.FAIL;
    }
}
