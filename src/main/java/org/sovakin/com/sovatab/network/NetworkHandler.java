package org.sovakin.com.sovatab.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.sovakin.com.sovatab.client.CustomTabListRenderer;

import java.util.Arrays;
import java.util.function.Supplier;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("sovatab", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        System.out.println("Initializing NetworkHandler");
        INSTANCE.registerMessage(0, RequestPrefixesPacket.class, RequestPrefixesPacket::encode, RequestPrefixesPacket::decode, RequestPrefixesPacket::handle);
        INSTANCE.registerMessage(1, PrefixDataPacket.class, PrefixDataPacket::encode, PrefixDataPacket::decode, PrefixDataPacket::handle);
    }

    public static class RequestPrefixesPacket {
        private final String playerName;

        public RequestPrefixesPacket(String playerName) {
            this.playerName = playerName;
        }

        public static void encode(RequestPrefixesPacket packet, FriendlyByteBuf buffer) {
            buffer.writeUtf(packet.playerName);
        }

        public static RequestPrefixesPacket decode(FriendlyByteBuf buffer) {
            return new RequestPrefixesPacket(buffer.readUtf());
        }

        public static void handle(RequestPrefixesPacket packet, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                System.out.println("Client requested prefixes: " + packet.playerName);
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class PrefixDataPacket {
        private final String[] playerNames;
        private final String[] prefixes;

        public PrefixDataPacket(String[] playerNames, String[] prefixes) {
            this.playerNames = playerNames;
            this.prefixes = prefixes;
        }

        public static void encode(PrefixDataPacket packet, FriendlyByteBuf buffer) {
            buffer.writeInt(packet.playerNames.length);
            for (int i = 0; i < packet.playerNames.length; i++) {
                buffer.writeUtf(packet.playerNames[i]);
                buffer.writeUtf(packet.prefixes[i]);
            }
        }

        public static PrefixDataPacket decode(FriendlyByteBuf buffer) {
            System.out.println("Decoding PrefixDataPacket");
            int size = buffer.readInt();
            System.out.println("Number of players: " + size);
            String[] playerNames = new String[size];
            String[] prefixes = new String[size];
            for (int i = 0; i < size; i++) {
                playerNames[i] = buffer.readUtf();
                prefixes[i] = buffer.readUtf();
                System.out.println("Decoded player: " + playerNames[i] + ", prefix: " + prefixes[i]);
            }
            return new PrefixDataPacket(playerNames, prefixes);
        }

        public static void handle(PrefixDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                System.out.println("Handling PrefixDataPacket");
                System.out.println("Received prefix data: " + Arrays.toString(packet.playerNames) + " " + Arrays.toString(packet.prefixes));
                CustomTabListRenderer.updatePrefixes(packet.playerNames, packet.prefixes);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}