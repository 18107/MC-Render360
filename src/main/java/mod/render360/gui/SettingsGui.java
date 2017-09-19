package mod.render360.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class SettingsGui extends GuiScreen {

	private final GuiScreen parentGuiScreen;
	public static final String screenTitle = "Render 360 Settings";
	
	private static Settings settingsGui = new StandardGui();
	
	public SettingsGui(GuiScreen guiScreenIn) {
		this.parentGuiScreen = guiScreenIn;
	}
	
	@Override
	public void initGui() {
		GuiButton button = new GuiButton(18190, super.width / 2 - 190, super.height / 6 - 12, 120, 20, "Standard");
		if (settingsGui instanceof StandardGui) button.enabled = false;
		super.buttonList.add(button);
		
		button = new GuiButton(18191, super.width / 2 - 60, super.height / 6 - 12, 120, 20, "Simple");
		if (settingsGui instanceof SimpleGui) button.enabled = false;
		super.buttonList.add(button);
		
		button = new GuiButton(18192, super.width / 2 + 70, super.height / 6 - 12, 120, 20, "Advanced");
		if (settingsGui instanceof AdvancedGui) button.enabled = false;
		super.buttonList.add(button);
		
		super.buttonList.add(new GuiButton(200, super.width / 2 - 100, super.height / 6 + 168, I18n.format("gui.done", new Object[0])));
		settingsGui.initGui(super.buttonList, super.width, super.height);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			switch (button.id) {
			case 18190: //Standard
				settingsGui = new StandardGui();
				mc.displayGuiScreen(new SettingsGui(parentGuiScreen));
				break;
			case 18191: //Simple
				settingsGui = new SimpleGui();
				mc.displayGuiScreen(new SettingsGui(parentGuiScreen));
				break;
			case 18192: //Advanced
				settingsGui = new AdvancedGui();
				mc.displayGuiScreen(new SettingsGui(parentGuiScreen));
				break;
			case 200: //done
				this.mc.displayGuiScreen(parentGuiScreen);
				break;
			default:
				settingsGui.actionPerformed(button, parentGuiScreen);
				break;
			}
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawDefaultBackground();
		this.drawCenteredString(super.fontRendererObj, this.screenTitle, this.width / 2, 15, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
