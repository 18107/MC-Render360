package mod.render360.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public interface Settings {

	public void initGui(List<GuiButton> buttonList, int width, int height);
	
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen);
}
