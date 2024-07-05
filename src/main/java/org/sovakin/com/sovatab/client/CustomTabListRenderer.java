package com.yourname.modid.client;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class CustomTabListRenderer {

    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        // Проверяем, что рендеринг происходит на клиентской стороне и есть подключение к серверу
        if (mc.level != null && mc.getConnection() != null) {
            renderCustomTabList(event.getGuiGraphics());
        }
    }

    private static void renderCustomTabList(GuiGraphics guiGraphics) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return;
        }

        // Преобразуем Collection в List
        List<PlayerInfo> players = new ArrayList<>(connection.getOnlinePlayers());
        Font font = mc.font;
        int width = 200;
        int height = 20 + players.size() * 10;
        int x = (mc.getWindow().getGuiScaledWidth() - width) / 2;
        int y = 10;

        // Фон
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.5F);
        guiGraphics.fill(x, y, x + width, y + height, 0x80000000);

        // Заголовок
        String header = "Tab Header";
        guiGraphics.drawCenteredString(font, Component.literal(header), x + width / 2, y + 5, 0xFF0000);

        // Игроки
        y += 20;
        for (PlayerInfo playerInfo : players) {
            String playerName = playerInfo.getProfile().getName();
            guiGraphics.drawString(font, Component.literal(playerName), x + 10, y, 0xFFFFFF);
            y += 10;
        }

        // Нижняя строка
        String footer = "Players online: " + players.size() + " / " + connection.getOnlinePlayers().size();
        guiGraphics.drawCenteredString(font, Component.literal(footer), x + width / 2, y, 0xFFFF00);
    }
}