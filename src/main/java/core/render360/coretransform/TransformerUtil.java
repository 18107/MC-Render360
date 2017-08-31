package core.render360.coretransform;

import core.render360.Render360Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
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
	
	//EntityRenderer
	public static void rotateCamera() {
		Render360Event.RotateCameraEvent event = new Render360Event.RotateCameraEvent();
		MinecraftForge.EVENT_BUS.post(event);
		GlStateManager.rotate(event.roll, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(event.pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(event.yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0, 0, -0.05f);
	}
	
	//Particle
	public static float rotationX;
	public static float rotationZ;
	public static float rotationYZ;
	public static float rotationXY;
	public static float rotationXZ;
	public static float posX;
	public static float posY;
	public static float posZ;
	public static void transformParticle(float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ,
			float posX, float posY, float posZ) {
		
		Render360Event.RotateParticleEvent event = new Render360Event.RotateParticleEvent(
				rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ, posX, posY, posZ);
		MinecraftForge.EVENT_BUS.post(event);
		
		//TODO find a way to return these values instead of having them public
		TransformerUtil.rotationX = event.rotationX;
		TransformerUtil.rotationZ = event.rotationZ;
		TransformerUtil.rotationYZ = event.rotationYZ;
		TransformerUtil.rotationXY = event.rotationXY;
		TransformerUtil.rotationXZ = event.rotationXZ;
		TransformerUtil.posX = event.posX;
		TransformerUtil.posY = event.posY;
		TransformerUtil.posZ = event.posZ;
	}
}
