package mod.render360.gui;

import java.util.List;

import org.lwjgl.input.Keyboard;

import mod.render360.RenderUtil;
import mod.render360.render.Flex;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class SimpleGui implements Settings {
	
	private static Flex flex = new Flex();
	
	private Slider FOVSlider;
	private GuiTextField FOVTextField;
	private GuiTextField ZoomTextField;

	public SimpleGui() {
		RenderUtil.renderMethod = flex;
		RenderUtil.forceReload();
	}
	
	@Override
	public void updateScreen() {
		FOVTextField.updateCursorCounter();
		ZoomTextField.updateCursorCounter();
	}
	
	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height, FontRenderer fontRendererObj) {
		FOVSlider = new Slider(new Responder(), 18120, width / 2 - 180, height / 6 + 24, 360, 20, "FOV", 0f, 360f, flex.fov, 1f, null);
		buttonList.add(FOVSlider);
		FOVTextField = new GuiTextField(18121, fontRendererObj, width / 2 - 155, height / 6 + 72, 150, 20);
		FOVTextField.setText(String.format("%s", flex.fov));
		ZoomTextField = new GuiTextField(18122, fontRendererObj, width / 2 + 5, height / 6 + 72, 150, 20);
		ZoomTextField.setText(String.format("%s", flex.zoom));
		buttonList.add(new GuiButton(18124, width / 2 + 5, height / 6 + 96, 150, 20, "Show Hand: " + (flex.renderHand ? "ON" : "OFF")));
	}
	
	@Override
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen) {
		if (guiButton.id == 18124) {
			flex.renderHand = !flex.renderHand;
			guiButton.displayString = "Show Hand: " + (flex.renderHand ? "ON" : "OFF");
		}
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			return;
		}
		if (FOVTextField.isFocused()) {
			FOVTextField.textboxKeyTyped(typedChar, keyCode);
			try {
				flex.fov = Float.parseFloat(FOVTextField.getText());
			} catch (NumberFormatException e) {
				FOVTextField.moveCursorBy(-1);
				FOVTextField.deleteFromCursor(1); //TODO test this
			}
		} else if (ZoomTextField.isFocused()) {
			ZoomTextField.textboxKeyTyped(typedChar, keyCode);
			try {
				flex.zoom = Float.parseFloat(ZoomTextField.getText());
			} catch (NumberFormatException e) {
				ZoomTextField.moveCursorBy(-1);
				ZoomTextField.deleteFromCursor(1); //TODO test this
			}
		}
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		FOVTextField.mouseClicked(mouseX, mouseY, mouseButton);
		ZoomTextField.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void drawScreen() {
		FOVTextField.drawTextBox();
		ZoomTextField.drawTextBox();
	}
	
	public class Responder implements GuiResponder {
		@Override
		public void setEntryValue(int id, boolean value) {
			
		}
		
		@Override
		public void setEntryValue(int id, float value) {
			//FOV
			if (id == 18120) {
				flex.fov = value;
			}
		}

		@Override
		public void setEntryValue(int id, String value) {
			
		}
	}
}
