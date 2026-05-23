package net.v_black_cat.goetydelight.compat.jade;

import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.DollEntity;
import net.v_black_cat.goetydelight.event.ModRegisterEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum DollEntityComponentProvider implements IEntityComponentProvider {
    INSTANCE;

    public static final ResourceLocation ID = new ResourceLocation(GoetyDelight.MODID, "doll_entity");

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
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        if (key == null) {
            return;
        }
        String vanillaDesc = ModRegisterEvent.VANILLA_TOOLTIPS.getOrDefault(key, "vanilla");
        String specialDesc = ModRegisterEvent.SPECIAL_TOOLTIPS.getOrDefault(key, vanillaDesc);
        tooltip.add(Component.translatable("tooltip.goetydelight.doll." + specialDesc).withStyle(ChatFormatting.AQUA));
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
