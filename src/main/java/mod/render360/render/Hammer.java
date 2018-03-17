package mod.render360.render;

import java.util.List;

import mod.render360.Reader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.EntityRenderer;

public class Hammer extends RenderMethod {
	
	public boolean resizeGui = false;
	
	public float quality = 1;
	
	public int antialiasing = 16;
	
	public boolean skyBackground = true;
	
	@Override
	public String getName() {
		return "Hammer";
	}
	
	@Override
	public String getFragmentShader() {
		return Reader.read("/mod/hammer.fs");
	}
	
	@Override
	public float getQuality() {
		return quality;
	}
	
	@Override
	public boolean getResizeGui() {
		return resizeGui;
	}
	
	@Override
	public int getAntialiasing() {
		return antialiasing;
	}
	
	@Override
	public boolean replaceLoadingScreen() {
		return true;
	}
	
	@Override
	public float[] getBackgroundColor() {
		if (skyBackground) {
			EntityRenderer er = Minecraft.getMinecraft().entityRenderer;
			return new float[] {er.fogColorRed, er.fogColorGreen, er.fogColorBlue};
		} else {
			return null;
		}
	}
}
