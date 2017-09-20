package mod.render360.gui;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.render.Standard;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class StandardGui implements Settings {
	
	public StandardGui() {
		RenderUtil.renderMethod = new Standard();
		RenderUtil.forceReload();
	}

	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height) {
		
	}
	
	@Override
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen) {
		
	}
}
