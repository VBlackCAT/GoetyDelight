package net.v_black_cat.goetydelight.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * 仅对"容器战利品表"生效：要求上下文带有 BLOCK_ENTITY 参数
 * （箱子、桶、漏斗等方块实体掉落时才会带这个参数）。
 */
public class ContainerYBelowCondition implements LootItemCondition {

    private final int maxY;

    public ContainerYBelowCondition(int maxY) {
        this.maxY = maxY;
    }

    @Override
    public boolean test(LootContext context) {
        // 关键：只有容器战利品表上下文才会有 BLOCK_ENTITY
        if (!context.hasParam(LootContextParams.BLOCK_ENTITY)) {
            return false;
        }

        if (!context.hasParam(LootContextParams.ORIGIN)) {
            return false;
        }

        BlockPos pos = BlockPos.containing(context.getParam(LootContextParams.ORIGIN));
        return pos.getY() < maxY;
    }

    @Override
    public LootItemConditionType getType() {
        return RegHelper.CONTAINER_Y_BELOW_CONDITION.get();
    }

    public static class Serializer
            implements net.minecraft.world.level.storage.loot.Serializer<ContainerYBelowCondition> {

        @Override
        public void serialize(JsonObject json, ContainerYBelowCondition condition,
                              JsonSerializationContext context) {
            json.addProperty("max_y", condition.maxY);
        }

        @Override
        public ContainerYBelowCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            int maxY = GsonHelper.getAsInt(json, "max_y", 64);
            return new ContainerYBelowCondition(maxY);
        }
    }
}