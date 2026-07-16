package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.Goety;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.List;

public class TheBoxOfTheDeadItem extends Item {
    private static final ResourceLocation URN_LOOT_TABLE = ResourceLocation.parse(Goety.MOD_ID);

    public TheBoxOfTheDeadItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide && livingEntity instanceof Player player) {
            for (int i = 0; i < 3; i++) {
                generateUrnLoot(player, (ServerLevel) level);
            }
        }

        return result;
    }

    private void generateUrnLoot(Player player, ServerLevel level) {
//        try {
//            LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(URN_LOOT_TABLE);
//            if (lootTable == null || lootTable == LootTable.EMPTY) {
//                return;
//            }
//
//
//            LootParams params = new LootParams.Builder(level)
//                    .withParameter(LootContextParams.ORIGIN, player.position())
//                    .withParameter(LootContextParams.THIS_ENTITY, player)
//                    .withLuck(player.getLuck())
//                    .create(LootContextParamSets.CHEST);
//
//            ObjectArrayList<ItemStack> generatedLoot = lootTable.getRandomItems(params);
//
//            for (ItemStack itemStack : generatedLoot) {
//                if (!itemStack.isEmpty() && !player.getInventory().add(itemStack)) {
//                    player.drop(itemStack, false);
//                }
//            }
//
//        } catch (Exception e) {
//            GoetyDelight.LOGGER.warn("Failed to generate urn loot");
//        }
    }
}