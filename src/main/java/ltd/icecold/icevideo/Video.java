package ltd.icecold.icevideo;

import com.sun.jna.Pointer;
import net.minecraft.client.Minecraft;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoEngineVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.videoengine.VideoEngine;
import uk.co.caprica.vlcj.player.embedded.videosurface.videoengine.VideoEngineCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.videoengine.VideoEngineCallbackAdapter;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author ice_cold
 * @date Create in 13:38 2020/7/4
 */
public class Video {

    private final VideoEngineCallback videoEngineCallback;
    private final Semaphore contextSemaphore = new Semaphore(0);
    private final MediaPlayerFactory mediaPlayerFactory;
    private final EmbeddedMediaPlayer mediaPlayer;
    private VideoEngineVideoSurface videoSurface;
    private long vlcWindowHandle;
    private long mainWindowHandle;


    public Video() {
        mainWindowHandle = Minecraft.getInstance().getMainWindow().getHandle();
        this.videoEngineCallback =  new VideoEngineHandler();
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        this.videoSurface = mediaPlayerFactory.videoSurfaces().newVideoSurface(VideoEngine.libvlc_video_engine_opengl, videoEngineCallback);

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
        glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        //glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        this.mediaPlayer.videoSurface().set(videoSurface);

    }

    public void playVideo(String mrl) {
        Minecraft.getInstance().mouseHelper.ungrabMouse();
        mediaPlayer.media().play(mrl);
        loop();
        mediaPlayer.release();
        mediaPlayerFactory.release();

        vlcWindowHandle = glfwCreateWindow(640, 480, "videos", 0, mainWindowHandle);
        glfwFreeCallbacks(vlcWindowHandle);
    }


    private void loop() {
        glfwMakeContextCurrent(vlcWindowHandle);
        contextSemaphore.release();
        while (!glfwWindowShouldClose(mainWindowHandle)) {
            glfwPollEvents();
            try {
                Thread.sleep(1000 / 60);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class VideoEngineHandler extends VideoEngineCallbackAdapter {
        @Override
        public long onGetProcAddress(Pointer opaque, String functionName) {
            AtomicLong l = new AtomicLong();
            Minecraft.getInstance().execute(() -> {
                l.set(glfwGetProcAddress(functionName));
            });
            return l.get();
        }

        @Override
        public boolean onMakeCurrent(Pointer opaque, boolean enter) {
            if (enter) {
                try {
                    contextSemaphore.acquire();
                    glfwMakeContextCurrent(vlcWindowHandle);
                } catch (Exception e) {
                    glfwMakeContextCurrent(vlcWindowHandle);
                    contextSemaphore.release();
                    return false;
                }
            } else {
                try {
                    glfwMakeContextCurrent(vlcWindowHandle);
                } finally {
                    contextSemaphore.release();
                }
            }
            return true;
        }

        @Override
        public void onSwap(Pointer opaque) {
            Minecraft.getInstance().execute(() -> {glfwSwapBuffers(vlcWindowHandle);});

        }


        @Override
        public void onCleanup(Pointer opaque) {
            Minecraft.getInstance().execute(() -> {glfwMakeContextCurrent(mainWindowHandle);});

            //Minecraft.getInstance().enqueue(() -> Minecraft.getInstance().displayGuiScreen(null));
            //Minecraft.getInstance().gameRenderer.updateCameraAndRender(Minecraft.getInstance().getRenderPartialTicks(),Util.nanoTime(),true);
            //Minecraft.getInstance().gameRenderer.tick();
            //Minecraft.getInstance().mouseHelper.grabMouse();
        }
    }
}
