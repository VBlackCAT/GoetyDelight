package net.v_black_cat.goetydelight.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class FalseProverbsItemModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "false_proverbs_model"), "main");
	private final ModelPart knife;
	private final ModelPart bone2;
	private final ModelPart group2;
	private final ModelPart group;

	public FalseProverbsItemModel(ModelPart root) {
		this.knife = root.getChild("knife");
		this.bone2 = this.knife.getChild("bone2");
		this.group2 = this.bone2.getChild("group2");
		this.group = this.knife.getChild("group");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition knife = partdefinition.addOrReplaceChild("knife", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition bone2 = knife.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(15, 38).addBox(-1.0F, -2.0F, -10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.15F))
		.texOffs(0, 43).addBox(-2.0F, -2.0F, -7.25F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.2F))
		.texOffs(4, 15).addBox(-2.0F, -1.5F, -1.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(15, 38).addBox(-1.0F, -2.0F, -12.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(29, 27).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.02F))
		.texOffs(0, 0).addBox(-4.0F, -1.001F, -2.5F, 8.0F, 0.001F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(29, 38).addBox(-1.5F, -1.5F, -11.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 1.0F));

		PartDefinition cube_r1 = bone2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 10).addBox(-2.0F, -2.5F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4F, 0.0F, -12.9F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r2 = bone2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(8, 47).addBox(-6.7269F, -0.995F, 4.7269F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.15F))
		.texOffs(29, 15).addBox(-10.3533F, -0.007F, 4.6033F, 5.75F, 0.002F, 5.75F, new CubeDeformation(0.0F))
		.texOffs(29, 21).addBox(-6.7723F, -0.495F, 2.7723F, 4.0F, 1.0F, 4.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, -1.005F, -0.9991F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r3 = bone2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 55).addBox(-1.0701F, -0.995F, -0.9299F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, -1.005F, -12.9991F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r4 = bone2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 51).addBox(-1.0701F, -0.995F, -0.9299F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, -1.005F, 6.0009F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r5 = bone2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(8, 51).addBox(1.7583F, -0.995F, -3.7584F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, -1.005F, 4.0009F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r6 = bone2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(43, 38).addBox(2.9486F, -0.995F, -4.0847F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(37, 46).addBox(1.9486F, -0.995F, -5.0847F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.005F, 0.0009F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r7 = bone2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(16, 44).addBox(-1.9486F, -0.995F, -6.0847F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(5.0F, -1.005F, -2.2491F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r8 = bone2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 47).addBox(-5.6905F, -0.995F, -4.3489F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1.5F, -1.005F, 4.0009F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r9 = bone2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(37, 43).addBox(-3.9486F, -0.995F, -5.0847F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(43, 38).addBox(-3.9486F, -0.995F, -4.0847F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.005F, 0.0009F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r10 = bone2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 35).addBox(-0.0514F, -0.995F, -6.0847F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-5.0F, -1.005F, -2.2491F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r11 = bone2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(28, 43).addBox(3.6905F, -0.995F, -4.3489F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-1.5F, -1.005F, 4.0009F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group2 = bone2.addOrReplaceChild("group2", CubeListBuilder.create(), PartPose.offset(-4.0F, -1.005F, 1.0009F));

		PartDefinition cube_r12 = group2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 27).addBox(-7.9486F, 0.004F, -9.0847F, 6.0F, 0.001F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r13 = group2.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(12, 27).addBox(1.9486F, 0.004F, -9.0847F, 6.0F, 0.001F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group = knife.addOrReplaceChild("group", CubeListBuilder.create().texOffs(0, 3).addBox(1.5F, 8.5F, -1.25F, -3.0F, -3.0F, -3.0F, new CubeDeformation(0.0F))
		.texOffs(4, 8).addBox(-0.5F, 6.5F, -3.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(2, 8).addBox(1.0F, 8.0F, -1.75F, -2.0F, -2.0F, -2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		knife.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}