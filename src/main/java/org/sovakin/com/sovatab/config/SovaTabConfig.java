package org.sovakin.com.sovatab.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Mod.EventBusSubscriber
public class SovaTabConfig {
    public static String header;
    public static int maxPlayers;
    public static String footerColor;

    private static final File CONFIG_FILE = new File("config/sovatab.toml");

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            try (InputStream in = SovaTabConfig.class.getClassLoader().getResourceAsStream("sovatab.toml")) {
                if (in != null) {
                    Files.copy(in, CONFIG_FILE.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (FileConfig config = FileConfig.of(CONFIG_FILE)) {
            config.load();

            header = config.getOrElse("header", "[#800080]TwitchWorld [#808080]| [#FFFFFF]сервер ZalupaTech");
            maxPlayers = config.getOrElse("maxPlayers", 100);
            footerColor = config.getOrElse("footerColor", "#FFFF00");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setup(final FMLCommonSetupEvent event) {
        loadConfig();
    }

    public static void doClientStuff(final FMLClientSetupEvent event) {
        loadConfig();
    }

    static {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SovaTabConfig::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SovaTabConfig::doClientStuff);
    }
}