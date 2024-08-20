package org.sovakin.com.sovatab.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sovakin.com.sovatab.config.SovaTabConfig;
import com.mojang.blaze3d.platform.NativeImage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class CustomTabListRenderer {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ResourceLocation DEFAULT_SKIN = DefaultPlayerSkin.getDefaultSkin();
    private static final int MAX_HEIGHT = 300;
    private static final int DEFAULT_COLUMN_WIDTH = 100;
    private static final Map<String, ResourceLocation> SKIN_CACHE = new WeakHashMap<>();
    private static final int backgroundColor = 0xB0D3D3D3;
    private static final int playerBackgroundColor = 0x80FFFFFF;
    private static final int padding = 10;
    private static final int headSize = 14;
    private static Map<String, String> playerPrefixes = new HashMap<>();

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_LIST.type()) {
            return;
        }
        if (!mc.options.keyPlayerList.isDown()) {
            return;
        }
        event.setCanceled(true);
        renderCustomTabList(event.getGuiGraphics());
    }

    private static void renderCustomTabList(GuiGraphics guiGraphics) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return;
        }

        List<PlayerInfo> players = new ArrayList<>(connection.getOnlinePlayers());
        Font font = mc.font;

        Map<PlayerInfo, Integer> nameWidths = new HashMap<>();
        for (PlayerInfo player : players) {
            String displayName = getDisplayName(player);
            nameWidths.put(player, font.width(displayName));
        }

        String header = SovaTabConfig.header;
        int headerWidth = getWidthForColoredText(font, header);

        int maxPlayers = SovaTabConfig.maxPlayers;
        int maxNameWidth = font.width("Онлайн сервера: " + players.size() + " / " + maxPlayers);
        for (PlayerInfo player : players) {
            int nameWidth = nameWidths.get(player) + headSize + 5 + padding * 2;
            if (nameWidth > maxNameWidth) {
                maxNameWidth = nameWidth;
            }
        }

        int columnWidth = Math.max(DEFAULT_COLUMN_WIDTH, Math.max(headerWidth, maxNameWidth));

        int columns = (int) Math.ceil((double) players.size() / (MAX_HEIGHT / 20.0));
        int playersPerColumn = (int) Math.ceil((double) players.size() / columns);
        int width = columns * columnWidth;
        int height = 20 + playersPerColumn * 20 + 20;
        int x = (mc.getWindow().getGuiScaledWidth() - width) / 2;
        int y = 10;

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.fillGradient(x, y, x + width, y + height, 0x50000000, 0x50000000);

        drawColoredText(guiGraphics, font, header, x + width / 2, y + 5);

        y += 20;
        int column = 0;

        int startX = x + (columnWidth - maxNameWidth) / 2;

        for (int i = 0; i < players.size(); i++) {
            if (i % playersPerColumn == 0 && i != 0) {
                column++;
                y = 30;
                startX = x + column * columnWidth + (columnWidth - maxNameWidth) / 2;
            }
            PlayerInfo playerInfo = players.get(i);
            String playerName = playerInfo.getProfile().getName();

            int headX = startX + padding;
            int headY = y;

            int playerBackgroundWidth = maxNameWidth;
            int playerBackgroundX1 = startX;
            int playerBackgroundX2 = playerBackgroundX1 + playerBackgroundWidth;
            int playerBackgroundY1 = y - 2;
            int playerBackgroundY2 = y + headSize + 2;
            guiGraphics.fillGradient(playerBackgroundX1, playerBackgroundY1, playerBackgroundX2, playerBackgroundY2, playerBackgroundColor, playerBackgroundColor);

            ResourceLocation skinLocation = getSkinResourceLocation(playerName);

            RenderSystem.setShaderTexture(0, skinLocation);
            guiGraphics.blit(skinLocation, headX, headY, headSize, headSize, 0.0F, 0.0F, 16, 16, 16, 16);

            String displayName = getDisplayName(playerInfo);
            guiGraphics.drawString(font, Component.literal(displayName), headX + headSize + 5, headY + (headSize / 2 - 4), 0xFFFFFF);
            y += 20;
        }

        y = 30 + playersPerColumn * 20 + 10;
        String footer = "Онлайн сервера: " + players.size() + " / " + maxPlayers;
        guiGraphics.drawCenteredString(font, Component.literal(footer), x + width / 2, y, parseColor(SovaTabConfig.footerColor));

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
    }

    private static String getDisplayName(PlayerInfo playerInfo) {
        String playerName = playerInfo.getProfile().getName();
        String prefix = playerPrefixes.getOrDefault(playerName, "");
        System.out.println("Getting display name for " + playerName + ": prefix = " + prefix);
        return prefix + playerName;
    }

    private static ResourceLocation getSkinResourceLocation(String playerName) {
        String url = "https://twitchworld.ru/texture-provider/AVATAR/256/" + playerName;
        ResourceLocation resourceLocation = new ResourceLocation("custom_skins", playerName.toLowerCase(Locale.ROOT));

        if (!SKIN_CACHE.containsKey(playerName)) {
            try (InputStream in = new URL(url).openStream()) {
                NativeImage image = NativeImage.read(in);
                DynamicTexture dynamicTexture = new DynamicTexture(image);
                mc.getTextureManager().register(resourceLocation, dynamicTexture);
                SKIN_CACHE.put(playerName, resourceLocation);
            } catch (IOException e) {
                SKIN_CACHE.put(playerName, DEFAULT_SKIN);
            }
        }

        return SKIN_CACHE.getOrDefault(playerName, DEFAULT_SKIN);
    }

    private static int parseColor(String hexColor) {
        try {
            if (hexColor.startsWith("#")) {
                hexColor = hexColor.substring(1);
            }
            return Integer.parseInt(hexColor, 16) + 0xFF000000;
        } catch (NumberFormatException e) {
            return 0xFFFFFFFF;
        }
    }

    private static int getWidthForColoredText(Font font, String text) {
        Pattern pattern = Pattern.compile("\\[(#[0-9a-fA-F]{6})\\]([^\\[]*)");
        Matcher matcher = pattern.matcher(text);
        int width = 0;

        while (matcher.find()) {
            String content = matcher.group(2);
            width += font.width(content);
        }

        return width;
    }

    private static void drawColoredText(GuiGraphics guiGraphics, Font font, String text, int x, int y) {
        Pattern pattern = Pattern.compile("\\[(#[0-9a-fA-F]{6})\\]([^\\[]*)");
        Matcher matcher = pattern.matcher(text);
        int currentX = x - getWidthForColoredText(font, text) / 2;

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            String content = matcher.group(2);
            int colorCode = parseColor(hexColor);
            guiGraphics.drawString(font, Component.literal(content), currentX, y, colorCode);
            currentX += font.width(content);
        }
    }

    public static void updatePrefixes(String[] playerNames, String[] prefixes) {
        System.out.println("CustomTabListRenderer: Updating prefixes");
        System.out.println("Players: " + Arrays.toString(playerNames));
        System.out.println("Prefixes: " + Arrays.toString(prefixes));
        playerPrefixes.clear();
        for (int i = 0; i < playerNames.length; i++) {
            playerPrefixes.put(playerNames[i], prefixes[i]);
            System.out.println("Updated prefix for " + playerNames[i] + ": " + prefixes[i]);
        }
    }
}