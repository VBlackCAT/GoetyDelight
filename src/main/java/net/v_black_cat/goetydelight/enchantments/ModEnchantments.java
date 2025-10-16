package net.v_black_cat.goetydelight.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "goetydelight", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, "goetydelight");

    /*
    public static final RegistryObject<Enchantment> FROST_ASPECT =
            ENCHANTMENTS.register("frost_aspect",
                    () -> new FrostAspectEnchantment(
                            Enchantment.Rarity.UNCOMMON,
                            EnchantmentCategory.WEAPON,
                            EquipmentSlot.MAINHAND
                    ));

    */
    public static final RegistryObject<Enchantment> SOUL_MENDING =
            ENCHANTMENTS.register("soul_mending",
                    () -> new SoulMendingEnchantment(
                            Enchantment.Rarity.VERY_RARE,
                            EnchantmentCategory.BREAKABLE,
                            EquipmentSlot.values()
                    ));


}