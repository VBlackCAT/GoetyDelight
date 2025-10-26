package net.v_black_cat.goetydelight.entities;
import com.Polarice3.Goety.common.entities.ai.FloatSwimGoal;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.entities.neutral.AbstractWraith;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.ModBlocks;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.RichSoilFarmlandBlock;

import java.util.OptionalInt;
import java.util.*;

import static com.Polarice3.Goety.common.items.ModItems.ECTOPLASM;

public class GhostFarmerEntity extends AbstractWraith implements Merchant {
    
    
    private static final int MAX_GROWTH_STAGE = 7;
    private static final double PLANT_CHANCE = 0.05;
    private static final int SCAN_RADIUS = 15;
    private static final String PLANTED_STEMS_TAG = "PlantedStems";
    private static final Component TRADE_TITLE = Component.translatable("entity.goetydelight.ghost_farmer.trade");
    private static final Component NOT_NIGHT_MESSAGE = Component.translatable("entity.goetydelight.ghost_farmer.not_night");

    
    public final AnimationState idleAnimationState = new AnimationState();
    private final Set<BlockPos> plantStemsPos = new HashSet<>();
    private final Set<BlockPos> attachedStems = new HashSet<>();
    private final Set<BlockPos> plantedPos = new HashSet<>();
    private final SimpleContainer inventory = new SimpleContainer(8);
    @Nullable
    protected MerchantOffers offers;
    @Nullable
    private Player tradingPlayer;
    private long lastRestockTime = -1;
    private boolean hasRestockedToday = false;

    public final AnimationState attackAnimationState = new AnimationState();
    private int attackTick = 0;
    private static final int ATTACK_DURATION = 38; 
    private Player attackTarget;
    private int idleAnimationTimeout = 0;
    private int attackAnimationTimeout = 0;
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(GhostFarmerEntity.class, EntityDataSerializers.BOOLEAN);


