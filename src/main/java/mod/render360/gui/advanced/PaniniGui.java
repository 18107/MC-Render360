package mod.render360.gui.advanced;

import java.text.DecimalFormat;
import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.gui.Slider;
import mod.render360.gui.Slider.FormatHelper;
import mod.render360.render.Panini;
import mod.render360.render.RenderMethod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;

public class PaniniGui implements Advanced {
	
	private static final Panini panini = new Panini();

	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height) {
		FormatHelper formatHelper = new FormatHelper() {
			@Override
			public String getText(int id, String name, float value) {
				return name + ": " + new DecimalFormat("#.##").format(value);
			}
		};
		buttonList.add(new Slider(new Responder(), 18160, width / 2 - 180, height / 6 + 48, 360, 20, "FOV", 0f, 360f, panini.fov, 1f, null));
		buttonList.add(new Slider(new Responder(), 18161, width / 2 - 155, height / 6 + 72, 150, 20, "Quality", 0.1f, 5f, panini.quality, 0.1f, null));
		buttonList.add(new Slider(new Responder(), 18162, width / 2 + 5, height / 6 + 72, 150, 20, "Distance", 0f, 2f, panini.dist, 0.01f, formatHelper));
		buttonList.add(new GuiButton(18163, width / 2 - 155, height / 6 + 96, 150, 20, "Resize Gui: " + (panini.resizeGui ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18164, width / 2 + 5, height / 6 + 96, 150, 20, "Antialiasing: " + (panini.antialiasing == 1 ? "OFF" : panini.antialiasing == 4 ? "LOW" : "HIGH")));
		buttonList.add(new GuiButton(18165, width / 2 + 5, height / 6 + 120, 150, 20, "Show Hand: " + (panini.renderHand ? "ON" : "OFF")));
	}

	@Override
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen) {
		switch (guiButton.id) {
		case 18163:
			panini.resizeGui = !panini.resizeGui;
			guiButton.displayString = "Resize Gui: " + (panini.resizeGui ? "ON" : "OFF");
			break;
		case 18164:
			switch (panini.antialiasing) {
			case 1:
				panini.antialiasing = 4;
				break;
			case 4:
				panini.antialiasing = 16;
				break;
			default:
			case 16:
				panini.antialiasing = 1;
				break;
			}
			guiButton.displayString = "Antialiasing: " + (panini.antialiasing == 1 ? "OFF" : panini.antialiasing == 4 ? "LOW" : "HIGH");
			break;
		case 18165:
			panini.renderHand = !panini.renderHand;
			guiButton.displayString = "Show Hand: " + (panini.renderHand ? "ON" : "OFF");
			break;
		}
	}

	@Override
	public RenderMethod getRenderMethod() {
		return panini;
	}
	
	private class Responder implements GuiResponder {
		@Override
		public void setEntryValue(int id, boolean value) {
			
		}

		@Override
		public void setEntryValue(int id, float value) {
			//FOV
			switch (id) {
			case 18160:
				panini.fov = value;
				break;
			case 18161:
				if (panini.quality != value) {
					panini.quality = value;
					RenderUtil.forceReload();
				}
				break;
			case 18162: //dist
				panini.dist = value;
				break;
			}
		}

		@Override
		public void setEntryValue(int id, String value) {
			
		}
	}
}
