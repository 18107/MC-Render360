package mod.render360.gui.advanced;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.gui.Slider;
import mod.render360.render.Equirectangular;
import mod.render360.render.RenderMethod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;

public class EquirectangularGui implements Advanced {
	
	public static final Equirectangular equirectangular = new Equirectangular();

	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height) { //TODO change ids
		buttonList.add(new Slider(new Responder(), 18111, width / 2 - 155, height / 6 + 72, 150, 20, "Quality", 0.1f, 5f, equirectangular.quality, 0.1f, null));
		buttonList.add(new GuiButton(18112, width / 2 + 5, height / 6 + 72, 150, 20, "Antialiasing: " + (equirectangular.antialiasing == 1 ? "OFF" : equirectangular.antialiasing == 4 ? "LOW" : "HIGH")));
		buttonList.add(new GuiButton(18109, width / 2 - 155, height / 6 + 96, 150, 20, "Resize Gui: " + (equirectangular.resizeGui ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18110, width / 2 + 5, height / 6 + 96, 150, 20, "Draw Circle: " + (equirectangular.drawCircle ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18107, width / 2 - 155, height / 6 + 120, 150, 20, "Stabilize Pitch: " + (equirectangular.stabilizePitch ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18108, width / 2 + 5, height / 6 + 120, 150, 20, "Stabilize Yaw: " + (equirectangular.stabilizeYaw ? "ON" : "OFF")));
	}

	@Override
	public void actionPerformed(GuiButton guiButton) {
		switch (guiButton.id) {
		case 18107:
			equirectangular.stabilizePitch = !equirectangular.stabilizePitch;
			guiButton.displayString = "Stabilize Pitch: " + (equirectangular.stabilizePitch ? "ON" : "OFF");
			break;
		case 18108:
			equirectangular.stabilizeYaw = !equirectangular.stabilizeYaw;
			guiButton.displayString = "Stabilize Yaw: " + (equirectangular.stabilizeYaw ? "ON" : "OFF");
			break;
		case 18109:
			equirectangular.resizeGui = !equirectangular.resizeGui;
			guiButton.displayString = "Resize Gui: " + (equirectangular.resizeGui ? "ON" : "OFF");
			break;
		case 18110:
			equirectangular.drawCircle = !equirectangular.drawCircle;
			guiButton.displayString = "Draw Circle: " + (equirectangular.drawCircle ? "ON" : "OFF");
			break;
		case 18112:
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
			if (id == 18111) {
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
