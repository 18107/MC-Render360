package mod.render360.render;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import mod.render360.Reader;
import mod.render360.RenderUtil;
import mod.render360.Shader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;

/**
 * @author 18107, Shaunlebron
 */
public class Flex extends RenderMethod {
	
	public float fov = 180;
	private float lastFOV = 180;
	
	public float zoom = 4;
	
	public boolean renderHand;

	@Override
	public String getName() {
		return "Flex";
	}

	@Override
	public String getFragmentShader() {
		return Reader.read("render360:shaders/flex.fs");
	}
	
	@Override
	public void renderLoadingScreen(GuiScreen guiScreen, Framebuffer framebufferIn) {
		if (getFOV() >= 90) {
			super.renderLoadingScreen(guiScreen, framebufferIn);
		} else {
			//Prevents null pointer exception when moving between dimensions
			if (guiScreen == null) {
				guiScreen = new GuiScreen(){};
				guiScreen.width = framebufferIn.framebufferTextureWidth;
				guiScreen.height = framebufferIn.framebufferTextureHeight;
			}
			Minecraft mc = Minecraft.getMinecraft();
			Framebuffer framebuffer = new Framebuffer((int)(Display.getHeight()*getQuality()), (int)(Display.getHeight()*getQuality()), true);

			framebuffer.bindFramebuffer(false);
			OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture, 0);
			GlStateManager.bindTexture(0);

			//replacement for guiScreen.drawBackground(0);
			GlStateManager.disableLighting();
			GlStateManager.disableFog();
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer vertexbuffer = tessellator.getBuffer();
			mc.getTextureManager().bindTexture(guiScreen.OPTIONS_BACKGROUND);
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			vertexbuffer.pos((double)guiScreen.width*(1-90/getFOV()), (double)guiScreen.height*90/getFOV(), 0.0D).tex(0.0D, (double)((float)guiScreen.height / 32.0F)).color(64, 64, 64, 255).endVertex();
			vertexbuffer.pos((double)guiScreen.width*90/getFOV(), (double)guiScreen.height*90/getFOV(), 0.0D).tex((double)((float)guiScreen.width / 32.0F), (double)((float)guiScreen.height / 32.0F)).color(64, 64, 64, 255).endVertex();
			vertexbuffer.pos((double)guiScreen.width*90/getFOV(), (double)guiScreen.height*(1-90/getFOV()), 0.0D).tex((double)((float)guiScreen.width / 32.0F), (double)0).color(64, 64, 64, 255).endVertex();
			vertexbuffer.pos((double)guiScreen.width*(1-90/getFOV()), (double)guiScreen.height*(1-90/getFOV()), 0.0D).tex(0.0D, 0).color(64, 64, 64, 255).endVertex();
			tessellator.draw();
			//

			framebufferIn.bindFramebuffer(false);
			GlStateManager.viewport(0, 0, framebufferIn.framebufferTextureWidth, framebufferIn.framebufferTextureHeight);

			Shader shader = new Shader();
			shader.createShaderProgram(this);

			GL20.glUseProgram(shader.getShaderProgram());

			//Setup view
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			GL11.glOrtho(-1, 1, -1, 1, -1, 1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();

			//Anti-aliasing
			int aaUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "antialiasing");
			GL20.glUniform1i(aaUniform, getAntialiasing());
			int pixelOffsetUniform;
			if (getAntialiasing() == 1) {
				pixelOffsetUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "pixelOffset[0]");
				GL20.glUniform2f(pixelOffsetUniform, 0, 0);
			}
			else if (getAntialiasing() == 4) {
				pixelOffsetUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "pixelOffset[0]");
				GL20.glUniform2f(pixelOffsetUniform, -0.25f/mc.displayWidth, -0.25f/mc.displayHeight);
				pixelOffsetUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "pixelOffset[1]");
				GL20.glUniform2f(pixelOffsetUniform, 0.25f/mc.displayWidth, -0.25f/mc.displayHeight);
				pixelOffsetUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "pixelOffset[2]");
				GL20.glUniform2f(pixelOffsetUniform, -0.25f/mc.displayWidth, 0.25f/mc.displayHeight);
				pixelOffsetUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "pixelOffset[3]");
				GL20.glUniform2f(pixelOffsetUniform, 0.25f/mc.displayWidth, 0.25f/mc.displayHeight);
			}
			else if (getAntialiasing() == 16) {
				float left = (-0.5f+0.125f)/mc.displayWidth;
				float top = (-0.5f+0.125f)/mc.displayHeight;
				float right = 0.25f/mc.displayWidth;
				float down = 0.25f/mc.displayHeight;
				for (int y = 0; y < 4; y++) {
					for (int x = 0; x < 4; x++) {
						pixelOffsetUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "pixelOffset[" + (y*4+x) + "]");
						GL20.glUniform2f(pixelOffsetUniform, left + right*x, top + down*y);
					}
				}
			}

			int texFrontUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "texFront");
			GL20.glUniform1i(texFrontUniform, 0);
			int texBackUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "texBack");
			GL20.glUniform1i(texBackUniform, 1);
			int texLeftUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "texLeft");
			GL20.glUniform1i(texLeftUniform, 2);
			int texRightUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "texRight");
			GL20.glUniform1i(texRightUniform, 3);
			int texTopUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "texTop");
			GL20.glUniform1i(texTopUniform, 4);
			int texBottomUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "texBottom");
			GL20.glUniform1i(texBottomUniform, 5);
			int fovxUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "fovx");
			GL20.glUniform1f(fovxUniform, getFOV());
			int fovyUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "fovy");
			GL20.glUniform1f(fovyUniform, getFOV()*Display.getHeight()/Display.getWidth());
			int backgroundUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "backgroundColor");
			GL20.glUniform4f(backgroundUniform, 0, 0, 0, 1);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, shader.getVbo());
			GL20.glEnableVertexAttribArray(0);
			GL20.glVertexAttribPointer(0, 2, GL11.GL_BYTE, false, 0, 0L);
			for (int i = 0; i < 6; i++) {
				GL13.glActiveTexture(GL13.GL_TEXTURE0+i);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
			}
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL20.glDisableVertexAttribArray(0);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			//Reset view
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPopMatrix();

			//unbind textures
			for (int i = 5; i >= 0; i--) {
				GL13.glActiveTexture(GL13.GL_TEXTURE0+i);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			}

			//Unbind shader
			GL20.glUseProgram(0);
			shader.deleteShaderProgram();
			framebuffer.deleteFramebuffer();
			framebufferIn.bindFramebuffer(false);
		}
	}
	
	@Override
	public void renderWorld(EntityRenderer er, Minecraft mc, Framebuffer framebuffer, Shader shader,
			int[] framebufferTextures, float partialTicks, long finishTimeNano, int width, int height, float sizeIncrease) {
		if (getFOV() >= 90) {
			super.renderWorld(er, mc, framebuffer, shader, framebufferTextures, partialTicks, finishTimeNano, width, height, sizeIncrease);
		} else {
			setPlayerRotation(mc);
			
			//clear the primary framebuffer
			mc.getFramebuffer().framebufferClear();
			//clear the secondary framebuffer
			framebuffer.framebufferClear();
			//bind the secondary framebuffer
			framebuffer.bindFramebuffer(false);
			
			mc.displayWidth = (int)(height*sizeIncrease);
			mc.displayHeight = (int)(height*sizeIncrease); //Must be square
			
			RenderUtil.partialWidth = mc.displayWidth;
			RenderUtil.partialHeight = mc.displayHeight;
			
			RenderUtil.render360 = true;
			
			OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, framebufferTextures[0], 0);
			GlStateManager.bindTexture(0);
			RenderUtil.renderPass = 0;
			
			float fov = mc.gameSettings.fovSetting;
			mc.gameSettings.fovSetting = getFOV();
			renderPass = 0;
			er.renderWorldPass(2, partialTicks, finishTimeNano);
			mc.gameSettings.fovSetting = fov;
			
			resetPlayerRotation();
			
			//reset displayWidth and displayHeight to the primary framebuffer dimensions
			mc.displayWidth = width;
			mc.displayHeight = height;
			
			//reset viewport to full screen
			GlStateManager.viewport(0, 0, width, height);
			mc.getFramebuffer().bindFramebuffer(false);
			
			if (!getResizeGui() || mc.gameSettings.hideGUI) {
				GL20.glUseProgram(shader.getShaderProgram());
				int cursorUniform = GL20.glGetUniformLocation(shader.getShaderProgram(), "drawCursor");
				GL20.glUniform1i(cursorUniform, 0);
				runShader(er, mc, framebuffer, shader, framebufferTextures);
			}
		}
	}
	
	@Override
	public boolean replaceLoadingScreen() {
		return true;
	}
	
	@Override
	public float getFOV() {
		float fov = this.fov;
		if (fov == 0)
			fov = 0.00001f;
		if (RenderUtil.KEY_ZOOM.isKeyDown()) {
			return fov / zoom;
		} else {
			return fov;
		}
	}
	
	@Override
	public boolean lockDefaultFOV() {
		return getFOV() >= 90;
	}
	
	@Override
	public float getQuality() {
		float quality;
		if (getFOV() < 270) {
			quality = super.getQuality()*2f;
			if (lastFOV >= 270) {
				RenderUtil.forceReload();
				quality = super.getQuality();
			}
		} else {
			quality = super.getQuality();
			if (lastFOV < 270) {
				RenderUtil.forceReload();
				quality = super.getQuality()*2f;
			}
		}
		lastFOV = getFOV();
		return quality;
	}
	
	@Override
	public boolean getRenderHand() {
		return renderPass == 0 && renderHand;
	}
}
