package mod.render360.gui.advanced;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.gui.Slider;
import mod.render360.render.Fisheye;
import mod.render360.render.RenderMethod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;

public class FisheyeGui implements Advanced {
	
	public static final Fisheye fisheye = new Fisheye();

	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height) {
		buttonList.add(new GuiButton(18140, width / 2 - 190, height / 6 - 12, 120, 20, (fisheye.fisheyeType==0?"Stereographic":"")));
		buttonList.add(new GuiButton(18141, width / 2 - 60, height / 6 - 12, 120, 20, (fisheye.fisheyeType==1?"Equidistant":"")));
		buttonList.add(new GuiButton(18142, width / 2 + 70, height / 6 - 12, 120, 20, (fisheye.fisheyeType==2?"Equisolid":"")));
		buttonList.add(new GuiButton(18143, width / 2 + 80, height / 6 - 12, 120, 20, (fisheye.fisheyeType==3?"Thoby":"")));
		buttonList.add(new GuiButton(18144, width / 2 + 90, height / 6 - 12, 120, 20, (fisheye.fisheyeType==4?"Orthographic":"")));
		
		buttonList.add(new Slider(new Responder(), 18150, width / 2 - 155, height / 6 + 72, 150, 20, "Quality", 0.1f, 5f, fisheye.quality, 0.1f, null));
		buttonList.add(new GuiButton(18151, width / 2 + 5, height / 6 + 72, 150, 20, "Antialiasing: " + (fisheye.antialiasing == 1 ? "OFF" : fisheye.antialiasing == 4 ? "LOW" : "HIGH")));
		buttonList.add(new GuiButton(18152, width / 2 - 155, height / 6 + 96, 150, 20, "Resize Gui: " + (fisheye.resizeGui ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18153, width / 2 + 5, height / 6 + 96, 150, 20, "Background Color: " + (fisheye.skyBackground ? "Sky" : "Black")));

		buttonList.add(new GuiButton(18166, width / 2 + 5, height / 6 + 120, 150, 20, "Full Frame: " + (fisheye.fullFrame ? "ON" : "OFF")));
	}

	@Override
	public void actionPerformed(GuiButton guiButton) {
		switch (guiButton.id) {
		case 18161:
			switch (fisheye.antialiasing) {
			case 1:
				fisheye.antialiasing = 4;
				break;
			case 4:
				fisheye.antialiasing = 16;
				break;
			default:
			case 16:
				fisheye.antialiasing = 1;
				break;
			}
			guiButton.displayString = "Antialiasing: " + (fisheye.antialiasing == 1 ? "OFF" : fisheye.antialiasing == 4 ? "LOW" : "HIGH");
			break;
		case 18162:
			fisheye.resizeGui = !fisheye.resizeGui;
			guiButton.displayString = "Resize Gui: " + (fisheye.resizeGui ? "ON" : "OFF");
			break;
		}
	}

	@Override
	public RenderMethod getRenderMethod() {
		return fisheye;
	}
	
	private class Responder implements GuiResponder {
		@Override
		public void setEntryValue(int id, boolean value) {
			
		}

		@Override
		public void setEntryValue(int id, float value) {
			//Quality
			if (id == 18160) {
				if (fisheye.quality != value) {
					fisheye.quality = value;
					RenderUtil.forceReload();
				}
			}
		}

		@Override
		public void setEntryValue(int id, String value) {
			
		}
	}

}
