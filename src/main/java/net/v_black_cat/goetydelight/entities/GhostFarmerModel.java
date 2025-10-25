package net.v_black_cat.goetydelight.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.v_black_cat.goetydelight.entities.GhostFarmerEntity;
import net.v_black_cat.goetydelight.entities.ModAnimationDefinitions;

public class GhostFarmerModel<T extends Entity> extends HierarchicalModel<T> {

	private final ModelPart wraith;
	private final ModelPart handl;
	private final ModelPart robel;
	private final ModelPart handr;
	private final ModelPart rober;
	private final ModelPart head;
	private final ModelPart hat;
	private final ModelPart robehead;
	private final ModelPart bodyskeleton;
	private final ModelPart skull;

	public GhostFarmerModel(ModelPart root) {
		this.wraith = root.getChild("wraith");
		this.handl = this.wraith.getChild("handl");
		this.robel = this.handl.getChild("robel");
		this.handr = this.wraith.getChild("handr");
		this.rober = this.handr.getChild("rober");
		this.head = this.wraith.getChild("head");
		this.hat = this.head.getChild("hat");
		this.robehead = this.head.getChild("robehead");
		this.bodyskeleton = this.wraith.getChild("bodyskeleton");
		this.skull = this.bodyskeleton.getChild("skull");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition wraith = partdefinition.addOrReplaceChild("wraith", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 1.0F));

		PartDefinition handl = wraith.addOrReplaceChild("handl", CubeListBuilder.create().texOffs(56, 26).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -15.0F, -1.0F));

		PartDefinition robel = handl.addOrReplaceChild("robel", CubeListBuilder.create().texOffs(24, 52).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition handr = wraith.addOrReplaceChild("handr", CubeListBuilder.create().texOffs(56, 52).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -15.0F, -1.0F));

		PartDefinition rober = handr.addOrReplaceChild("rober", CubeListBuilder.create().texOffs(40, 52).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = wraith.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 41).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -18.0F, -1.0F));

		PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, 0.0F, -6.0F, 16.0F, 0.01F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(32, 40).addBox(-7.0F, -4.0F, -2.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -6.0F, -2.0F));

		PartDefinition robehead = head.addOrReplaceChild("robehead", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bodyskeleton = wraith.addOrReplaceChild("bodyskeleton", CubeListBuilder.create().texOffs(32, 16).addBox(-4.0F, 0.0F, 1.0F, 8.0F, 20.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -19.0F, -4.0F));

		PartDefinition skull = bodyskeleton.addOrReplaceChild("skull", CubeListBuilder.create().texOffs(56, 21).addBox(-3.0F, 3.95F, -3.5F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 54).addBox(-3.0F, -0.05F, -3.5F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(20, 54).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(56, 16).addBox(-3.0F, 1.95F, -3.5F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 4.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		if (entity instanceof GhostFarmerEntity) {
			GhostFarmerEntity ghostFarmer = (GhostFarmerEntity) entity;

			// 使用新的动画系统
			this.animateWalk(ModAnimationDefinitions.ANIMATION_GHOST_FARMER_IDLE, limbSwing, limbSwingAmount, 2f, 2.5f);
			this.animate(ghostFarmer.idleAnimationState, ModAnimationDefinitions.ANIMATION_GHOST_FARMER_IDLE, ageInTicks, 1f);
			this.animate(ghostFarmer.attackAnimationState, ModAnimationDefinitions.ANIMATION_GHOST_FARMER_ATTACK, ageInTicks, 1f);
		}
	}
	
	private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
		pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
		pHeadPitch = Mth.clamp(pHeadPitch, -25.0F, 45.0F);

		this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		wraith.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return wraith;
	}
}