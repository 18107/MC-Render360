package mod.render360.render;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.Shader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.shader.Framebuffer;

public class Standard extends RenderMethod {
	
	@Override
	public String getName() {
		return "Standard";
	}
	
	@Override
	public String getFragmentShader() {
		return "#version 130//\n in vec2 texcoord; out vec4 color; void main(void) { color = vec4(1, 0, 1, 1); }";
	}
	
	@Override
	public void renderWorld(EntityRenderer er, Minecraft mc, Framebuffer framebuffer, Shader shader,
			int cubeTexture, float partialTicks, long finishTimeNano, int width, int height, float sizeIncrease) {
		RenderUtil.render360 = false;
		RenderUtil.partialWidth = mc.displayWidth;
		RenderUtil.partialHeight = mc.displayHeight;
		er.renderWorldPass(2, partialTicks, finishTimeNano);
	}
	
	@Override
	public boolean getResizeGui() {
		return false;
	}
}
