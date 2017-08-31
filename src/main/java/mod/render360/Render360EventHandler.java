package mod.render360;

import core.render360.Render360Event;
import core.render360.coretransform.RenderUtil;
import mod.render360.gui.Render360Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Render360EventHandler {

	@SubscribeEvent
	public void initGui(InitGuiEvent.Post e) {
		if (e.getGui() instanceof GuiOptions) {
			e.getButtonList().add(new GuiButton(18107,e.getGui().width / 2 - 155,
					e.getGui().height / 6 + 12, 150, 20, "Render 360 Settings"));
		}
	}
	
	@SubscribeEvent
	public void actionPerformed(ActionPerformedEvent.Pre e) {
		if (e.getGui() instanceof GuiOptions && e.getButton().id == 18107) {
			e.getGui().mc.gameSettings.saveOptions();
			e.getGui().mc.displayGuiScreen(new Render360Settings(e.getGui()));
		}
	}
	
	@SubscribeEvent
	public void worldLoad(Render360Event.DimensionLoadEvent e) {
		RenderUtil.onWorldLoad(); //FIXME
	}
	
	@SubscribeEvent
	public void worldUnload(Render360Event.DimensionUnloadEvent e) {
		RenderUtil.onWorldUnload(); //FIXME
	}
	
	@SubscribeEvent
	public void setLoadingProgressBackground(Render360Event.LoadingProgressBackgroundEvent e) {
		if (RenderUtil.renderMethod.replaceLoadingScreen()) {
			e.setCanceled(true);
			RenderUtil.renderMethod.renderLoadingScreen(e.guiScreen, e.framebuffer);
		}
	}
	
	@SubscribeEvent
	public void drawWorldBackground(Render360Event.DrawWorldBackgroundEvent e) {
		if (RenderUtil.renderMethod.getResizeGui()) {
			e.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public void drawBackground(Render360Event.DrawBackgroundEvent e) {
		if (RenderUtil.renderMethod.replaceLoadingScreen()) {
			e.setCanceled(true);
			RenderUtil.renderMethod.renderLoadingScreen(e.guiScreen);
		}
	}
	
	@SubscribeEvent
	public void rotateParticle(Render360Event.RotateParticleEvent e) {
		float posX = e.posX;
		float posY = e.posY - Minecraft.getMinecraft().thePlayer.eyeHeight;
		float posZ = e.posZ;
		
		float hDist = (float) (Math.sqrt(posZ*posZ + posX*posX));
		float dist = (float) (Math.sqrt(posZ*posZ + posY*posY + posX*posX));
		
		e.rotationX = posZ/hDist;
		e.rotationZ = 1-Math.abs(posY/dist);
		e.rotationYZ = -posX/hDist;
		e.rotationXY = -posY/dist * posX/hDist;
		e.rotationXZ = -posY/dist * posZ/hDist;
	}
}
