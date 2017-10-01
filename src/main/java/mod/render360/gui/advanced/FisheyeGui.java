package mod.render360.gui.advanced;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.gui.SettingsGui;
import mod.render360.gui.Slider;
import mod.render360.render.Fisheye;
import mod.render360.render.RenderMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;

public class FisheyeGui implements Advanced {
	
	public static final Fisheye fisheye = new Fisheye();

	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height) {

		GuiButton button0 = new GuiButton(18140, width / 2 - 190 + ((190*2-0*4)/5 + 0) * 0, height / 6 + 48, (190*2-0*4)/5, 20, "Orthographic" );
		GuiButton button1 = new GuiButton(18141, width / 2 - 190 + ((190*2-0*4)/5 + 0) * 1, height / 6 + 48, (190*2-0*4)/5, 20, "Thoby"        );
		GuiButton button2 = new GuiButton(18142, width / 2 - 190 + ((190*2-0*4)/5 + 0) * 2, height / 6 + 48, (190*2-0*4)/5, 20, "Equisolid"    );
		GuiButton button3 = new GuiButton(18143, width / 2 - 190 + ((190*2-0*4)/5 + 0) * 3, height / 6 + 48, (190*2-0*4)/5, 20, "Equidistant"  );
		GuiButton button4 = new GuiButton(18144, width / 2 - 190 + ((190*2-0*4)/5 + 0) * 4, height / 6 + 48, (190*2-0*4)/5, 20, "Stereographic");
		button0.enabled = fisheye.fisheyeType!=0;
		button1.enabled = fisheye.fisheyeType!=1;
		button2.enabled = fisheye.fisheyeType!=2;
		button3.enabled = fisheye.fisheyeType!=3;
		button4.enabled = fisheye.fisheyeType!=4;
		buttonList.add(button0);
		buttonList.add(button1);
		buttonList.add(button2);
		buttonList.add(button3);
		buttonList.add(button4);
		
		buttonList.add(new Slider(new Responder(), 18150, width / 2 - 155, height / 6 + 72, 150, 20, "Quality", 0.1f, 5f, fisheye.quality, 0.1f, null));
		buttonList.add(new GuiButton(18151, width / 2 + 5, height / 6 + 72, 150, 20, "Antialiasing: " + (fisheye.antialiasing == 1 ? "OFF" : fisheye.antialiasing == 4 ? "LOW" : "HIGH")));
		buttonList.add(new GuiButton(18152, width / 2 - 155, height / 6 + 96, 150, 20, "Resize Gui: " + (fisheye.resizeGui ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18153, width / 2 + 5, height / 6 + 96, 150, 20, "Background Color: " + (fisheye.skyBackground ? "Sky" : "Black")));

		buttonList.add(new GuiButton(18160, width / 2 + 5, height / 6 + 120, 150, 20, "Full Frame: " + (fisheye.fullFrame ? "ON" : "OFF")));
	}

	@Override
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen) {
		switch (guiButton.id) {
		case 18140:
		case 18141:
		case 18142:
		case 18143:
		case 18144:
			fisheye.fisheyeType=-18140+guiButton.id;
			/*
			buttonList.byID(18140).enabled = true;
			buttonList.byID(18141).enabled = true;
			buttonList.byID(18142).enabled = true;
			buttonList.byID(18143).enabled = true;
			buttonList.byID(18144).enabled = true;
			 */
			guiButton.enabled = false;
			Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(parentScreen));
			break;
		case 18151:
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
		case 18152:
			fisheye.resizeGui = !fisheye.resizeGui;
			guiButton.displayString = "Resize Gui: " + (fisheye.resizeGui ? "ON" : "OFF");
			break;
		case 18153:
			fisheye.skyBackground = !fisheye.skyBackground;
			guiButton.displayString = "Background Color: " + (fisheye.skyBackground ? "Sky" : "Black");
			break;
		case 18160:
			fisheye.fullFrame = !fisheye.fullFrame;
			guiButton.displayString = "Full Frame: " + (fisheye.fullFrame ? "ON" : "OFF");
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
