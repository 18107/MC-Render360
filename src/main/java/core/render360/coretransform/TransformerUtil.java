package core.render360.coretransform;

import core.render360.Render360Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.common.MinecraftForge;

public class TransformerUtil {

	//Minecraft
	public static void onWorldLoad(WorldClient worldClient) {
		if (worldClient != null) {
			MinecraftForge.EVENT_BUS.post(new Render360Event.DimensionLoadEvent());
		} else {
			MinecraftForge.EVENT_BUS.post(new Render360Event.DimensionUnloadEvent());
		}
	}
	
	//LoadingScreenRenderer
	public static boolean setLoadingProgressBackground(Framebuffer framebuffer) {
		Render360Event.LoadingProgressBackgroundEvent event =
				new Render360Event.LoadingProgressBackgroundEvent(
						Minecraft.getMinecraft().currentScreen, framebuffer);
		MinecraftForge.EVENT_BUS.post(event);
		return event.isCanceled();
	}
	
	//GuiScreen
	public static boolean onDrawWorldBackground(GuiScreen guiScreen) {
		Render360Event.DrawWorldBackgroundEvent event = new Render360Event.DrawWorldBackgroundEvent(guiScreen);
		MinecraftForge.EVENT_BUS.post(event);
		return event.isCanceled();
	}
	
	//GuiScreen
	public static boolean onDrawBackground(GuiScreen guiScreen) {
		Render360Event.DrawBackgroundEvent event = new Render360Event.DrawBackgroundEvent(guiScreen);
		MinecraftForge.EVENT_BUS.post(event);
		return event.isCanceled();
	}
}
