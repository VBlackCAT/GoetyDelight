package net.v_black_cat.goetydelight.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.DollEntity;
import net.v_black_cat.goetydelight.events.DollRegisterEventHandler;
import org.apache.commons.lang3.StringUtils;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum DollEntityComponentProvider implements IEntityComponentProvider {
    INSTANCE;

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "doll_entity");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig pluginConfig) {
        if (!(accessor.getEntity() instanceof DollEntity dollEntity)) {
            return;
        }

        String dollId = dollEntity.getCustomDollId();
        if (StringUtils.isNotBlank(dollId)) {
            tooltip.add(Component.translatable("tooltip.goetydelight.doll." + dollId).withStyle(ChatFormatting.AQUA));
            return;
        }

        Block block = dollEntity.getDisplayBlockState().getBlock();
        // 使用 BuiltInRegistries 替代 ForgeRegistries
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        if (key == null) {
            return;
        }
        String vanillaDesc = DollRegisterEventHandler.VANILLA_TOOLTIPS.getOrDefault(key, "vanilla");
        String specialDesc = DollRegisterEventHandler.SPECIAL_TOOLTIPS.getOrDefault(key, vanillaDesc);
        tooltip.add(Component.translatable("tooltip.goetydelight.doll." + specialDesc).withStyle(ChatFormatting.AQUA));
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}