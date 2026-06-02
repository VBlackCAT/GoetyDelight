package net.v_black_cat.goetydelight.entities.soul_lich;// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class SoulLichModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	private final ModelPart soul_lich;
	private final ModelPart Body;
	private final ModelPart Head;
	private final ModelPart L_eye;
	private final ModelPart R_eye;
	private final ModelPart hat;
	private final ModelPart Skirt;
	private final ModelPart Skirt_Front;
	private final ModelPart Skirt_Right;
	private final ModelPart Skirt_Left;
	private final ModelPart Skirt_Back;
	private final ModelPart R_Arm;
	private final ModelPart R_Arm_Robe;
	private final ModelPart R_scapula;
	private final ModelPart weapon;
	private final ModelPart L_Arm;
	private final ModelPart L_Arm_Robe;
	private final ModelPart L_scapula;
	private final ModelPart Legs;

	public SoulLichModel(ModelPart root) {
		this.soul_lich = root.getChild("soul_lich");
		this.Body = this.soul_lich.getChild("Body");
		this.Head = this.soul_lich.getChild("Head");
		this.L_eye = this.Head.getChild("L_eye");
		this.R_eye = this.Head.getChild("R_eye");
		this.hat = this.Head.getChild("hat");
		this.Skirt = this.soul_lich.getChild("Skirt");
		this.Skirt_Front = this.Skirt.getChild("Skirt_Front");
		this.Skirt_Right = this.Skirt.getChild("Skirt_Right");
		this.Skirt_Left = this.Skirt.getChild("Skirt_Left");
		this.Skirt_Back = this.Skirt.getChild("Skirt_Back");
		this.R_Arm = this.soul_lich.getChild("R_Arm");
		this.R_Arm_Robe = this.R_Arm.getChild("R_Arm_Robe");
		this.R_scapula = this.R_Arm.getChild("R_scapula");
		this.weapon = this.R_Arm.getChild("weapon");
		this.L_Arm = this.soul_lich.getChild("L_Arm");
		this.L_Arm_Robe = this.L_Arm.getChild("L_Arm_Robe");
		this.L_scapula = this.L_Arm.getChild("L_scapula");
		this.Legs = this.soul_lich.getChild("Legs");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition soul_lich = partdefinition.addOrReplaceChild("soul_lich", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Body = soul_lich.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(24, 25).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(28, 15).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition Head = soul_lich.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(28, 15).addBox(-2.5F, -4.0F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition L_eye = Head.addOrReplaceChild("L_eye", CubeListBuilder.create().texOffs(36, 20).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -2.5F, 2.51F));

		PartDefinition R_eye = Head.addOrReplaceChild("R_eye", CubeListBuilder.create().texOffs(36, 20).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -2.5F, 2.51F));

		PartDefinition hat = Head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(-3.5F, -3.0F, -3.5F, 7.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition Skirt = soul_lich.addOrReplaceChild("Skirt", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Skirt_Front = Skirt.addOrReplaceChild("Skirt_Front", CubeListBuilder.create().texOffs(42, 7).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, -0.2007F, 0.0F, 0.0F));

		PartDefinition Skirt_Right = Skirt.addOrReplaceChild("Skirt_Right", CubeListBuilder.create().texOffs(30, 0).addBox(0.0F, 0.0F, -3.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2007F));

		PartDefinition Skirt_Left = Skirt.addOrReplaceChild("Skirt_Left", CubeListBuilder.create().texOffs(24, 35).addBox(0.0F, 0.0F, -3.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2007F));

		PartDefinition Skirt_Back = Skirt.addOrReplaceChild("Skirt_Back", CubeListBuilder.create().texOffs(44, 25).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.2007F, 0.0F, 0.0F));

		PartDefinition R_Arm = soul_lich.addOrReplaceChild("R_Arm", CubeListBuilder.create(), PartPose.offset(2.5F, -1.0F, 0.0F));

		PartDefinition R_Arm_Robe = R_Arm.addOrReplaceChild("R_Arm_Robe", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition R_Arm_r1 = R_Arm_Robe.addOrReplaceChild("R_Arm_r1", CubeListBuilder.create().texOffs(40, 44).addBox(2.5F, -11.6F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 11.0F, 0.0F, 0.0F, 0.1745F, -0.0873F));

		PartDefinition R_Arm_Robe_r1 = R_Arm_Robe.addOrReplaceChild("R_Arm_Robe_r1", CubeListBuilder.create().texOffs(12, 44).addBox(3.0F, -11.8F, -1.5F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 11.0F, 0.0F, 0.0F, 0.1745F, -0.0873F));

		PartDefinition R_scapula = R_Arm.addOrReplaceChild("R_scapula", CubeListBuilder.create().texOffs(42, 0).addBox(-1.5F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, -1.0F, 0.0F));

		PartDefinition weapon = R_Arm.addOrReplaceChild("weapon", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, 6.5F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r1 = weapon.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(20, 41).mirror().addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.0F, -9.5F, 3.1416F, 0.0F, 0.0F));

		PartDefinition cube_r2 = weapon.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(16, 41).mirror().addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(38, 13).mirror().addBox(-2.5F, -0.5F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 0.0F, -9.5F, 3.1416F, 0.0F, 0.0F));

		PartDefinition cube_r3 = weapon.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(38, 11).mirror().addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, -9.5F, 3.1416F, 0.0F, 0.0F));

		PartDefinition cube_r4 = weapon.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(16, 37).mirror().addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -10.0F, 3.1416F, 0.0F, 0.0F));

		PartDefinition cube_r5 = weapon.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(30, 11).mirror().addBox(-1.5F, -1.5F, -2.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -8.5F, 3.1416F, 0.0F, 0.0F));

		PartDefinition cube_r6 = weapon.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, -0.5F, -9.0F, 1.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 3.1416F, 0.0F, 0.0F));

		PartDefinition L_Arm = soul_lich.addOrReplaceChild("L_Arm", CubeListBuilder.create(), PartPose.offset(-2.5F, -1.0F, 0.0F));

		PartDefinition L_Arm_Robe = L_Arm.addOrReplaceChild("L_Arm_Robe", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition L_Arm_r1 = L_Arm_Robe.addOrReplaceChild("L_Arm_r1", CubeListBuilder.create().texOffs(36, 44).addBox(-3.5F, -11.6F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 11.0F, 0.0F, 0.0F, -0.1745F, 0.0873F));

		PartDefinition L_Arm_Robe_r1 = L_Arm_Robe.addOrReplaceChild("L_Arm_Robe_r1", CubeListBuilder.create().texOffs(0, 44).addBox(-5.0F, -11.8F, -1.5F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 11.0F, 0.0F, 0.0F, -0.1745F, 0.0873F));

		PartDefinition L_scapula = L_Arm.addOrReplaceChild("L_scapula", CubeListBuilder.create().texOffs(0, 37).addBox(-1.5F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, -1.0F, 0.0F));

		PartDefinition Legs = soul_lich.addOrReplaceChild("Legs", CubeListBuilder.create().texOffs(36, 35).addBox(-2.0F, -2.5F, -1.5F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.5F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		soul_lich.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}