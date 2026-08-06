package net.v_black_cat.goetydelight.init;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.buff.ActiveBuffs;
import net.v_black_cat.goetydelight.buff.BuffInstance;
import net.v_black_cat.goetydelight.visual.EntityVisualEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 统一管理所有附件类型的注册。
 * 附件用于为实体等对象附加临时或持久化数据。
 */
public class ModAttachments {

    private static final StreamCodec<ByteBuf, ActiveBuffs> ACTIVE_BUFFS_STREAM_CODEC =
            BuffInstance.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(ModAttachments::toActiveBuffs, ModAttachments::toInstanceList);


    private static final StreamCodec<ByteBuf, EntityVisualEffects> VISUAL_EFFECTS_STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(
                    tag -> {
                        EntityVisualEffects effects = new EntityVisualEffects();
                        effects.deserializeNBT(tag);
                        return effects;
                    },
                    EntityVisualEffects::serializeNBTForSync
            );

    private static ActiveBuffs toActiveBuffs(List<BuffInstance> instances) {
        ActiveBuffs buffs = new ActiveBuffs();
        for (BuffInstance inst : instances) {
            buffs.addBuff(inst.getTypeId(), inst.getDuration(), inst.getAmplifier());
        }
        return buffs;
    }

    private static List<BuffInstance> toInstanceList(ActiveBuffs buffs) {
        List<BuffInstance> all = new ArrayList<>();
        for (ResourceLocation typeId : buffs.getActiveTypes()) {
            all.addAll(buffs.getInstances(typeId));
        }
        return all;
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GoetyDelight.MODID);

    public static final Supplier<AttachmentType<ActiveBuffs>> ACTIVE_BUFFS =
            ATTACHMENT_TYPES.register("active_buffs",
                    () -> AttachmentType.serializable(ActiveBuffs::new)
                            .copyOnDeath()
                            .sync(ACTIVE_BUFFS_STREAM_CODEC)
                            .build()
            );

    public static final Supplier<AttachmentType<Long>> CHERRY_BLOSSOM_LAST_USAGE_DAY =
            ATTACHMENT_TYPES.register("cherry_blossom_last_usage_day",
                    () -> AttachmentType.builder(() -> 0L)
                            .serialize(Codec.LONG)          // 利用 Codec 自动序列化
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<EntityVisualEffects>> VISUAL_EFFECTS =
            ATTACHMENT_TYPES.register("visual_effects",
                    () -> AttachmentType.builder(EntityVisualEffects::new)
                            .serialize(new VisualEffectsSerializer()) // 提取为独立类或 lambda
                            .copyOnDeath()
                            .sync(VISUAL_EFFECTS_STREAM_CODEC)
                            .build()
            );

    // ==================== MinionBoost 层数附件（循环注册） ====================

    /**
     * 使用枚举或循环注册多个类似的整数附件，避免重复代码。
     */
    private enum MinionBoostType {
        STEW("minion_stew_boost_count"),
        SOUP("minion_soup_boost_count");

        final String path;

        MinionBoostType(String path) {
            this.path = path;
        }
    }

    public static final Supplier<AttachmentType<Integer>> MINION_STEW_BOOST_COUNT =
            registerIntAttachment(MinionBoostType.STEW);
    public static final Supplier<AttachmentType<Integer>> MINION_SOUP_BOOST_COUNT =
            registerIntAttachment(MinionBoostType.SOUP);

    private static Supplier<AttachmentType<Integer>> registerIntAttachment(MinionBoostType type) {
        return ATTACHMENT_TYPES.register(type.path,
                () -> AttachmentType.builder(() -> 0)
                        .serialize(Codec.INT)
                        .copyOnDeath()
                        .build()
        );
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }


    private static final class VisualEffectsSerializer
            implements IAttachmentSerializer<CompoundTag, EntityVisualEffects> {

        @Override
        public EntityVisualEffects read(IAttachmentHolder holder, CompoundTag tag,
                                        HolderLookup.Provider provider) {
            EntityVisualEffects effects = new EntityVisualEffects();
            effects.deserializeNBT(tag);
            return effects;
        }

        @Override
        public CompoundTag write(EntityVisualEffects attachment, HolderLookup.Provider provider) {
            return attachment.serializeNBT();
        }
    }
}