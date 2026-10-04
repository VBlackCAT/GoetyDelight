package net.v_black_cat.goetydelight.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.core.BlockPos;

public class YBelowCondition implements LootItemCondition {

    private final int maxY;

    public YBelowCondition(int maxY) {
        this.maxY = maxY;
    }

    @Override
    public boolean test(LootContext context) {
        // 容器战利品表上下文中，ORIGIN 就是方块位置
        BlockPos pos = null;
        if (context.hasParam(LootContextParams.ORIGIN)) {
            pos = BlockPos.containing(context.getParam(LootContextParams.ORIGIN));
        } else if (context.hasParam(LootContextParams.BLOCK_STATE)) {
            // 兜底
            pos = BlockPos.containing(context.getParam(LootContextParams.ORIGIN));
        }
        return pos != null && pos.getY() < maxY;
    }

    @Override
    public LootItemConditionType getType() {
        return RegHelper.Y_BELOW_CONDITION.get();
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<YBelowCondition> {
        @Override
        public void serialize(JsonObject json, YBelowCondition condition, JsonSerializationContext context) {
            json.addProperty("max_y", condition.maxY);
        }

        @Override
        public YBelowCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            int maxY = GsonHelper.getAsInt(json, "max_y", 64);
            return new YBelowCondition(maxY);
        }
    }
}