package mod.render360.gui;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public interface Settings {
	
	public void  updateScreen();

	public void initGui(List<GuiButton> buttonList, int width, int height, FontRenderer fontRendererObj);
	
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen);
	
	public void keyTyped(char typedChar, int keyCode);
	
	public void mouseClicked(int mouseX, int mouseY, int mouseButton);
	
	public void drawScreen();
}
