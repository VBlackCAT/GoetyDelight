package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.render.test.ModShaderRendererHelper;

import static net.v_black_cat.goetydelight.init.ModBlockEntities.RENDER_BLOCK;

public class RenderBlockEntity extends BlockEntity {

    public RenderBlockEntity(BlockPos pos, BlockState state) {
        super(RENDER_BLOCK.get(), pos, state);
    }

    public float getShaderTime() {
        if (level == null) return 0;
        return (level.getGameTime() + ModShaderRendererHelper.getPartialTick()) / 20.0f;
    }
}
