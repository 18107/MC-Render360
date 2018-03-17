package mod.render360.render;

import java.util.List;

import org.lwjgl.opengl.GL20;

import mod.render360.Reader;
import mod.render360.Shader;
import mod.render360.gui.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.shader.Framebuffer;

public class Equirectangular extends RenderMethod {
	
	public boolean drawCircle = false;
	
	public boolean stabilizeYaw = false;
	public boolean stabilizePitch = false;
	
	public boolean resizeGui = false;
	
	public float quality = 1;
	
	public int antialiasing = 16;
	
	@Override
	public String getName() {
		return "Equirectangular";
	}
	
	@Override
	public String getFragmentShader() {
		return Reader.read("/mod/render360/shaders/equirectangular.fs");
	}
	
	@Override
	public void runShader(EntityRenderer er, Minecraft mc, Framebuffer framebuffer,
			Shader shader, int[] framebufferTextures) {
		GL20.glUseProgram(shader.getShaderProgram());

		GL20.glUseProgram(shader.getShaderProgram());
		int circleUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "drawCircle");
		GL20.glUniform1i(circleUniform, drawCircle ? 1 : 0);
		
		if (!getResizeGui() || mc.gameSettings.hideGUI) {
			int cursorUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "drawCursor");
			GL20.glUniform1i(cursorUniform, 0);
		}
		
		//FIXME
		float rotX = stabilizeYaw ? mc.getRenderViewEntity().rotationYaw : 0;
		float rotY = stabilizePitch ? mc.getRenderViewEntity().rotationPitch : 0;
		while (rotX < 0) rotX += 360;
		while (rotX > 360) rotX -= 360;
		if (mc.gameSettings.thirdPersonView == 2) {
			rotY = -rotY;
		}
		
		int angleUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "rotation");
		GL20.glUniform2f(angleUniform, rotX, rotY);
		
		super.runShader(er, mc, framebuffer, shader, framebufferTextures);
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
	public boolean renderAllSides() {
		return stabilizeYaw || stabilizePitch;
	}
}
