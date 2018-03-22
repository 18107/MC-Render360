package mod.render360.render;

import java.util.List;

import org.lwjgl.opengl.GL20;

import mod.render360.Reader;
import mod.render360.gui.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.renderer.EntityRenderer;

public class Fisheye extends RenderMethod {
	
	public float quality = 1;
	
	public int antialiasing = 16;
	
	public boolean skyBackground = true;

	public int fisheyeType = 3;
	public boolean fullFrame = false;
	
	public float fov = 360;
	
	public boolean renderHand;

	@Override
	public String getName() {
		return "Fisheye";
	}

	@Override
	public String getFragmentShader() {
		return Reader.read("/mod/render360/shaders/fisheye.fs");
	}
	
	@Override
	public float getQuality() {
		return quality;
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

	@Override
	public float getFOV() {
		return fov != 0 ? fov : 0.00001f;
	}

	@Override
	public int getFisheyeType() {
		return fisheyeType;
	}
	
	@Override
	public boolean getFullFrame() {
		return fullFrame;
	}

	public class Responder implements GuiResponder {
		@Override
		public void setEntryValue(int id, boolean value) {

		}

		@Override
		public void setEntryValue(int id, float value) {
			//FOV
			if (id == 18104) {
				fov = value;
			}
		}

		@Override
		public void setEntryValue(int id, String value) {

		}
	}
	
	@Override
	public boolean getRenderHand() {
		return renderPass == 0 && renderHand;
	}
}