    public GhostFarmerEntity(EntityType<? extends Summoned> entityType, Level level) {
        super(entityType, level);
    }
    @Override
    protected boolean isSunSensitive() {
        return false;
    }
    
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            setupAnimationStates(); 
        } else {
            checkMidnightRestock();

            
            if (isAttacking()) {
                attackTick++;

                if (attackTick == ATTACK_DURATION / 2 && attackTarget != null) {
                    executePlayer(attackTarget);
                }

                if (attackTick >= ATTACK_DURATION) {
                    resetAttack();
                }
            }
            if (isNightTime() && isInTargetStructure()) {
                if(tickCount%20==0){
                    checkAndRemoveInvalidPlantingSites();
                    melonStemWithered();
                    acceleratingStemsGrowth();
                }

                if(tickCount%100==0){
                    scanForSuitablePlantingLocations();
                    plantMelonStems();
                }

            }
            
            if(!isNightTime()){
                if (!plantStemsPos.isEmpty() || !attachedStems.isEmpty()){
                    plantStemsPos.clear();
                    attachedStems.clear();
                    plantedPos.clear();
                }
            }
        }
    }
    
    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    
    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
    }
    
    private void setupAnimationStates() {
        
        if (this.idleAnimationTimeout <= 0 && !isAttacking()) {
            this.idleAnimationTimeout = 60; 
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }

        
        if (isAttacking() && attackAnimationTimeout <= 0) {
            attackAnimationTimeout = ATTACK_DURATION; 
            attackAnimationState.start(this.tickCount);
        } else if (attackAnimationTimeout > 0) {
            --this.attackAnimationTimeout;
            if (this.attackAnimationTimeout <= 0) {
                this.attackAnimationState.stop();
            }
        }

        
        if (!isAttacking()) {
            attackAnimationState.stop();
        }
    }



    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    public boolean isTrading() {
        return this.tradingPlayer != null;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            this.updateTrades();
        }
        return this.offers;
    }

    @Override
    public void overrideOffers(@Nullable MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        if (!this.level().isClientSide) {
            this.playSound(this.getTradeUpdatedSound(!stack.isEmpty()), this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
        
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public void openTradingScreen(Player player, Component displayName, int level) {
        OptionalInt optionalint = player.openMenu(new SimpleMenuProvider((containerId, playerInventory, playerEntity) -> {
            return new GhostFarmerMerchantMenu(containerId, playerInventory, this);
        }, this.getCustomTradeTitle()));

        if (optionalint.isPresent()) {
            MerchantOffers merchantoffers = this.getOffers();
            if (!merchantoffers.isEmpty()) {
                player.sendMerchantOffers(optionalint.getAsInt(), merchantoffers, 0, 0, false, false);
            }
        }
    }

    protected Component getCustomTradeTitle() {
        return TRADE_TITLE;
    }

    protected void updateTrades() {
        this.offers.clear();

        this.offers.add(new MerchantOffer(
                new ItemStack(Items.MELON_SEEDS, 1),
                new ItemStack(ECTOPLASM.get(), 2),
                new ItemStack(net.v_black_cat.goetydelight.item.ModItems.ECTOPLASMIC_MELON_SEEDS.get(), 1),
                12,
                0,
                0.0f
        ));
        
        this.offers.add(new MerchantOffer(
                new ItemStack(ECTOPLASM.get(), 18),
                new ItemStack(Items.MELON, 1),
                new ItemStack(ModBlocks.ECTOPLASMIC_MELON_BLOCK.get().asItem(), 1),
                12,
                0,
                0.0f
        ));

        this.offers.add(new MerchantOffer(
                new ItemStack(ECTOPLASM.get(), 3),
                new ItemStack(Items.MELON_SLICE, 1),
                new ItemStack(net.v_black_cat.goetydelight.item.ModItems.ECTOPLASMIC_MELON.get(), 1),
                Integer.MAX_VALUE,
                0,
                0.0f
        ));
    }

    protected void stopTrading() {
        this.setTradingPlayer(null);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.getItem() != Items.VILLAGER_SPAWN_EGG && this.isAlive() && !this.isTrading()) {
            if (!this.level().isClientSide && !isNightTime()) {
                player.displayClientMessage(NOT_NIGHT_MESSAGE, true);
                return InteractionResult.sidedSuccess(false);
            }

            if (this.level().isClientSide) {
                return InteractionResult.SUCCESS;
            } else {
                this.startTrading(player);
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    private void startTrading(Player player) {
        this.setTradingPlayer(player);
        this.openTradingScreen(player, this.getDisplayName(), 0);
    }

    public boolean canTradeNow() {
        return isNightTime();
    }

    
    private void melonStemWithered() {
        for (BlockPos stemPos : attachedStems){
            Block block = this.level().getBlockState(stemPos).getBlock();
            if (block instanceof AttachedStemBlock) {
                BlockPos pos = stemPos.below();
                Block blockBelow = this.level().getBlockState(pos).getBlock();
                if (blockBelow instanceof RichSoilFarmlandBlock) {
                    this.level().setBlockAndUpdate(stemPos, ModBlocks.ECTOPLASMIC_MELON_STEM.get().defaultBlockState().setValue(StemBlock.AGE, MAX_GROWTH_STAGE));
                }
            } else if (block instanceof StemBlock) {
                BlockPos pos = stemPos.below();
                Block blockBelow = this.level().getBlockState(pos).getBlock();

                if (blockBelow instanceof RichSoilFarmlandBlock) {
                    int currentAge = this.level().getBlockState(stemPos).getValue(StemBlock.AGE);
                    if (currentAge > 0) {
                        this.level().setBlockAndUpdate(stemPos, this.level().getBlockState(stemPos).setValue(StemBlock.AGE, currentAge - 1));
                    } else {
                        this.level().removeBlock(stemPos, false);
                    }
                }
            }
        }
    }

    private void checkAndRemoveInvalidPlantingSites() {
        plantStemsPos.removeIf(pos -> {
            Block block = this.level().getBlockState(pos).getBlock();

            if (!(block instanceof StemBlock)) {
                return true;
            }


            if (attachedStems.contains(pos)) {
                return true;
            }
            
            return false;
        });
    }

    private void acceleratingStemsGrowth() {
        for (BlockPos stemPos : plantStemsPos){
            if (!attachedStems.contains(stemPos)){
                Block block = this.level().getBlockState(stemPos).getBlock();
                if (block instanceof StemBlock) {
                    int age = this.level().getBlockState(stemPos).getValue(StemBlock.AGE);
                    if (age < MAX_GROWTH_STAGE) {
                        this.level().setBlockAndUpdate(stemPos, this.level().getBlockState(stemPos).setValue(StemBlock.AGE, age + 1));
                    }
                    
                    if (age >= MAX_GROWTH_STAGE) {
                        for (int i=0; i < 10; i++){
                            Block stemBlock = this.level().getBlockState(stemPos).getBlock();
                            stemBlock.randomTick(this.level().getBlockState(stemPos), (ServerLevel) this.level(), stemPos, this.level().random);
                        }
                    }
                    
                    if(this.level().getBlockState(stemPos).getBlock() instanceof AttachedStemBlock){
                        attachedStems.add(stemPos);
                    }
                }
            }
        }
    }

    private void plantMelonStems() {
        for (BlockPos stemPos : plantStemsPos) {
            if(!attachedStems.contains(stemPos)){
                Block block = this.level().getBlockState(stemPos).getBlock();
                if (!(block instanceof StemBlock)&&!plantedPos.contains(stemPos)) {
                    if (this.random.nextDouble() < PLANT_CHANCE) {
                        this.level().setBlockAndUpdate(stemPos, ModBlocks.ECTOPLASMIC_MELON_STEM.get().defaultBlockState());
                        plantedPos.add(stemPos);
                    }
                }
            }
        }
    }

    private void scanForSuitablePlantingLocations() {
        int scanRadius = SCAN_RADIUS;
        BlockPos currentPos = this.blockPosition();
        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int z = -scanRadius; z <= scanRadius; z++) {
                for (int y = -2; y <= 2; y++) {
                    BlockPos pos = currentPos.offset(x, y, z);
                    if (attachedStems.contains(pos)) continue;
                    BlockPos posAbove = pos.above();
                    if (this.level().getBlockState(pos).getBlock() instanceof RichSoilFarmlandBlock) {
                        if (this.level().getBlockState(posAbove).getBlock() instanceof AirBlock) {
                            plantStemsPos.add(posAbove);
                        }
                    }
                }
            }
        }
    }

    
    private void checkMidnightRestock() {
        long currentTime = this.level().getDayTime();
        long currentDay = currentTime / 24000;

        if (lastRestockTime / 24000 < currentDay) {
            hasRestockedToday = false;
        }

        long timeOfDay = currentTime % 24000;
        if (timeOfDay >= 0 && timeOfDay < 1000 && !hasRestockedToday) {
            restockTrades();
            hasRestockedToday = true;
            lastRestockTime = currentTime;
        }
    }

    private void restockTrades() {
        if (this.offers != null) {
            for (MerchantOffer offer : this.offers) {
                offer.resetUses();
            }
        }
    }

    private boolean isNightTime() {
        long timeOfDay = this.level().getDayTime() % 24000;
        return timeOfDay >= 13000 && timeOfDay < 24000;
    }

    private boolean isInTargetStructure() {
        BlockPos currentPos = this.blockPosition();
        ServerLevel serverLevel = (ServerLevel) this.level();
        ResourceKey<Structure> structure = ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("goetydelight", "ectoplasmic_melon_field"));

        StructureStart structureStart = serverLevel.structureManager().getStructureWithPieceAt(currentPos, structure);
        return structureStart != null && structureStart.isValid();
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatSwimGoal(this));
        this.goalSelector.addGoal(2, new AttackPlayerGoal()); 
        this.goalSelector.addGoal(3, new RestrictSunGoal(this));
        this.goalSelector.addGoal(4, new FleeSunGoal(this, 1.0));
        this.goalSelector.addGoal(9, new WraithLookGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new WraithLookGoal(this, Mob.class, 8.0F));
        this.goalSelector.addGoal(10, new WraithLookRandomlyGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return null;
    }

    
    class AttackPlayerGoal extends Goal {
        private Player targetPlayer;
        private int cooldown = 0;

        public AttackPlayerGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (!isNightTime() || !isInTargetStructure()) {
                return false;
            }
            this.targetPlayer = findAttackTarget();
            return this.targetPlayer != null && !isAttacking();
        }

        @Override
        public boolean canContinueToUse() {
            return isAttacking() || (targetPlayer != null && targetPlayer.isAlive() &&
                    distanceToSqr(targetPlayer) < 256.0D); 
        }

        @Override
        public void start() {
            if (targetPlayer != null) {
                startAttack(targetPlayer);
                cooldown = 100;
                
                setAttacking(true);
            }
        }

        @Override
        public void stop() {
            resetAttack();
            this.targetPlayer = null;
        }

        @Override
        public void tick() {
            if (targetPlayer != null && !isAttacking()) {
                
                getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);

                
                if (distanceToSqr(targetPlayer) < 16.0D) { 
                    startAttack(targetPlayer);
                }
            }
        }

        private Player findAttackTarget() {
            AABB searchArea = new AABB(blockPosition()).inflate(16); 
            List<Player> players = level().getEntitiesOfClass(Player.class, searchArea);

            for (Player player : players) {
                if (hasStolenMelon(player) || isSuspicious(player)) {
                    return player;
                }
            }
            return null;
        }
    }

    private void startAttack(Player player) {
        teleportToPlayerFront(player);
        setAttacking(true);
        this.attackTick = 0;
        this.attackTarget = player;
        this.attackAnimationState.start(this.tickCount);
        this.getNavigation().stop();

    }


    private void resetAttack() {
        setAttacking(false);
        this.attackTick = 0;
        this.attackTarget = null;
        this.attackAnimationState.stop();
    }

    
    private void executePlayer(Player player) {
        if (!player.isAlive()) return;
        dealMagicDamageToPlayer(player);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        addParticlesAroundSelf(ParticleTypes.REVERSE_PORTAL);
        if (player instanceof ServerPlayer) {
            player.sendSystemMessage(Component.translatable("entity.goetydelight.ghost_farmer.execute_message"));
        }
    }





    
    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        this.stopTrading();
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel server) {
        this.stopTrading();
        return super.changeDimension(server);
    }

    protected void addParticlesAroundSelf(ParticleOptions particleOption) {
        for (int i = 0; i < 5; ++i) {
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(particleOption, this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0), d0, d1, d2);
        }
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    protected SoundEvent getTradeUpdatedSound(boolean success) {
        return success ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.goetydelight.ghost_farmer");
    }


    
    public void onEctoplasmicMelonBreak(Player player) {
        
        markPlayerAsSuspicious(player);

        
        if (distanceToSqr(player) < 16.0D) {
            startAttack(player);
        }
    }

    
    private void markPlayerAsSuspicious(Player player) {
        
    }

    private boolean hasStolenMelon(Player player) {
        
        
        return false;
    }

    private boolean isSuspicious(Player player) {
        
        return false;
    }

    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        
    }

    private void teleportToPlayerFront(Player player) {
        
        double distance = 2.0; 
        double yaw = Math.toRadians(player.getYHeadRot());
        
        
        double x = player.getX() - Math.sin(yaw) * distance;
        double y = player.getY();
        double z = player.getZ() + Math.cos(yaw) * distance;
        
        
        x += (this.random.nextDouble() - 0.5) * 0.5;
        z += (this.random.nextDouble() - 0.5) * 0.5;
        
        
        double dx = player.getX() - x;
        double dz = player.getZ() - z;
        float yawToPlayer = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        
        
        this.moveTo(x, y, z, yawToPlayer, this.getXRot());
        
        
        this.level().addParticle(ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(), 
                0.0, 0.1, 0.0);
    }
    private void dealMagicDamageToPlayer(Player player) {
        float damage = player.getMaxHealth();
        DamageSource magicDamage = new DamageSource(this.damageSources().magic().typeHolder(), this);
        player.sendSystemMessage(Component.translatable("entity.goetydelight.ghost_farmer.attack_message"));
        player.hurt(magicDamage, damage);
    }
