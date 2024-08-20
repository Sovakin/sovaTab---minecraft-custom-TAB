package org.sovakin.com.sovatab;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.sovakin.com.sovatab.client.ClientEvents;
import org.sovakin.com.sovatab.client.CustomTabListRenderer;
import org.sovakin.com.sovatab.config.SovaTabConfig;
import org.sovakin.com.sovatab.network.NetworkHandler;

@Mod("sovatab")
public class SovaTab {

    public SovaTab() {
        System.out.println("SovaTab mod is initializing");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(this::clientSetup));

        SovaTabConfig.loadConfig();

        NetworkHandler.init();

        MinecraftForge.EVENT_BUS.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(ClientEvents.class));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        System.out.println("SovaTab common setup");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        System.out.println("SovaTab client setup");
        MinecraftForge.EVENT_BUS.register(CustomTabListRenderer.class);
    }
}