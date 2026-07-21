package net.v_black_cat.goetydelight.block;

import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;

import static net.v_black_cat.goetydelight.init.ModBlocks.ATTACHED_ECTOPLASMIC_MELON_STEM;
import static net.v_black_cat.goetydelight.init.ModBlocks.ECTOPLASMIC_MELON_STEM;

/**
 * 在 1.21.1 中 StemGrownBlock 已被移除，直接继承 Block
 */
public class EctoplasmicMelonBlock extends Block {
    public EctoplasmicMelonBlock(Properties p_57058_) {
        super(p_57058_);
    }

    public StemBlock getStem() {
        return (StemBlock) ECTOPLASMIC_MELON_STEM.get();
    }

    public AttachedStemBlock getAttachedStem() {
        return (AttachedStemBlock) ATTACHED_ECTOPLASMIC_MELON_STEM.get();
    }
}
