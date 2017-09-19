package mod.render360.gui;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.render.Flex;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;

public class SimpleGui implements Settings {
	
	private static Flex flex = new Flex();

	public SimpleGui() {
		RenderUtil.renderMethod = flex;
		RenderUtil.forceReload();
	}
	
	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height) {
		buttonList.add(new Slider(new Responder(), 18104, width / 2 - 180, height / 6 + 24, 360, 20, "FOV", 0f, 360f, flex.fov, 1f, null));
	}
	
	@Override
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen) {
		
	}
	
	public class Responder implements GuiResponder {
		@Override
		public void setEntryValue(int id, boolean value) {
			
		}
		
		@Override
		public void setEntryValue(int id, float value) {
			//FOV
			if (id == 18104) {
				flex.fov = value;
			}
		}

		@Override
		public void setEntryValue(int id, String value) {
			
		}
	}
}
