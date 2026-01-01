package com.Interest.Doll;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, easydoll.MOD_ID);

    public static final RegistryObject<BlockEntityType<PlushieBlockEntity>> PLUSHIE_BE =
            BLOCK_ENTITIES.register("plushie_be", () ->
                    BlockEntityType.Builder.of(PlushieBlockEntity::new, easydoll.PLUSHIE_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}