package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.items.magic.MagicFocus;

/**
 * 保留原物品注册 ID，仅替换玩家可见的文本 ID。
 */
public class PetroleumMistFocus extends MagicFocus {

    public static final String DESCRIPTION_ID = "item.goetydelight.petroleum_mist_focus";

    public PetroleumMistFocus(ISpell spell) {
        super(spell);
    }

    @Override
    public String getDescriptionId() {
        return DESCRIPTION_ID;
    }
}
