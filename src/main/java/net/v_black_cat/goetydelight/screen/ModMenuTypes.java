package net.v_black_cat.goetydelight.screen;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, GoetyDelight.MODID);

    public static final RegistryObject<MenuType<CursedIngotPotMenu>> CURSED_INGOT_POT =
            registerMenuType("cursed_ingot_pot", CursedIngotPotMenu::new);

    public static final RegistryObject<MenuType<ShadeStoveMenu>> SHADE_STOVE =
            registerMenuType("shade_stove", ShadeStoveMenu::new);


    public static final RegistryObject<MenuType<NightStoveMenu>> NIGHT_STOVE =
            registerMenuType("night_stove", NightStoveMenu::new);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    public static String getTranslatedString(String key, String... args) {
        return I18n.get(GoetyDelight.MODID + "." + key, (Object[]) args);
    }
}