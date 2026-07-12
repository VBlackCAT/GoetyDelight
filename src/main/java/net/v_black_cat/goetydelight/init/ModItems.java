package net.v_black_cat.goetydelight.init;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModItems {
    // 创建专属于物品的 DeferredRegister，使用模组主类的 MODID
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoetyDelight.MODID);

    // 示例物品：一个普通的物品（无特殊属性）
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem(
            "example_item",
            new Item.Properties()
    );

    // 示例食物物品
    public static final DeferredItem<Item> EXAMPLE_FOOD = ITEMS.registerSimpleItem(
            "example_food",
            new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(4).saturationModifier(0.5f).build())
    );

    // 注册方法：供主类调用，将 DeferredRegister 绑定到 mod 事件总线
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}