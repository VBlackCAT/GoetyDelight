package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.HashSet;
import java.util.Set;

/**
 * 石油雾领域的目标判定。
 *
 * 判定优先级：
 * 1. 排除施法者本人、与施法者同盟的生物、无敌/已死亡目标；
 * 2. 命中 {@link #TARGET_ENTITY_IDS} 中写死的实体 id；
 * 3. 命中 {@link #TARGET_TAG}（goetydelight:malevolent_shrine_targets）实体类型标签。
 *
 * 以后要调整攻击对象时，改这里即可：
 * - 临时添加某个实体：往 TARGET_ENTITY_IDS 里加 ResourceLocation；
 * - 让整合包作者自定义：往 data/goetydelight/tags/entity_types/malevolent_shrine_targets.json 里加条目。
 */
public final class MalevolentShrineTargets {

    private static final TagKey<EntityType<?>> TARGET_TAG = TagKey.create(
            Registries.ENTITY_TYPE,
            new ResourceLocation(GoetyDelight.MODID, "malevolent_shrine_targets")
    );

    /** 默认写死的攻击目标 id，方便代码里直接改；标签可额外覆盖。 */
    private static final Set<ResourceLocation> TARGET_ENTITY_IDS = new HashSet<>();

    static {
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "zombie"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "husk"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "drowned"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "skeleton"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "stray"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "wither_skeleton"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "spider"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "cave_spider"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "creeper"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "enderman"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "blaze"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "ghast"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "witch"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "vindicator"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "evoker"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "pillager"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "ravager"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "phantom"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "silverfish"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "endermite"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "shulker"));
        TARGET_ENTITY_IDS.add(new ResourceLocation("minecraft", "warden"));
    }

    private MalevolentShrineTargets() {
    }

    public static boolean isTarget(LivingEntity target, LivingEntity caster) {
        if (target == null || caster == null || target == caster) {
            return false;
        }
        if (!target.isAlive() || target.isInvulnerable()) {
            return false;
        }
        if (MobUtil.areAllies(target, caster)) {
            return false;
        }

        ResourceLocation id = EntityType.getKey(target.getType());
        return TARGET_ENTITY_IDS.contains(id) || target.getType().is(TARGET_TAG);
    }
}