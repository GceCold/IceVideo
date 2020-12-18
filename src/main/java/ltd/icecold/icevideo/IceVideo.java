package ltd.icecold.icevideo;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * @author icecold
 * @date 2019/9/14 11:47
 */
@Mod("icevideo")
public class IceVideo {
    public IceVideo() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    private void clientSetup(FMLClientSetupEvent event) {
        new NetworkHandler();
    }
}
