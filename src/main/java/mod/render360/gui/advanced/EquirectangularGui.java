package mod.render360.gui.advanced;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.gui.Slider;
import mod.render360.render.Equirectangular;
import mod.render360.render.RenderMethod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;

public class EquirectangularGui implements Advanced {
	
	public static final Equirectangular equirectangular = new Equirectangular();

	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height) {
		buttonList.add(new Slider(new Responder(), 18170, width / 2 - 155, height / 6 + 72, 150, 20, "Quality", 0.1f, 5f, equirectangular.quality, 0.1f, null));
		buttonList.add(new GuiButton(18171, width / 2 + 5, height / 6 + 72, 150, 20, "Antialiasing: " + (equirectangular.antialiasing == 1 ? "OFF" : equirectangular.antialiasing == 4 ? "LOW" : "HIGH")));
		buttonList.add(new GuiButton(18172, width / 2 - 155, height / 6 + 96, 150, 20, "Resize Gui: " + (equirectangular.resizeGui ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18173, width / 2 + 5, height / 6 + 96, 150, 20, "Draw Circle: " + (equirectangular.drawCircle ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18174, width / 2 - 155, height / 6 + 120, 150, 20, "Stabilize Pitch: " + (equirectangular.stabilizePitch ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18175, width / 2 + 5, height / 6 + 120, 150, 20, "Stabilize Yaw: " + (equirectangular.stabilizeYaw ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18177, width / 2 + 5, height / 6 + 144, 150, 20, "Show Hand: " + (equirectangular.renderHand ? "ON" : "OFF")));
	}

	@Override
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen) {
		switch (guiButton.id) {
		case 18171:
			switch (equirectangular.antialiasing) {
			case 1:
				equirectangular.antialiasing = 4;
				break;
			case 4:
				equirectangular.antialiasing = 16;
				break;
			default:
			case 16:
				equirectangular.antialiasing = 1;
				break;
			}
			guiButton.displayString = "Antialiasing: " + (equirectangular.antialiasing == 1 ? "OFF" : equirectangular.antialiasing == 4 ? "LOW" : "HIGH");
			break;
		case 18172:
			equirectangular.resizeGui = !equirectangular.resizeGui;
			guiButton.displayString = "Resize Gui: " + (equirectangular.resizeGui ? "ON" : "OFF");
			break;
		case 18173:
			equirectangular.drawCircle = !equirectangular.drawCircle;
			guiButton.displayString = "Draw Circle: " + (equirectangular.drawCircle ? "ON" : "OFF");
			break;
		case 18174:
			equirectangular.stabilizePitch = !equirectangular.stabilizePitch;
			guiButton.displayString = "Stabilize Pitch: " + (equirectangular.stabilizePitch ? "ON" : "OFF");
			break;
		case 18175:
			equirectangular.stabilizeYaw = !equirectangular.stabilizeYaw;
			guiButton.displayString = "Stabilize Yaw: " + (equirectangular.stabilizeYaw ? "ON" : "OFF");
			break;
		case 18177:
			equirectangular.renderHand = !equirectangular.renderHand;
			guiButton.displayString = "Show Hand: " + (equirectangular.renderHand ? "ON" : "OFF");
			break;
		}
	}

	@Override
	public RenderMethod getRenderMethod() {
		return equirectangular;
	}
	
	private class Responder implements GuiResponder {
		@Override
		public void setEntryValue(int id, boolean value) {
			
		}

		@Override
		public void setEntryValue(int id, float value) {
			//Quality
			if (id == 18170) {
				if (equirectangular.quality != value) {
					equirectangular.quality = value;
					RenderUtil.forceReload();
				}
			}
		}

		@Override
		public void setEntryValue(int id, String value) {
			
		}
	}

}
