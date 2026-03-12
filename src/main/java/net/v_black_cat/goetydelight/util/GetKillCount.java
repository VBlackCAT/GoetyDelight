package net.v_black_cat.goetydelight.util;

import net.minecraft.stats.Stats;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class GetKillCount {
    public static int getKillCount(ServerPlayer player, EntityType<?> entityType) {
        var statsCounter = player.getStats();
        // 构建针对该实体类型的“击杀”统计项
        // 例如: Stats.ENTITY_KILLED.get(EntityType.ZOMBIE) 代表击杀僵尸的数量
        Stat<EntityType<?>> killStat = Stats.ENTITY_KILLED.get(entityType);
        return statsCounter.getValue(killStat);
    }
}
