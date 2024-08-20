package org.sovakin.com.sovatab.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sovakin.com.sovatab.network.NetworkHandler;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.tickCount % (20 * 5) == 0) { // Каждые 5 секунд
                NetworkHandler.INSTANCE.sendToServer(new NetworkHandler.RequestPrefixesPacket(mc.player.getGameProfile().getName()));
            }
        }
    }
}