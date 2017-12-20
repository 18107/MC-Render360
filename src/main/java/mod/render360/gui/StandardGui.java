package mod.render360.gui;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.render.Standard;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class StandardGui implements Settings {
	
	public StandardGui() {
		RenderUtil.renderMethod = new Standard();
		RenderUtil.forceReload();
	}

	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height, FontRenderer fontRendererobj) {
		
	}

	@Override
	public void updateScreen() {
		
	}
	
	@Override
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen) {
		
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
	}

	@Override
	public void drawScreen() {
		
	}
}
