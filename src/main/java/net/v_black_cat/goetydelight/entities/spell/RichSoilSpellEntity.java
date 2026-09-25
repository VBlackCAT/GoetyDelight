package net.v_black_cat.goetydelight.entities.spell;

import com.Polarice3.Goety.common.entities.projectiles.SpellEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.spell.CropGrowthSpell;
import net.v_black_cat.goetydelight.spell.HoeHarvestSpell;
import net.v_black_cat.goetydelight.spell.LaowangSpell;
import net.v_black_cat.goetydelight.spell.LoveAndFertilitySpell;
import net.v_black_cat.goetydelight.spell.MarbleWaterSpell;
import net.v_black_cat.goetydelight.spell.RichSoilSpell;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class RichSoilSpellEntity extends SpellEntity {
    public static final int WINDUP_TICKS = 30;
    public static final int IMPACT_TICKS = 8;
    public static final int TOTAL_LIFETIME_TICKS = 42;
    public static final int MAX_RADIUS = 7;
    public static final float BEAM_HEIGHT = 14.0F;

    private static final EntityDataAccessor<Integer> DATA_RADIUS =
            SynchedEntityData.defineId(RichSoilSpellEntity.class, EntityDataSerializers.INT);

    private EffectType effectType = EffectType.RICH_SOIL;
    private boolean shiftMode;
    private boolean silkTouch;
    private int fortune;
    private int potency;
    private boolean magnet;
    private int durationTicks;
    private int cloudSide;
    private int penetration;
    private Direction direction = Direction.UP;
    private ItemStack effectTool = ItemStack.EMPTY;
    private boolean completed;

    public RichSoilSpellEntity(EntityType<? extends RichSoilSpellEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setInvisible(true);
    }

    public RichSoilSpellEntity(Level level, LivingEntity caster, BlockPos center, int radius, EffectType effectType) {
        this(ModEntities.RICH_SOIL_SPELL.get(), level);
        this.effectType = effectType;
        this.setOwner(caster);
        this.setRadius(radius);
        this.setPos(center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D);
    }

    public RichSoilSpellEntity(Level level, BlockPos center, int radius) {
        this(level, null, center, radius, EffectType.RICH_SOIL);
    }

    public RichSoilSpellEntity setStaff(ItemStack staff) {
        this.setCastingStaff(staff);
        return this;
    }

    public RichSoilSpellEntity setEffectTool(ItemStack effectTool) {
        this.effectTool = effectTool.copy();
        return this;
    }

    public RichSoilSpellEntity setShiftMode(boolean shiftMode) {
        this.shiftMode = shiftMode;
        return this;
    }

    public RichSoilSpellEntity setToolData(boolean silkTouch, int fortune, boolean magnet) {
        this.silkTouch = silkTouch;
        this.fortune = fortune;
        this.magnet = magnet;
        return this;
    }

    public RichSoilSpellEntity setPotency(int potency) {
        this.potency = potency;
        return this;
    }

    public RichSoilSpellEntity setDurationTicks(int durationTicks) {
        this.durationTicks = durationTicks;
        return this;
    }

    public RichSoilSpellEntity setCloudSide(int cloudSide) {
        this.cloudSide = cloudSide;
        return this;
    }

    /** 穿透层数：y ∈ [-penetration, +penetration]；0 = 只转化光柱落地那一层 */
    public RichSoilSpellEntity setPenetration(int penetration) {
        this.penetration = Math.max(0, penetration);
        return this;
    }

    public RichSoilSpellEntity setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.getOwnerId();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        if (this.tickCount == 1) {
            this.playSound(SoundEvents.BEACON_ACTIVATE, 1.1F, 0.72F);
        }
        if (this.tickCount == 12) {
            this.playSound(SoundEvents.CONDUIT_AMBIENT_SHORT, 0.8F, 1.35F);
        }

        if (this.tickCount < WINDUP_TICKS) {
            if (this.tickCount % 2 == 0) {
                this.spawnDistantSoulParticles();
            }
            return;
        }

        if (!this.completed) {
            this.completed = true;
            this.impact();
        }

        if (this.tickCount >= TOTAL_LIFETIME_TICKS) {
            this.discard();
        }
    }

    protected void impact() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos center = this.blockPosition().below();
        int radius = this.getRadius();
        LivingEntity owner = this.getOwner();
        boolean changed = this.performImpact(serverLevel, owner, center, radius);
        int burstCount = 18 + radius * 6;
        serverLevel.sendParticles(ParticleTypes.SOUL,
                this.getX(), this.getY() + 0.35D, this.getZ(),
                burstCount, radius * 0.55D, 0.25D, radius * 0.55D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getY() + 0.5D, this.getZ(),
                12 + radius * 3, radius * 0.45D, 0.35D, radius * 0.45D, 0.03D);

        this.playSound(SoundEvents.SOUL_ESCAPE, 1.25F, 0.72F);
        this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.1F, 1.15F);
        if (changed) {
            this.playSound(SoundEvents.BONE_MEAL_USE, 1.2F, 0.85F);
        }
    }

    protected boolean performImpact(ServerLevel serverLevel, LivingEntity owner, BlockPos center, int radius) {
        return switch (this.effectType) {
            case RICH_SOIL -> this.convertRichSoil(serverLevel, center, radius);
            case CROP_GROWTH -> CropGrowthSpell.performDeferredEffect(serverLevel, center, radius);
            // 老王聚晶已与光柱解耦（改由 LaowangSpell.SpellResult 直接召唤），此分支保留仅作兼容
            case LAOWANG -> LaowangSpell.performDeferredEffect(serverLevel, owner, this.potency);
            case HOE_HARVEST -> HoeHarvestSpell.performDeferredEffect(
                    serverLevel, owner, center, radius, this.shiftMode, this.silkTouch, this.fortune, this.magnet, this.effectTool);
            case MARBLE_WATER -> MarbleWaterSpell.performDeferredEffect(
                    serverLevel, owner, center, this.direction, radius, this.shiftMode);
            case LOVE_AND_FERTILITY -> LoveAndFertilitySpell.performDeferredEffect(
                    serverLevel, owner, center, this.cloudSide, this.durationTicks);
        };
    }

    private boolean convertRichSoil(ServerLevel serverLevel, BlockPos center, int radius) {
        int converted = 0;
        for (int y = -this.penetration; y <= this.penetration; ++y) {
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    BlockPos pos = center.offset(dx, y, dz);
                    BlockState current = serverLevel.getBlockState(pos);
                    BlockState richSoil = RichSoilSpell.convertState(current);
                    if (richSoil == null || richSoil == current) {
                        continue;
                    }

                    serverLevel.setBlock(pos, richSoil, 3);
                    converted++;

                    if (serverLevel.random.nextInt(5) == 0) {
                        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                                pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                                1, 0.16D, 0.05D, 0.16D, 0.01D);
                    }
                }
            }
        }
        return converted > 0;
    }

    private void spawnDistantSoulParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double angle = this.random.nextDouble() * Math.PI * 2.0D;
        double radius = 0.45D + this.random.nextDouble() * (0.8D + this.getRadius() * 0.18D);
        double x = this.getX() + Math.cos(angle) * radius;
        double z = this.getZ() + Math.sin(angle) * radius;
        double y = this.getY() + this.random.nextDouble() * BEAM_HEIGHT;

        serverLevel.sendParticles(ParticleTypes.SOUL, x, y, z, 1, 0.02D, 0.04D, 0.02D, 0.012D);
        if (this.tickCount % 6 == 0) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, this.getY() + 0.15D, z,
                    1, 0.04D, 0.08D, 0.04D, 0.01D);
        }
    }

    public int getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public void setRadius(int radius) {
        this.entityData.set(DATA_RADIUS, Mth.clamp(radius, 1, MAX_RADIUS));
    }

    public float animationTime(float partialTick) {
        return Math.max(0.0F, this.tickCount + partialTick - 1.0F);
    }

    @Override
    public boolean save(CompoundTag tag) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SpellEntity.OWNER_UNIQUE_ID, Optional.empty());
        this.entityData.define(SpellEntity.OWNER_CLIENT_ID, -1);
        this.entityData.define(SpellEntity.TARGET_UNIQUE_ID, Optional.empty());
        this.entityData.define(SpellEntity.TARGET_CLIENT_ID, -1);
        this.entityData.define(SpellEntity.DATA_EXTRA_DAMAGE, 0.0F);
        this.entityData.define(DATA_RADIUS, 1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128.0D * 128.0D;
    }

    public enum EffectType {
        RICH_SOIL,
        CROP_GROWTH,
        LAOWANG,
        HOE_HARVEST,
        MARBLE_WATER,
        LOVE_AND_FERTILITY
    }
}