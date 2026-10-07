package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class ApollyonCakeRenderer
        implements BlockEntityRenderer<ApollyonCakeBlockEntity> {

    private final ApollyonCakeModel model;

    public ApollyonCakeRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        this.model = new ApollyonCakeModel();
    }

    @Override
    public void render(
            ApollyonCakeBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {

        BlockState state = blockEntity.getBlockState();

        /*
         * ============================
         * 动画时间
         * ============================
         */

        long gameTime = 0L;

        if (blockEntity.getLevel() != null) {
            gameTime = blockEntity.getLevel().getGameTime();
        }

        float time = gameTime + partialTick;

        /*
         * 720 ticks = 360°
         */
        float angle = (time % 720.0F) * 0.5F;

        /*
         * The End 中不旋转。
         */
        if (state.getValue(ApollyonCakeBlock.IS_THE_END)) {
            angle = 0.0F;
        }

        /*
         * ============================
         * 当前方块朝向
         * ============================
         */

        Direction facing =
                state.getValue(ApollyonCakeBlock.FACING);

        /*
         * ============================
         * Block Atlas
         * ============================
         */

        VertexConsumer consumer =
                buffer.getBuffer(
                        RenderType.entityCutoutNoCull(
                                TextureAtlas.LOCATION_BLOCKS
                        )
                );

        poseStack.pushPose();

        /*
         * ==========================================================
         * 让整个蛋糕跟随 BlockState.FACING
         * ==========================================================
         *
         * 模型 JSON 的默认方向视为 NORTH。
         *
         * 然后根据 FACING：
         *
         * NORTH = 0°
         * EAST  = 90°
         * SOUTH = 180°
         * WEST  = 270°
         *
         * 绕方块中心旋转。
         */

        float blockRotation = getBlockRotation(facing);

        poseStack.translate(
                0.5F,
                0.0F,
                0.5F
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(blockRotation)
        );

        poseStack.translate(
                -0.5F,
                0.0F,
                -0.5F
        );

        /*
         * ==========================================================
         * 静态部分
         * ==========================================================
         */

        model.renderStatic(
                poseStack,
                consumer,
                1.0F,
                1.0F,
                1.0F,
                1.0F,
                packedLight,
                packedOverlay
        );

        /*
         * ==========================================================
         * 动态环的法线
         * ==========================================================
         *
         * model.getAnimatedNormal()
         *
         * 是 JSON / FaceBakery 得到的：
         *
         * MODEL SPACE NORMAL
         *
         * 现在需要把它根据 FACING 转换成：
         *
         * WORLD SPACE NORMAL
         */

        Vector3f worldNormal =
                rotateNormalForFacing(
                        model.getAnimatedNormal(),
                        facing
                );

        /*
         * ==========================================================
         * 动态环
         * ==========================================================
         */

        model.renderAnimated(
                poseStack,
                consumer,
                angle,
                worldNormal,
                1.0F,
                1.0F,
                1.0F,
                1.0F,
                packedLight,
                packedOverlay
        );

        poseStack.popPose();
    }

    /**
     * BlockState 的 FACING → Y 轴旋转角度。
     *
     * 默认模型方向：NORTH
     */
    private static float getBlockRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 0.0F;
            case EAST  -> 270.0F;
            case SOUTH -> 180.0F;
            case WEST  -> 90.0F;
            default -> 0.0F;
        };
    }

    /**
     * 将模型空间的法线根据方块 FACING
     * 转换成世界空间法线。
     */
    private static Vector3f rotateNormalForFacing(
            Vector3f modelNormal,
            Direction facing
    ) {

        Vector3f normal = new Vector3f(modelNormal);

        float rotation = getBlockRotation(facing);

        /*
         * 这里必须和上面对整个模型的
         * Y 轴旋转完全一致。
         */
        normal.rotateY(
                (float) Math.toRadians(rotation)
        );

        return normal.normalize();
    }
}