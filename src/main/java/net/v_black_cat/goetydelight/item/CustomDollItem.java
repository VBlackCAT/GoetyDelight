package net.v_black_cat.goetydelight.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.v_black_cat.goetydelight.block.CustomDollBlockEntity;
import net.v_black_cat.goetydelight.init.doll.CustomDollLoader;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.init.doll.ServerCustomDollLoader;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CustomDollItem extends BlockItem {
    private static final String NBT_MODEL_ID = "DollModelId";

    public CustomDollItem(Block block) {
        super(block, new Item.Properties());
    }

    @Nullable
    public static String getModelId(ItemStack doll) {
        CustomData data = doll.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.contains(NBT_MODEL_ID)) {
            return data.copyTag().getString(NBT_MODEL_ID);
        }
        return null;
    }

    public static void setModelId(ItemStack doll, String modelId) {
        doll.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY,
                d -> d.update(tag -> tag.putString(NBT_MODEL_ID, modelId)));
    }

    public static void addCreativeTab(CreativeModeTab.Output output) {
        output.accept(new ItemStack(ModItems.CUSTOM_DOLL.get()));
        ServerCustomDollLoader.getModels().forEach(modelId -> {
            ItemStack dollStack = new ItemStack(ModItems.CUSTOM_DOLL.get());
            setModelId(dollStack, modelId);
            output.accept(dollStack);
        });
    }

    public static ItemStack getDefaultItemStack() {
        ItemStack dollStack = new ItemStack(ModItems.CUSTOM_DOLL.get());
        ServerCustomDollLoader.getModels().stream().findFirst().ifPresent(modelId -> {
            setModelId(dollStack, modelId);
        });
        return dollStack;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!result) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CustomDollBlockEntity customDollBlockEntity) {
                String modelId = getModelId(stack);
                if (modelId != null) {
                    customDollBlockEntity.setModelId(modelId);
                }
            }
        }
        return result;
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("block.goetydelight.doll");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        String modelId = getModelId(stack);
        if (modelId != null) {
            String locale = Minecraft.getInstance().getLanguageManager().getSelected();
            String[] lines = CustomDollLoader.getLanguage(locale, modelId).split("\n");
            for (String line : lines) {
                list.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            list.add(Component.translatable("tooltip.goetydelight.custom.unknown").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
