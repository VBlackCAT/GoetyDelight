package net.v_black_cat.goetydelight.entities.soul_lich;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class SoulLichModel<T extends SoulLichEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart head;

    private static final float TRANSITION_DURATION = 0.5F;
    private AnimationState activeAnimationState = null;
    private AnimationState previousAnimationState = null;
    private AnimationDefinition previousAnimationDefinition = null;
    private float transitionProgress = 1.0F;

    private final List<ModelPart> allParts = new ArrayList<>();
    private float[] previousPoseCache;

    public SoulLichModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("soul_lich").getChild("Head");
        collectAllParts(root);
        this.previousPoseCache = new float[allParts.size() * 6];
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition soul_lich = partdefinition.addOrReplaceChild("soul_lich", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Body = soul_lich.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(24, 25).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(28, 15).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition Head = soul_lich.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(28, 15).addBox(-2.5F, -4.0F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, -2.0F, 0.0F));
        Head.addOrReplaceChild("L_eye", CubeListBuilder.create().texOffs(36, 20).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -2.5F, 2.51F));
        Head.addOrReplaceChild("R_eye", CubeListBuilder.create().texOffs(36, 20).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -2.5F, 2.51F));
        Head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-3.5F, -3.0F, -3.5F, 7.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition Skirt = soul_lich.addOrReplaceChild("Skirt", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 3.1416F, 0.0F));
        Skirt.addOrReplaceChild("Skirt_Front", CubeListBuilder.create().texOffs(42, 7).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, -0.2007F, 0.0F, 0.0F));
        Skirt.addOrReplaceChild("Skirt_Right", CubeListBuilder.create().texOffs(30, 0).addBox(0.0F, 0.0F, -3.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2007F));
        Skirt.addOrReplaceChild("Skirt_Left", CubeListBuilder.create().texOffs(24, 35).addBox(0.0F, 0.0F, -3.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2007F));
        Skirt.addOrReplaceChild("Skirt_Back", CubeListBuilder.create().texOffs(44, 25).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.2007F, 0.0F, 0.0F));

        PartDefinition R_Arm = soul_lich.addOrReplaceChild("R_Arm", CubeListBuilder.create(), PartPose.offset(2.5F, -1.0F, 0.0F));
        PartDefinition R_Arm_Robe = R_Arm.addOrReplaceChild("R_Arm_Robe", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        R_Arm_Robe.addOrReplaceChild("R_Arm_r1", CubeListBuilder.create().texOffs(40, 44).addBox(2.5F, -11.6F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 11.0F, 0.0F, 0.0F, 0.1745F, -0.0873F));
        R_Arm_Robe.addOrReplaceChild("R_Arm_Robe_r1", CubeListBuilder.create().texOffs(12, 44).addBox(3.0F, -11.8F, -1.5F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 11.0F, 0.0F, 0.0F, 0.1745F, -0.0873F));
        R_Arm.addOrReplaceChild("R_scapula", CubeListBuilder.create().texOffs(42, 0).addBox(-1.5F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, -1.0F, 0.0F));

        PartDefinition weapon = R_Arm.addOrReplaceChild("weapon", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, 6.5F, 0.0F, 0.0F, 3.1416F, 0.0F));
        weapon.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(20, 41).mirror().addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.0F, -9.5F, 3.1416F, 0.0F, 0.0F));
        weapon.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(16, 41).mirror().addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(38, 13).mirror().addBox(-2.5F, -0.5F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 0.0F, -9.5F, 3.1416F, 0.0F, 0.0F));
        weapon.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(38, 11).mirror().addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, -9.5F, 3.1416F, 0.0F, 0.0F));
        weapon.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(16, 37).mirror().addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -10.0F, 3.1416F, 0.0F, 0.0F));
        weapon.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(30, 11).mirror().addBox(-1.5F, -1.5F, -2.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -8.5F, 3.1416F, 0.0F, 0.0F));
        weapon.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, -0.5F, -9.0F, 1.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 3.1416F, 0.0F, 0.0F));

        PartDefinition L_Arm = soul_lich.addOrReplaceChild("L_Arm", CubeListBuilder.create(), PartPose.offset(-2.5F, -1.0F, 0.0F));
        PartDefinition L_Arm_Robe = L_Arm.addOrReplaceChild("L_Arm_Robe", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        L_Arm_Robe.addOrReplaceChild("L_Arm_r1", CubeListBuilder.create().texOffs(36, 44).addBox(-3.5F, -11.6F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 11.0F, 0.0F, 0.0F, -0.1745F, 0.0873F));
        L_Arm_Robe.addOrReplaceChild("L_Arm_Robe_r1", CubeListBuilder.create().texOffs(0, 44).addBox(-5.0F, -11.8F, -1.5F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 11.0F, 0.0F, 0.0F, -0.1745F, 0.0873F));
        L_Arm.addOrReplaceChild("L_scapula", CubeListBuilder.create().texOffs(0, 37).addBox(-1.5F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, -1.0F, 0.0F));

        soul_lich.addOrReplaceChild("Legs", CubeListBuilder.create().texOffs(36, 35).addBox(-2.0F, -2.5F, -1.5F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    private void collectAllParts(ModelPart root) {
        root.getAllParts().forEach(allParts::add);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean isAttacking = entity.isAttacking();
        boolean isMoving = limbSwingAmount > 0.1F;

        AnimationState targetState;
        AnimationDefinition targetDefinition;

        if (isAttacking) {
            targetState = entity.attackAnimationState;
            targetDefinition = SoulLichAnimation.attacking;
        } else if (isMoving) {
            targetState = entity.walkAnimationState;
            targetDefinition = SoulLichAnimation.walk;
        } else {
            targetState = entity.idleAnimationState;
            targetDefinition = SoulLichAnimation.waiting;
        }

        if (activeAnimationState != targetState) {
            previousAnimationDefinition = getDefinitionForState(activeAnimationState, entity);
            previousAnimationState = activeAnimationState;
            activeAnimationState = targetState;
            transitionProgress = 0.0F;

            if (!targetState.isStarted()) {
                targetState.start((int) ageInTicks);
            }
        }

        if (transitionProgress < 1.0F && previousAnimationState != null && previousAnimationDefinition != null) {
            transitionProgress += 1.0F / (TRANSITION_DURATION * 20.0F);
            transitionProgress = Math.min(transitionProgress, 1.0F);
            applyTransitionPose(transitionProgress, previousAnimationState, previousAnimationDefinition,
                    targetState, targetDefinition, ageInTicks);
        } else {
            animate(targetState, targetDefinition, ageInTicks, 1.0F);
        }

        this.head.yRot = -(netHeadYaw * ((float) Math.PI / 180F));
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }

    private AnimationDefinition getDefinitionForState(AnimationState state, T entity) {
        if (state == entity.attackAnimationState) return SoulLichAnimation.attacking;
        if (state == entity.walkAnimationState) return SoulLichAnimation.walk;
        return SoulLichAnimation.waiting;
    }

    private void applyTransitionPose(float progress, AnimationState prevState, AnimationDefinition prevDef,
                                     AnimationState targetState, AnimationDefinition targetDef, float ageInTicks) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        animate(prevState, prevDef, ageInTicks, 1.0F);
        snapshotPoseToCache();

        this.root().getAllParts().forEach(ModelPart::resetPose);
        animate(targetState, targetDef, ageInTicks, 1.0F);
        interpolateFromCache(progress);
    }

    private void snapshotPoseToCache() {
        for (int i = 0; i < allParts.size(); i++) {
            ModelPart part = allParts.get(i);
            int offset = i * 6;
            previousPoseCache[offset] = part.xRot;
            previousPoseCache[offset + 1] = part.yRot;
            previousPoseCache[offset + 2] = part.zRot;
            previousPoseCache[offset + 3] = part.x;
            previousPoseCache[offset + 4] = part.y;
            previousPoseCache[offset + 5] = part.z;
        }
    }

    private void interpolateFromCache(float progress) {
        for (int i = 0; i < allParts.size(); i++) {
            ModelPart part = allParts.get(i);
            int offset = i * 6;
            part.xRot = Mth.lerp(progress, previousPoseCache[offset], part.xRot);
            part.yRot = Mth.lerp(progress, previousPoseCache[offset + 1], part.yRot);
            part.zRot = Mth.lerp(progress, previousPoseCache[offset + 2], part.zRot);
            part.x = Mth.lerp(progress, previousPoseCache[offset + 3], part.x);
            part.y = Mth.lerp(progress, previousPoseCache[offset + 4], part.y);
            part.z = Mth.lerp(progress, previousPoseCache[offset + 5], part.z);
        }
    }
}
