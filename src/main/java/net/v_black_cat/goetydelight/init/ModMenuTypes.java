package net.v_black_cat.goetydelight.init;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, GoetyDelight.MODID);

    // 示例菜单（需替换为实际容器类）
    // public static final DeferredHolder<MenuType<?>, MenuType<YourMenu>> YOUR_MENU =
    //         MENUS.register("your_menu", () -> new MenuType<>(YourMenu::new));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}