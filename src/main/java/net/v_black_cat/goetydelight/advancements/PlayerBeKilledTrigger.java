package net.v_black_cat.goetydelight.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerBeKilledTrigger extends SimpleCriterionTrigger<PlayerBeKilledTrigger.TriggerInstance> {
    final ResourceLocation id;
    public PlayerBeKilledTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext deserializationContext) {
        return new TriggerInstance(
                id,
                predicate,
                EntityPredicate.fromJson(json, "entity", deserializationContext),
                DamageSourcePredicate.fromJson(json.get("killing_blow")),
                EntityPredicate.fromJson(json, "victim", deserializationContext)
        );
    }

    public void trigger(ServerPlayer player, Entity killer, DamageSource source) {
        LootContext lootcontext = EntityPredicate.createContext(player, killer);
        LootContext victimContext = EntityPredicate.createContext(player, player);
        super.trigger(player, (instance) -> instance.matches(player, lootcontext, source, victimContext));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate killerPredicate;
        private final DamageSourcePredicate killingBlow;
        private final ContextAwarePredicate victimPredicate;

        public TriggerInstance(ResourceLocation criterion, ContextAwarePredicate player, ContextAwarePredicate killerPredicate, DamageSourcePredicate killingBlow, ContextAwarePredicate victimPredicate) {
            super(criterion, player);
            this.killerPredicate = killerPredicate;
            this.killingBlow = killingBlow;
            this.victimPredicate = victimPredicate;
        }

        public boolean matches(ServerPlayer player, LootContext killerContext, DamageSource source, LootContext victimContext) {
            if (!this.killingBlow.matches(player, source)) {
                return false;
            }
            if (!this.killerPredicate.matches(killerContext)) {
                return false;
            }
            return this.victimPredicate.matches(victimContext);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext conditions) {
            JsonObject jsonobject = super.serializeToJson(conditions);
            jsonobject.add("entity", this.killerPredicate.toJson(conditions));
            jsonobject.add("killing_blow", this.killingBlow.serializeToJson());
            jsonobject.add("victim", this.victimPredicate.toJson(conditions));
            return jsonobject;
        }
    }
}
