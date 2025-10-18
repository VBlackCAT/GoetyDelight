package net.v_black_cat.goetydelight.block;

import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;

import static net.v_black_cat.goetydelight.block.ModBlocks.ATTACHED_ECTOPLASMIC_MELON_STEM;
import static net.v_black_cat.goetydelight.block.ModBlocks.ECTOPLASMIC_MELON_STEM;

public class EctoplasmicMelonBlock extends StemGrownBlock {
    public EctoplasmicMelonBlock(Properties p_57058_) {
        super(p_57058_);
    }

    @Override
    public StemBlock getStem() {
        return (StemBlock)ECTOPLASMIC_MELON_STEM.get();
    }


    @Override
    public AttachedStemBlock getAttachedStem() {
        return (AttachedStemBlock) ATTACHED_ECTOPLASMIC_MELON_STEM.get();
    }
}
