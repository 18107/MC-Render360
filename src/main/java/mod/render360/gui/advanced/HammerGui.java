package mod.render360.gui.advanced;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.gui.Slider;
import mod.render360.render.Hammer;
import mod.render360.render.RenderMethod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;

public class HammerGui implements Advanced {
	
	private static final Hammer hammer = new Hammer();

	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height) {
		buttonList.add(new Slider(new Responder(), 18150, width / 2 - 155, height / 6 + 72, 150, 20, "Quality", 0.1f, 5f, hammer.quality, 0.1f, null));
		buttonList.add(new GuiButton(18151, width / 2 + 5, height / 6 + 72, 150, 20, "Antialiasing: " + (hammer.antialiasing == 1 ? "OFF" : hammer.antialiasing == 4 ? "LOW" : "HIGH")));
		buttonList.add(new GuiButton(18152, width / 2 - 155, height / 6 + 96, 150, 20, "Resize Gui: " + (hammer.resizeGui ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18153, width / 2 + 5, height / 6 + 96, 150, 20, "Background Color: " + (hammer.skyBackground ? "Sky" : "Black")));
		buttonList.add(new GuiButton(18155, width / 2 + 5, height / 6 + 120, 150, 20, "Show Hand: " + (hammer.renderHand ? "ON" : "OFF")));
	}

	@Override
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen) {
		switch (guiButton.id) {
		case 18151:
			switch (hammer.antialiasing) {
			case 1:
				hammer.antialiasing = 4;
				break;
			case 4:
				hammer.antialiasing = 16;
				break;
			default:
			case 16:
				hammer.antialiasing = 1;
				break;
			}
			guiButton.displayString = "Antialiasing: " + (hammer.antialiasing == 1 ? "OFF" : hammer.antialiasing == 4 ? "LOW" : "HIGH");
			break;
		case 18152:
			hammer.resizeGui = !hammer.resizeGui;
			guiButton.displayString = "Resize Gui: " + (hammer.resizeGui ? "ON" : "OFF");
			break;
		case 18153:
			hammer.skyBackground = !hammer.skyBackground;
			guiButton.displayString = "Background Color: " + (hammer.skyBackground ? "Sky" : "Black");
			break;
		case 18155:
			hammer.renderHand = !hammer.renderHand;
			guiButton.displayString = "Show Hand: " + (hammer.renderHand ? "ON" : "OFF");
			break;
		}
		
	}

	@Override
	public RenderMethod getRenderMethod() {
		return hammer;
	}
	
	private class Responder implements GuiResponder {
		@Override
		public void setEntryValue(int id, boolean value) {
			
		}

		@Override
		public void setEntryValue(int id, float value) {
			//Quality
			if (id == 18150) {
				if (hammer.quality != value) {
					hammer.quality = value;
					RenderUtil.forceReload();
				}
			}
		}

		@Override
		public void setEntryValue(int id, String value) {
			
		}
	}
}
