package mod.render360.gui.advanced;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.gui.Slider;
import mod.render360.render.Cubic;
import mod.render360.render.RenderMethod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;

public class CubicGui implements Advanced {
	
	private static final Cubic cubic = new Cubic();

	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height) {
		buttonList.add(new Slider(new Responder(), 18140, width / 2 - 155, height / 6 + 72, 150, 20, "Quality", 0.1f, 5f, cubic.quality, 0.1f, null));
		buttonList.add(new GuiButton(18141, width / 2 + 5, height / 6 + 72, 150, 20, "Antialiasing: " + (cubic.antialiasing == 1 ? "OFF" : cubic.antialiasing == 4 ? "LOW" : "HIGH")));
		buttonList.add(new GuiButton(18142, width / 2 - 155, height / 6 + 96, 150, 20, "Resize Gui: " + (cubic.resizeGui ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18143, width / 2 + 5, height / 6 + 96, 150, 20, "Background Color: " + (cubic.skyBackground ? "Sky" : "Black")));
	}

	@Override
	public void actionPerformed(GuiButton guiButton) {
		switch (guiButton.id) {
		case 18141:
			switch (cubic.antialiasing) {
			case 1:
				cubic.antialiasing = 4;
				break;
			case 4:
				cubic.antialiasing = 16;
				break;
			default:
			case 16:
				cubic.antialiasing = 1;
				break;
			}
			guiButton.displayString = "Antialiasing: " + (cubic.antialiasing == 1 ? "OFF" : cubic.antialiasing == 4 ? "LOW" : "HIGH");
			break;
		case 18142:
			cubic.resizeGui = !cubic.resizeGui;
			guiButton.displayString = "Resize Gui: " + (cubic.resizeGui ? "ON" : "OFF");
			break;
		case 18143:
			cubic.skyBackground = !cubic.skyBackground;
			guiButton.displayString = "Background Color: " + (cubic.skyBackground ? "Sky" : "Black");
			break;
		}
	}
	
	@Override
	public RenderMethod getRenderMethod() {
		return cubic;
	}
	
	private class Responder implements GuiResponder {
		@Override
		public void setEntryValue(int id, boolean value) {
			
		}

		@Override
		public void setEntryValue(int id, float value) {
			//Quality
			if (id == 18140) {
				if (cubic.quality != value) {
					cubic.quality = value;
					RenderUtil.forceReload();
				}
			}
		}

		@Override
		public void setEntryValue(int id, String value) {
			
		}
	}
}
