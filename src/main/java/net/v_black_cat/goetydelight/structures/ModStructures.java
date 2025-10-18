package net.v_black_cat.goetydelight.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModStructures {
    /**
     * 使用延迟注册系统来注册我们的结构，这是Forge上的首选方式。
     * 这将在正确的时间为我们注册基础结构，因此我们不必自己处理它。
     */
    public static final DeferredRegister<StructureType<?>> STRUCTURES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, GoetyDelight.MODID);

    /**
     * 注册基础结构本身并设置其路径。在这种情况下，
     * 此基础结构将具有资源位置 goetydelight:ectoplasmic_melon_field。
     */
    public static final RegistryObject<StructureType<EctoplasmicMelonFieldStructure>> ECTOPLASMIC_MELON_FIELD =
            STRUCTURES.register("ectoplasmic_melon_field",
                    () -> explicitStructureTypeTyping(EctoplasmicMelonFieldStructure.CODEC));

    /**
     * 最初，我在RegistryObject行上有双重lambda ()->()->，但事实证明
     * 一些IDE无法正确解析类型。此方法显式声明返回类型
     * 是这样IDE可以将其正确放入DeferredRegistry中。
     */
    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
        return () -> structureCodec;
    }

    public static void register(IEventBus eventBus) {
        STRUCTURES.register(eventBus);
    }
}