//    private void dealMagicDamageToPlayer(Player player) {
//        float damage = Float.MAX_VALUE;
//        DamageSource mobAttack = this.damageSources().mobAttack(this);
//        Holder<DamageType> damageTypeHolder = mobAttack.typeHolder();
//        if(damageTypeHolder instanceof Holder.Reference<DamageType> reference)
//            reference.bindTags(Set.of(DamageTypeTags.BYPASSES_ARMOR,DamageTypeTags.BYPASSES_ENCHANTMENTS,DamageTypeTags.BYPASSES_RESISTANCE));
//        player.sendSystemMessage(Component.translatable("entity.goetydelight.ghost_farmer.attack_message"));
//        player.hurt(mobAttack, damage);
//    }
//    无视护甲,附魔,抗性

    public static class GhostFarmerMerchantMenu extends MerchantMenu {
        public GhostFarmerMerchantMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, Merchant merchant) {
            super(containerId, playerInventory, merchant);
        }
    }

    
    @Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
    public static class EventHandler {
        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            
            if (event.getState().getBlock() == ModBlocks.ECTOPLASMIC_MELON_BLOCK.get()) {
                Player player = event.getPlayer();
                Level level = player.level();

                
                BlockPos breakPos = event.getPos();
                if (level instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel) level;
                    ResourceKey<Structure> structure = ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("goetydelight", "ectoplasmic_melon_field"));
                    StructureStart structureStart = serverLevel.structureManager().getStructureWithPieceAt(breakPos, structure);
                    if (structureStart == null || !structureStart.isValid()) {
                        return; 
                    }
                }

                
                AABB searchArea = new AABB(event.getPos()).inflate(16); 
                List<GhostFarmerEntity> ghostFarmers = level.getEntitiesOfClass(
                    GhostFarmerEntity.class, searchArea
                );

                
                for (GhostFarmerEntity ghostFarmer : ghostFarmers) {
                    ghostFarmer.onEctoplasmicMelonBreak(player);
                }
            }
        }

    }
