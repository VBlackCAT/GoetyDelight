package net.v_black_cat.goetydelight.datagen;


import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.v_black_cat.goetydelight.item.ModItems;



public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, GoetyDelight.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        simpleHandHoldItem(ModItems.APOCALYPTIUM_KNIFE);
        simpleHandHoldItem(ModItems.VENOMOUS_SPIDER_KNIFE);
        simpleHandHoldItem(ModItems.SPECTRE_KNIFE);
        simpleHandHoldItem(ModItems.APOCALYPTIUM_KNIFE2);
        simpleHandHoldItem(ModItems.APOCALYPTIUM_KNIFE1);
//        simpleHandHoldItem(ModItems.CURSED_INGOT_KNIFE);
        simpleHandHoldItem(ModItems.DARK_KNIFE);

        simpleItem(ModItems.CORPSE_MAGGOT);



        simpleItem(ModItems.GOETYDELIGHT_ICON);
        simpleItem(ModItems.CAKE);
        simpleItem(ModItems.SEVEN_LEAF_PUDDING);
        simpleItem(ModItems.TAINTED_DRINK);
        simpleItem(ModItems.OMINOUS_ICE_CREAM);
        simpleItem(ModItems.ECTOPLASMIC_MELON);
        simpleItem(ModItems.CURSED_METAL_BRUSH);
        simpleItem(ModItems.CRYING_SHARK_SUGAR_PACK);
        simpleItem(ModItems.SKULL_SHOT);
        simpleItem(ModItems.REJECTED_DARK_MEAT_SOUP);
        simpleItem(ModItems.SIBLING_SUNDAE);
        simpleItem(ModItems.PROMOTION_HARD_CANDY);
        simpleItem(ModItems.NIGHT_HEART_PEA_SOUP);
        simpleItem(ModItems.CUP);
        simpleItem(ModItems.BLUE_ECTOPLASMIC_SUNDAE);
        simpleItem(ModItems.TOXIC_MEAL);
        simpleItem(ModItems.POACHED_NETHER_WART_EGG);
        simpleItem(ModItems.POACHED_SPIDER_EGG);
        simpleItem(ModItems.ECTOPLASM_JELLY);
        simpleItem(ModItems.ROASTED_CORPSE_MAGGOTS);
        simpleItem(ModItems.GRILL_FROG_LEG);
        simpleItem(ModItems.BEAR_PAW);
        simpleItem(ModItems.FRENZIED_FUNGUS_POP_ROCKS);
        simpleItem(ModItems.WHITE_SHARK_CANDY);
        simpleItem(ModItems.WHITE_SHARK_SUGAR_PACK);
        simpleItem(ModItems.CANDY_FISH);
        simpleItem(ModItems.SOUL_CONVERGENCE_ROOM);
        simpleItem(ModItems.SOUL_CONVERGENCE_ROOM_2);
        simpleItem(ModItems.GRAPE_SLUSH);
        simpleItem(ModItems.FROG_LEG_SANDWICH);
        simpleItem(ModItems.SPIDER_EGG_BUBBLE_TEA);
        simpleItem(ModItems.SPIDER_EGG_BUBBLE_TEA_2);
        simpleItem(ModItems.SAUCE_GRILLED_CANDY_FISH);
        simpleItem(ModItems.BONE_LORD_ASH_RICE);
        simpleItem(ModItems.RUBY_HARD_CANDY);
        simpleItem(ModItems.CRISP_BISCUIT);
        simpleItem(ModItems.ROTTEN_CORPSE_MAGGOT_FEAST);
        simpleItem(ModItems.SUNSHINE_SUGAR_BUN);
        simpleItem(ModItems.QUICK_GROWING_SEED_POPCORN);
        simpleItem(ModItems.NETHER_STYLE_FRIED_EGG_SANDWICH);
        simpleItem(ModItems.EXOTIC_BREAKFAST);
        simpleItem(ModItems.JUNGLE_SALAD);
        simpleItem(ModItems.BOILING_BLOOD_BREW);
        simpleItem(ModItems.CURSED_METAL_BRUSH);
        simpleItem(ModItems.DARK_BRUSH);
        simpleItem(ModItems.APOCALYPTIUM_INGOT_BRUSH);
        simpleItem(ModItems.ASCENSION_MOONCAKE);
        simpleItem(ModItems.VILLAGERS_FEAST);
        simpleItem(ModItems.WARPED_WART_OMELETTE);
        simpleItem(ModItems.NETHER_WART_OMELETTE);
        simpleItem(ModItems.FULL_SPIDER_FEAST);
        simpleItem(ModItems.LIQUID_VOID_TEA_DRINK);



        simpleBlockItem(ModBlocks.MARBLE_DOOR);
        
        evenSimplerBlockItem(ModBlocks.SILT_MARBLE_HEAVY);
        evenSimplerBlockItem(ModBlocks.MARBLE_STAIRS);
        evenSimplerBlockItem(ModBlocks.MARBLE_SLAB);
        evenSimplerBlockItem(ModBlocks.MARBLE_PRESSURE_PLATE);
        evenSimplerBlockItem(ModBlocks.MARBLE_FENCE_GATE);

        trapdoorItem(ModBlocks.MARBLE_TRAPDOOR);
    }

    private ItemModelBuilder simpleHandHoldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/handheld")).texture("layer0",
                new ResourceLocation(GoetyDelight.MODID,"item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(GoetyDelight.MODID,"item/" + item.getId().getPath()));
    }

    public void evenSimplerBlockItem(RegistryObject<Block> block) {
        this.withExistingParent(GoetyDelight.MODID + ":" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath(),
                modLoc("block/" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath()));
    }

    public void trapdoorItem(RegistryObject<Block> block) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(),
                modLoc("block/" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath() + "_bottom"));
    }

    public void fenceItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(), mcLoc("block/fence_inventory"))
                .texture("texture",  new ResourceLocation(GoetyDelight.MODID, "block/" + ForgeRegistries.BLOCKS.getKey(baseBlock.get()).getPath()));
    }

    public void buttonItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(), mcLoc("block/button_inventory"))
                .texture("texture",  new ResourceLocation(GoetyDelight.MODID, "block/" + ForgeRegistries.BLOCKS.getKey(baseBlock.get()).getPath()));
    }

    public void wallItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(), mcLoc("block/wall_inventory"))
                .texture("wall",  new ResourceLocation(GoetyDelight.MODID, "block/" + ForgeRegistries.BLOCKS.getKey(baseBlock.get()).getPath()));
    }

    private ItemModelBuilder simpleBlockItem(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(GoetyDelight.MODID,"item/" + item.getId().getPath()));
    }
}
