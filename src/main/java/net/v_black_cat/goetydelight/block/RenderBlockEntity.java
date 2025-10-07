package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.render.test.ModShaderRendererHelper;

public class RenderBlockEntity extends BlockEntity {

    public RenderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RENDER_BLOCK.get(), pos, state);
    }

    // 获取着色器时间（基于游戏时间）
    public float getShaderTime() {
        if (level == null) return 0;
        return (level.getGameTime() + ModShaderRendererHelper.getPartialTick()) / 20.0f;
    }
}