package net.v_black_cat.goetydelight.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.util.FoodState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FoodStateCapability {

    public static final Capability<FoodState> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation ID = new ResourceLocation(GoetyDelight.MODID, "food_state");

    private FoodStateCapability() {
    }

    /** 获取实体上的食物状态；未附加时返回 null。 */
    @Nullable
    public static FoodState get(LivingEntity entity) {
        return entity.getCapability(CAP).resolve().orElse(null);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            Provider provider = new Provider();
            event.addCapability(ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    // 玩家死亡复活或跨维度时复制食物状态
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide) return;
        FoodState oldState = get(event.getOriginal());
        FoodState newState = get(event.getEntity());
        if (oldState != null && newState != null) {
            newState.fromTag(oldState.toTag());
        }
    }

    @Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent
        public static void register(RegisterCapabilitiesEvent event) {
            event.register(FoodState.class);
        }
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final FoodState state = new FoodState();
        private final LazyOptional<FoodState> optional = LazyOptional.of(() -> state);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CAP.orEmpty(cap, optional.cast());
        }

        @Override
        public CompoundTag serializeNBT() {
            return state.toTag();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            state.fromTag(nbt);
        }

        public void invalidate() {
            optional.invalidate();
        }
    }
}
