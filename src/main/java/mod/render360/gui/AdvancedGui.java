package mod.render360.gui;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.gui.advanced.Advanced;
import mod.render360.gui.advanced.CubicGui;
import mod.render360.gui.advanced.EquirectangularGui;
import mod.render360.gui.advanced.HammerGui;
import mod.render360.gui.advanced.PaniniGui;
import mod.render360.gui.advanced.FisheyeGui;
import mod.render360.render.Hammer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class AdvancedGui implements Settings {
	
	private static Advanced guiObject = new EquirectangularGui();
	
	public AdvancedGui() {
		RenderUtil.renderMethod = guiObject.getRenderMethod();
		RenderUtil.forceReload();
	}
	
	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height, FontRenderer fontRendererobj) {
		GuiButton button = new GuiButton(18130, width / 2 - 212, height / 6 + 24, 84, 20, "Cubic");
		if (guiObject instanceof CubicGui) button.enabled = false;
		buttonList.add(button);
		
		button = new GuiButton(18131, width / 2 - 127, height / 6 + 24, 84, 20, "Hammer");
		if (guiObject instanceof HammerGui) button.enabled = false;
		buttonList.add(button);
		
		button = new GuiButton(18132, width / 2 - 42, height / 6 + 24, 84, 20, "Fisheye");
		if (guiObject instanceof FisheyeGui) button.enabled = false;
		buttonList.add(button);
		
		button = new GuiButton(18133, width / 2 + 43, height / 6 + 24, 84, 20, "Panini");
		if (guiObject instanceof PaniniGui) button.enabled = false;
		buttonList.add(button);
				
		button = new GuiButton(18134, width / 2 + 128, height / 6 + 24, 84, 20, "Equirectangular");
		if (guiObject instanceof EquirectangularGui) button.enabled = false;
		buttonList.add(button);
		
		guiObject.initGui(buttonList, width, height);
	}

	@Override
	public void updateScreen() {
		
	}
	
	@Override
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen) {
		switch(guiButton.id) {
		default:
			guiObject.actionPerformed(guiButton, parentScreen);
			return;
		case 18130: //Cubic
			guiObject = new CubicGui();
			break;
		case 18131: //Hammer
			guiObject = new HammerGui();
			break;
		case 18132: //Fisheye
			guiObject = new FisheyeGui();
			break;
		case 18133: //Panini
			guiObject = new PaniniGui();
			break;
		case 18134: //Equirectangular
			guiObject = new EquirectangularGui();
			break;
		}
		RenderUtil.renderMethod = guiObject.getRenderMethod();
		RenderUtil.forceReload();
		Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(parentScreen));
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
