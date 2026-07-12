package net.v_black_cat.goetydelight.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.buff.BuffType;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.buff.effect.BurningEffect;
import net.v_black_cat.goetydelight.buff.effect.VigorEffect;

import java.util.*;

public class ModBuffTypes {
    public static final ResourceKey<Registry<BuffType>> BUFF_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "buff_types"));

    public static final DeferredRegister<BuffType> BUFF_TYPES =
            DeferredRegister.create(BUFF_REGISTRY_KEY, GoetyDelight.MODID);

    // 在静态初始化中直接创建注册表（此时 NewRegistryEvent 尚未触发）
    public static final Registry<BuffType> BUFF_REGISTRY = BUFF_TYPES.makeRegistry(builder -> {
        builder.sync(true);
        builder.defaultKey(ResourceLocation.withDefaultNamespace("empty"));
    });

    // 效果映射表
    private static final Map<ResourceLocation, BuffEffect> EFFECTS = new HashMap<>();

    private static <T extends BuffEffect> DeferredHolder<BuffType, BuffType> registerWithEffect(
            String name, BuffType type, T effect) {
        DeferredHolder<BuffType, BuffType> holder = BUFF_TYPES.register(name, () -> type);
        EFFECTS.put(holder.getId(), effect);
        return holder;
    }

    public static BuffEffect getEffect(ResourceLocation typeId) {
        return EFFECTS.get(typeId);
    }

    public static Set<ResourceLocation> getRegisteredIds() {
        if (BUFF_REGISTRY == null) return Collections.emptySet();
        return BUFF_REGISTRY.keySet();
    }

    // 占位符
    public static final DeferredHolder<BuffType, BuffType> EMPTY =
            registerWithEffect("empty", new BuffType(0, 0, false), (entity, amp) -> {});

    // 灼烧
    public static final DeferredHolder<BuffType, BuffType> BURNING =
            registerWithEffect("burning", new BuffType(200, 1, false), new BurningEffect());

    // 活力
    public static final DeferredHolder<BuffType, BuffType> VIGOR =
            registerWithEffect("vigor", new BuffType(600, 1, true), new VigorEffect());

    public static void register(IEventBus modEventBus) {
        BUFF_TYPES.register(modEventBus);
    }
}