//    反伤机制
//    @Override
//    public boolean hurt(DamageSource source, float amount) {
//        // 先调用父类的 hurt 方法
//        boolean hurtResult = super.hurt(source, amount);
//
//        // 如果成功受到伤害且伤害来源是实体攻击
//        if (hurtResult && source.getEntity() != null) {
//            Entity attacker = source.getEntity();
//            // 确保攻击者是玩家且不是自己
//            if (attacker instanceof Player && attacker != this) {
//                Player attackingPlayer = (Player) attacker;
//                // 调用 dealMagicDamageToPlayer 方法进行反伤
//                this.dealMagicDamageToPlayer(attackingPlayer);
//            }
//        }
//
//        return hurtResult;
//    }
//    限制伤害(hurt层)
//    @Override
//    public boolean hurt(DamageSource source, float amount) {
//        float limitedAmount = Math.min(amount, 1.0f);
//        return super.hurt(source, limitedAmount);
//    }
//    限制伤害(set层)
//    private long lastSetHealthTime = 0;
//    private static final long SET_HEALTH_COOLDOWN = 20; // 1秒冷却时间(20 ticks)
//
//    @Override
//    public void setHealth(float health) {
//        long currentTime = this.level().getGameTime();
//        if (currentTime - lastSetHealthTime < SET_HEALTH_COOLDOWN) {
//            return; // 如果还在冷却中，直接返回
//        }
//        float currentHealth = this.getHealth();
//        float damage = currentHealth - health; // 注意这里是反向计算，health < currentHealth 时为正数伤害
//        float maxHealth = this.getMaxHealth();
//        // 伤害值超过Float.MAX_VALUE时无视
//        if(damage > Float.MAX_VALUE){
//            return;
//        }
//        // 只有当伤害值超过最大生命值的10%与5之间的较小值时才限制
//        if (damage > Math.min(5,maxHealth * 0.1f)) {
//            // 限制伤害最多为最大生命值的10%与5之间的较小值
//            super.setHealth(currentHealth - Math.min(5,maxHealth * 0.1f));
//        } else {
//            // 正常设置生命值
//            super.setHealth(health);
//        }
//        lastSetHealthTime = currentTime;
//    }
//    被清除时停止交易
    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.DISCARDED || reason == RemovalReason.KILLED && this.tradingPlayer != null) {
            this.stopTrading();
        }
        super.setRemoved(reason);
    }
