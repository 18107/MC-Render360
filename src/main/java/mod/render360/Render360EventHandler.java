package mod.render360;

import core.render360.Render360Event;
import core.render360.coretransform.RenderUtil;
import mod.render360.gui.Render360Settings;
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
}
