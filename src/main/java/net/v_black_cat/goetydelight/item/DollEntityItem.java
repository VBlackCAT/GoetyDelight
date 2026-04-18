package net.v_black_cat.goetydelight.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.v_black_cat.goetydelight.entities.DollEntity;
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.event.ModRegisterEvent;
import net.v_black_cat.goetydelight.init.CustomDollLoader;
import net.v_black_cat.goetydelight.render.item.DollEntityItemRender;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

import static net.v_black_cat.goetydelight.entities.DollEntity.TAG_BLOCK_STATE;
import static net.v_black_cat.goetydelight.entities.DollEntity.TAG_CUSTOM_DOLL_ID;

public class DollEntityItem extends Item {
    private static final String TAG_DOLL_ENTITY = "doll_entity";

    public DollEntityItem() {
        super(new Properties());
    }

    public static ItemStack createItemWithEntity(DollEntity entity) {
        ItemStack stack = new ItemStack(ModItems.DOLL_ITEM.get());
        saveDollEntity(stack, entity);
        return stack;
    }

    public static ItemStack createItemWithBlockState(BlockState state) {
        ItemStack stack = new ItemStack(ModItems.DOLL_ITEM.get());
        CompoundTag entityTag = new CompoundTag();
        entityTag.put(TAG_BLOCK_STATE, NbtUtils.writeBlockState(state));
        CompoundTag stackTag = stack.getOrCreateTag();
        stackTag.put(TAG_DOLL_ENTITY, entityTag);
        return stack;
    }

    public static ItemStack createItemWithCustomDollId(String customDollId) {
        ItemStack stack = new ItemStack(ModItems.DOLL_ITEM.get());
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString(TAG_CUSTOM_DOLL_ID, customDollId);
        CompoundTag stackTag = stack.getOrCreateTag();
        stackTag.put(TAG_DOLL_ENTITY, entityTag);
        return stack;
    }
    public static void saveDollEntity(ItemStack stack, DollEntity entity) {
        if (!stack.is(ModItems.DOLL_ITEM.get())) {
            return;
        }

        CompoundTag entityTag = new CompoundTag();
        entity.addAdditionalSaveData(entityTag);

        CompoundTag stackTag = stack.getOrCreateTag();
        stackTag.put(TAG_DOLL_ENTITY, entityTag);
    }

    public static Block getBlockFromItemStack(ItemStack stack) {
        CompoundTag stackTag = stack.getTag();
        if (stackTag != null && stackTag.contains(TAG_DOLL_ENTITY)) {
            CompoundTag entityTag = stackTag.getCompound(TAG_DOLL_ENTITY);
            if (entityTag.contains(TAG_BLOCK_STATE)) {
                CompoundTag compound = entityTag.getCompound(TAG_BLOCK_STATE);
                HolderLookup<Block> lookup = BuiltInRegistries.BLOCK.asLookup();
                return NbtUtils.readBlockState(lookup, compound).getBlock();
            }
        }
        return Blocks.WHITE_WOOL;
    }

    public static String getCustomDollIdFromItemStack(ItemStack stack) {
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (registryName != null) {
            String dollId = registryName.getPath();
            ResourceLocation texture = CustomDollLoader.getTexture(dollId);
            if (texture != null) {
                return dollId;
            }
        }

        return StringUtils.EMPTY;
    }

    public static DollEntity getDollEntity(Level level, ItemStack stack) {
        if (!stack.is(ModItems.DOLL_ITEM.get())) {
            return new DollEntity(ModEntities.DOLL_ENTITY.get(), level);
        }

        CompoundTag stackTag = stack.getTag();
        if (stackTag != null && stackTag.contains(TAG_DOLL_ENTITY)) {
            CompoundTag entityTag = stackTag.getCompound(TAG_DOLL_ENTITY);
            DollEntity entity = new DollEntity(ModEntities.DOLL_ENTITY.get(), level);
            entity.load(entityTag);
            return entity;
        } else {
            return new DollEntity(ModEntities.DOLL_ENTITY.get(), level);
        }
    }

    public static void addCreativeTab(CreativeModeTab.Output output) {
        ModRegisterEvent.DOLL_BLOCKS.values().forEach(block -> {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            BlockState blockState = block.defaultBlockState();
            ItemStack stack = createItemWithBlockState(blockState);
            output.accept(stack);
        });
    }

    public static boolean hasEntityData(ItemStack stack) {
        CompoundTag stackTag = stack.getTag();
        return stackTag != null && stackTag.contains(TAG_DOLL_ENTITY);
    }

    public static void clearEntityData(ItemStack stack) {
        CompoundTag stackTag = stack.getTag();
        if (stackTag != null && stackTag.contains(TAG_DOLL_ENTITY)) {
            stackTag.remove(TAG_DOLL_ENTITY);
        }
    }
    @Override
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

    private boolean mayPlace(Player pPlayer, Direction pDirection, ItemStack pItemStack, BlockPos pPos) {
        return !pPlayer.level().isOutsideBuildHeight(pPos) && pPlayer.mayUseItemAt(pPos, pDirection, pItemStack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        return InteractionResultHolder.pass(itemStack);
    }
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
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

        // 检查位置是否可以放置实体
        BlockPos spawnPos = clickedPos.relative(clickedFace);
        Vec3 spawnLocation = Vec3.atBottomCenterOf(spawnPos);

        // 从物品获取或创建实体
        DollEntity dollEntity = getDollEntity(level, stack);

        if (StringUtils.isBlank(dollEntity.getCustomDollId())) {
            String customDollId = getCustomDollIdFromItemStack(stack);
            if (StringUtils.isNotBlank(customDollId)) {
                dollEntity.setCustomDollId(customDollId);
            } else {
                dollEntity.setCustomDollId("doll_5152");
            }
        }

        if (dollEntity.getDisplayBlockState().isAir()) {
            if (StringUtils.isNotBlank(dollEntity.getCustomDollId())) {
                dollEntity.setDisplayBlockState(ModBlocks.CUSTOM_DOLL.get().defaultBlockState());
            } else {
                String customId = getCustomDollIdFromItemStack(stack);
                if (StringUtils.isNotBlank(customId)) {
                    dollEntity.setCustomDollId(customId);
                } else {
                    dollEntity.setDisplayBlockState(Blocks.WHITE_WOOL.defaultBlockState());
                }
            }
        }

        // 设置实体位置和朝向
        dollEntity.setPos(spawnLocation.x, spawnLocation.y, spawnLocation.z);
        dollEntity.setYRot(player.getYRot() - 180);

        // 生成实体到世界
        if (dollEntity.canSurvives()) {
            if (!level.isClientSide) {
                dollEntity.playSound(SoundEvents.WOOL_PLACE, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, dollEntity.position());
                level.addFreshEntity(dollEntity);
            }
            // 消耗物品
            stack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.FAIL;
    }
}
