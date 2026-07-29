package net.v_black_cat.goetydelight.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.v_black_cat.goetydelight.render.doll.CustomDollItemRender;

import java.util.List;
import java.util.function.Consumer;

public class DollItem extends BlockItem {
    private final String langKey;

    public DollItem(Block block, String langKey) {
        super(block, new Properties());
        this.langKey = "tooltip.goetydelight.doll." + langKey;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("removal")
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private CustomDollItemRender render = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                Minecraft minecraft = Minecraft.getInstance();
                if (render == null) {
                    render = new CustomDollItemRender(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                }
                return render;
            }
        });
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("block.goetydelight.doll_block");
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(this.langKey).withStyle(ChatFormatting.DARK_GRAY));
    }
}