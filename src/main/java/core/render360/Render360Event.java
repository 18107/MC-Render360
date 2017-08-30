package core.render360;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class Render360Event extends Event {

	public static class DimensionLoadEvent extends Render360Event {
		
	}
	
	public static class DimensionUnloadEvent extends Render360Event {
		
	}
	
	@Cancelable
	public static class LoadingProgressBackgroundEvent extends Render360Event {
		
		public final GuiScreen guiScreen;
		public final Framebuffer framebuffer;
		
		public LoadingProgressBackgroundEvent(GuiScreen guiScreen, Framebuffer framebuffer) {
			this.guiScreen = guiScreen;
			this.framebuffer = framebuffer;
		}
	}
}
