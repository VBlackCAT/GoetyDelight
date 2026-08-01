package net.v_black_cat.goetydelight.init;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.buff.ActiveBuffs;
import net.v_black_cat.goetydelight.buff.BuffInstance;
import net.v_black_cat.goetydelight.visual.EntityVisualEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModAttachments {
    /**
     * ActiveBuffs 的流编解码：以 BuffInstance 列表形式同步到客户端。
     */
    private static final StreamCodec<ByteBuf, ActiveBuffs> ACTIVE_BUFFS_STREAM_CODEC =
            BuffInstance.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(ModAttachments::decodeActiveBuffs, ModAttachments::encodeActiveBuffs);

    private static ActiveBuffs decodeActiveBuffs(List<BuffInstance> instances) {
        ActiveBuffs buffs = new ActiveBuffs();
        for (BuffInstance inst : instances) {
            buffs.addBuff(inst.getTypeId(), inst.getDuration(), inst.getAmplifier());
        }
        return buffs;
    }

    private static List<BuffInstance> encodeActiveBuffs(ActiveBuffs buffs) {
        List<BuffInstance> all = new ArrayList<>();
        for (ResourceLocation typeId : buffs.getActiveTypes()) {
            all.addAll(buffs.getInstances(typeId));
        }
        return all;
    }

    /**
     * 视觉特效的同步编解码：与 serializeNBTForSync 一致（包含仅渲染的 transient 特效）。
     */
    private static final StreamCodec<ByteBuf, EntityVisualEffects> VISUAL_EFFECTS_STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(
                    ModAttachments::decodeVisualEffects,
                    ModAttachments::encodeVisualEffects
            );

    private static EntityVisualEffects decodeVisualEffects(CompoundTag tag) {
        EntityVisualEffects effects = new EntityVisualEffects();
        effects.deserializeNBT(tag);
        return effects;
    }

    private static CompoundTag encodeVisualEffects(EntityVisualEffects effects) {
        return effects.serializeNBTForSync();
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GoetyDelight.MODID);

    public static final Supplier<AttachmentType<ActiveBuffs>> ACTIVE_BUFFS =
            ATTACHMENT_TYPES.register("active_buffs",
                    () -> AttachmentType.serializable(ActiveBuffs::new)
                            .copyOnDeath()   // 死亡后保留
                            .sync(ACTIVE_BUFFS_STREAM_CODEC)  // 同步到客户端（初始跟踪自动同步，变更走 AttachmentSync）
                            .build()
            );

    public static final Supplier<AttachmentType<Long>> CHERRY_BLOSSOM_LAST_USAGE_DAY =
            ATTACHMENT_TYPES.register("cherry_blossom_last_usage_day",
                    () -> AttachmentType.builder(() -> 0L)
                            .serialize(Codec.LONG)
                            .copyOnDeath()
                            .build()
            );

    /**
     * 实体视觉特效存储（1.21.1 采用 attachment 系统，对应 EntityVisualEffectSystem）。
     */
    public static final Supplier<AttachmentType<EntityVisualEffects>> VISUAL_EFFECTS =
            ATTACHMENT_TYPES.register("visual_effects",
                    () -> AttachmentType.builder(EntityVisualEffects::new)
                            .serialize(new IAttachmentSerializer<CompoundTag, EntityVisualEffects>() {
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
                            })
                            .copyOnDeath()
                            .sync(VISUAL_EFFECTS_STREAM_CODEC)
                            .build()
            );

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
