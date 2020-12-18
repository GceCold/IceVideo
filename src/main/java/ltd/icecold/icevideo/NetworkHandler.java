package ltd.icecold.icevideo;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * @author ice-cold
 */
public class NetworkHandler {
    public static final int IDX = 233;
    private SimpleChannel channel;

    public NetworkHandler() {
        channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("ice_video_test", "message"))
                .networkProtocolVersion(() -> "ice")
                .serverAcceptedVersions(NetworkRegistry.ACCEPTVANILLA::equals)
                .clientAcceptedVersions(NetworkRegistry.ACCEPTVANILLA::equals)
                .simpleChannel();
        channel.registerMessage(IDX, String.class, this::encode, this::decode, this::handle);
    }

    private void encode(String pkt, PacketBuffer buf) {
        buf.writeBytes(pkt.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(PacketBuffer buf) {
        return buf.toString(StandardCharsets.UTF_8);
    }

    private void handle(final String pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.setPacketHandled(true);
        Minecraft.getInstance().enqueue(() -> {
            Video video = new Video();
            video.playVideo("2020-07-05 14-39-46.mp4");
        });

    }
}
