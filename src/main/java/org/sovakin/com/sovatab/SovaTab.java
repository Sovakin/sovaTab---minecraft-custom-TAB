package org.sovakin.com.sovatab;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.sovakin.com.sovatab.config.SovaTabConfig;

@Mod("sovatab")
public class SovaTab {
    public SovaTab() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::doClientStuff);
        }
        MinecraftForge.EVENT_BUS.register(this);
        SovaTabConfig.loadConfig();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new org.sovakin.com.sovatab.client.CustomTabListRenderer());
    }

    private void setup(final FMLCommonSetupEvent event) {
    }
}