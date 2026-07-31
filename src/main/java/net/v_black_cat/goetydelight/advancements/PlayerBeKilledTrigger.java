package net.v_black_cat.goetydelight.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Optional;

public class PlayerBeKilledTrigger extends SimpleCriterionTrigger<PlayerBeKilledTrigger.TriggerInstance> {
    private final ResourceLocation id;

    public PlayerBeKilledTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Entity killer, DamageSource source) {
        LootContext lootcontext = EntityPredicate.createContext(player, killer);
        LootContext victimContext = EntityPredicate.createContext(player, player);
        super.trigger(player, (instance) -> instance.matches(player, lootcontext, source, victimContext));
    }

    public static record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<ContextAwarePredicate> entity,
            Optional<DamageSourcePredicate> killingBlow,
            Optional<ContextAwarePredicate> victim
    ) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player")
                                .forGetter(TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity")
                                .forGetter(TriggerInstance::entity),
                        DamageSourcePredicate.CODEC.optionalFieldOf("killing_blow")
                                .forGetter(TriggerInstance::killingBlow),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("victim")
                                .forGetter(TriggerInstance::victim)
                ).apply(instance, TriggerInstance::new)
        );

        // 无条件的触发器
        public static Criterion<TriggerInstance> playerBeKilled() {
            return ModAdvancementsTrigger.GHOST_FARMER_KILL_PLAYER.get()
                    .createCriterion(new TriggerInstance(
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty()
                    ));
        }

        // 带杀手条件的触发器
        public static Criterion<TriggerInstance> playerBeKilledBy(EntityPredicate.Builder killerBuilder) {
            return ModAdvancementsTrigger.GHOST_FARMER_KILL_PLAYER.get()
                    .createCriterion(new TriggerInstance(
                            Optional.empty(),
                            Optional.of(EntityPredicate.wrap(killerBuilder)),
                            Optional.empty(),
                            Optional.empty()
                    ));
        }

        // 带伤害类型条件的触发器
        public static Criterion<TriggerInstance> playerBeKilledBySource(DamageSourcePredicate.Builder sourceBuilder) {
            return ModAdvancementsTrigger.GHOST_FARMER_KILL_PLAYER.get()
                    .createCriterion(new TriggerInstance(
                            Optional.empty(),
                            Optional.empty(),
                            Optional.of(sourceBuilder.build()),
                            Optional.empty()
                    ));
        }

        // 完整条件的触发器
        public static Criterion<TriggerInstance> playerBeKilledWithConditions(
                EntityPredicate.Builder killerBuilder,
                DamageSourcePredicate.Builder sourceBuilder
        ) {
            return ModAdvancementsTrigger.GHOST_FARMER_KILL_PLAYER.get()
                    .createCriterion(new TriggerInstance(
                            Optional.empty(),
                            Optional.of(EntityPredicate.wrap(killerBuilder)),
                            Optional.of(sourceBuilder.build()),
                            Optional.empty()
                    ));
        }

        public boolean matches(ServerPlayer player, LootContext killerContext, DamageSource source, LootContext victimContext) {
            // 检查伤害来源
            if (this.killingBlow.isPresent() && !this.killingBlow.get().matches(player, source)) {
                return false;
            }
            // 检查杀手实体
            if (this.entity.isPresent() && !this.entity.get().matches(killerContext)) {
                return false;
            }
            // 检查受害者（被杀的玩家）
            if (this.victim.isPresent() && !this.victim.get().matches(victimContext)) {
                return false;
            }
            return true;
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }

        @Override
        public void validate(CriterionValidator validator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            validator.validateEntity(this.entity, ".entity");
            validator.validateEntity(this.victim, ".victim");
        }
    }
}