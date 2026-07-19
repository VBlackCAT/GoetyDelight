package net.v_black_cat.goetydelight.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.screen.NightStoveMenu;
import net.v_black_cat.goetydelight.screen.ShadeStoveMenu;
import net.v_black_cat.goetydelight.screen.CursedIngotPotMenu;

public class ModMenuTypes {
    public static final DeferredRegister<
            MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, GoetyDelight.MODID);

    public static final DeferredHolder<
            MenuType<?>,
            MenuType<
                    NightStoveMenu>> NIGHT_STOVE = MENUS.register("night_stove", () -> IMenuTypeExtension.create(NightStoveMenu
            ::new));

    public static final DeferredHolder<
            MenuType<?>,
            MenuType<
                    ShadeStoveMenu>> SHADE_STOVE = MENUS.register("shade_stove", () -> IMenuTypeExtension.create(ShadeStoveMenu
            ::new));
    public static final DeferredHolder<
            MenuType<?>,
            MenuType<
                    CursedIngotPotMenu>> CURSED_INGOT_POT = MENUS.register("cursed_ingot_pot", () -> IMenuTypeExtension.create(CursedIngotPotMenu
            ::new));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}