//    限制伤害(更强的一层，来自csdy)
//    private float currentHealth = 16.0f;
//    private boolean isDying = false;
//    private long lastSetHealthTime = 0;
//    private static final long SET_HEALTH_COOLDOWN = 20; // 1秒冷却时间(20 ticks)
//    @Override
//    public boolean hurt(@NonNull DamageSource source, float damage) {
//        if (isDying)  return false;
//        float realhurt = Math.min(10,damage);
//        if (realhurt<5 || realhurt>Float.MAX_VALUE){
//            return false;
//        }
//        long currentTime = this.level().getGameTime();
//        if (currentTime - lastSetHealthTime > SET_HEALTH_COOLDOWN) {
//            this.currentHealth -= realhurt*0.1f;
//            lastSetHealthTime = currentTime;
//        } else {
//            if(this.currentHealth < currentHealth){
//            this.currentHealth += realhurt*0.5f;
//            return true;}
//        }
//        if (this.currentHealth < 0) {
//            currentHealth = 0;
//        }
//        if(this.currentHealth<=0 && !isDying){
//            handleDeath();
//        }
//        return true;
//    }
//    private void handleDeath() {
//        if(isDying) return;
//        isDying = true;
//        DamageSource deathSource = this.damageSources().generic();
//        super.die(deathSource);
//        this.dropAllDeathLoot(deathSource);
//        super.setHealth(0);
//    }
//    @Override
//    public float getHealth() {
//        return this.currentHealth;
//    }
//    private void syncHealthToNative() {
//        this.setHealth(Math.max(0.1f, this.currentHealth));
//    }
//    禁用掉用战利品表
//    @Override
//    public void dropCustomDeathLoot(DamageSource cause,int looting, boolean recentlyHit) {
//        if(NotAllowDrop == true && isDeadOrDying() = false){
//            this.level().getServer().execute(() -> {;
//                this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5D)).forEach(Entity::discard);
//                this.level().getEntitiesOfClass(ExperienceOrb.class, this.getBoundingBox().inflate(0.5D)).forEach(Entity::discard);
//            });
//        }return;
//    }
    // 拒绝设置NoAI
//    @Override
//    public void setNoAI() {
//        if(RefuseSetNoAI == true){
//            return;
//        }
//    }
    // 防止攻击自身及主人
//    public boolean setAttackTarget(LivingEntity target) {
//        // 检查目标是否是实体本身
//        if (target == this) {
//            return false;
//        }
//        // 检查目标是否是拥有者
//        if (target instanceof Player && this.getOwnerUUID() != null) {
//            UUID ownerUUID = this.getOwnerUUID();
//            if (ownerUUID.equals(target.getUUID())) {
//                return  false;
//            }
//        }
//        return true;
//    }
    // 传送到主人位置
//    private void teleportToQwner() {
//        if (this.getOwner() != null && distanceToSqr(this.getOwner()) > 20.0F) {
//            setPos(this.getOwner().getX(), this.getOwner().getY(), this.getOwner().getZ());
//        }
//    }
    // 防止与主人碰撞
//    @Override
//    public boolean canCollideWith(Entity entity) {
//        if (entity instanceof Player && this.getOwnerUUID() != null) {
//            UUID ownerUUID = this.getOwnerUUID();
//            if (ownerUUID.equals(entity.getUUID())) {
//                return false;
//            }
//        }
//        return super.canCollideWith(entity);
//    }
}