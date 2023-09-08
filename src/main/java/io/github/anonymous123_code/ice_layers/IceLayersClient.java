package io.github.anonymous123_code.ice_layers;

import net.minecraft.client.render.RenderLayer;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

public class IceLayersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        BlockRenderLayerMap.put(RenderLayer.getTranslucent(), IceLayers.ICE_LAYER);
    }
}
