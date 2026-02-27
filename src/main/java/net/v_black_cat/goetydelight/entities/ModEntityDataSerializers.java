package net.v_black_cat.goetydelight.entities;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.ArrayList;
import java.util.List;

public class ModEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, GoetyDelight.MODID);

    public static final RegistryObject<EntityDataSerializer<List<ItemStack>>> ITEM_STACK_LIST =
            ENTITY_DATA_SERIALIZERS.register("goetydelight_item_stack_list", () ->
                    new EntityDataSerializer<List<ItemStack>>() {
                        @Override
                        public void write(FriendlyByteBuf buffer, List<ItemStack> list) {
                            buffer.writeVarInt(list.size());
                            for (ItemStack stack : list) {
                                buffer.writeItem(stack);
                            }
                        }

                        @Override
                        public List<ItemStack> read(FriendlyByteBuf buffer) {
                            int size = buffer.readVarInt();
                            List<ItemStack> list = new ArrayList<>(size);
                            for (int i = 0; i < size; i++) {
                                list.add(buffer.readItem());
                            }
                            return list;
                        }

                        @Override
                        public List<ItemStack> copy(List<ItemStack> list) {
                            List<ItemStack> copy = new ArrayList<>(list.size());
                            for (ItemStack stack : list) {
                                copy.add(stack.copy());
                            }
                            return copy;
                        }
                    });

    public static void register(IEventBus eventBus) {
        ENTITY_DATA_SERIALIZERS.register(eventBus);
    }
}