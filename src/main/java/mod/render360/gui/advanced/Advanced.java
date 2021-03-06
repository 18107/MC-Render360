package mod.render360.gui.advanced;

import java.util.List;

import mod.render360.render.RenderMethod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public interface Advanced {

	public void initGui(List<GuiButton> buttonList, int width, int height);
	
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen);
	
	public RenderMethod getRenderMethod();
}
