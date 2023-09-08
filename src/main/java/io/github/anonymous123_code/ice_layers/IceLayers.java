package io.github.anonymous123_code.ice_layers;

import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IceLayers implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod name as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("Ice Layers");
	public static final Block ICE_LAYER =
		new IceLayerBlock(
			QuiltBlockSettings.create()
					.mapColor(MapColor.ICE)
					.slipperiness(0.98F)
					.ticksRandomly()
					.strength(0.5F)
					.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.solid(false)
					.nonSolid(true)
					.solidBlock(Blocks::never)
		);

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());
		Registry.register(Registries.BLOCK, new Identifier(mod.metadata().id(), "ice_layer"), ICE_LAYER);
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "ice_layer"), new BlockItem(ICE_LAYER, new QuiltItemSettings()));
	}
}
