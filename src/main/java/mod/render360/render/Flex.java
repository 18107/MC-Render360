package mod.render360.render;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

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

	@Override
	public String getName() {
		return "Flex";
	}

	@Override
	public String getFragmentShader() {
		return "#version 130\n\n#define M_PI 3.14159265\n#define M_E 2.718281828\n\n/* This comes interpolated from the vertex shader */\nin vec2 texcoord;\n\n/* The 6 textures to be rendered */\nuniform sampler2D texFront;\nuniform sampler2D texBack;\nuniform sampler2D texLeft;\nuniform sampler2D texRight;\nuniform sampler2D texTop;\nuniform sampler2D texBottom;\n\nuniform int antialiasing;\n\nuniform vec2 pixelOffset[16];\n\nuniform float fovx;\nuniform float fovy;\n\nout vec4 color;\n\nvec3 rotate(vec3 ray, vec2 angle) {\n  \n  //rotate y\n  float y = -sin(angle.y)*ray.z;\n  float z = cos(angle.y)*ray.z;\n  ray.y = y;\n  ray.z = z;\n  \n  //rotate x\n  float x = -sin(angle.x)*ray.z;\n  z = cos(angle.x)*ray.z;\n  ray.x = x;\n  ray.z = z;\n  \n  return ray;\n}\n\nvec3 passthrough(vec2 coord) {\n	return vec3(coord*2-1, -1);\n}\n\n//copied from github.com/shaunlebron/flex-fov\nvec3 latlon_to_ray(float lat, float lon) {\n  return vec3(\n    sin(lon)*cos(lat),\n    sin(lat),\n    -cos(lon)*cos(lat)\n  );\n}\n\nvec3 mercator_inverse(vec2 lenscoord) {\n  float lon = lenscoord.x;\n  float lat = atan(sinh(lenscoord.y*fovy/fovx));\n  return latlon_to_ray(lat, lon);\n}\nvec2 mercator_forward(float lat, float lon) {\n  float x = lon;\n  float y = log(tan(M_PI*0.25+lat*0.5));\n  return vec2(x,y);\n}\nvec3 mercator_ray(vec2 lenscoord) {\n  float scale = mercator_forward(0, radians(fovx)/2).x;\n  return mercator_inverse((lenscoord*2-1) * scale);\n}\n\nvec3 panini_inverse(vec2 lenscoord, float dist) {\n  float x = lenscoord.x;\n  float y = lenscoord.y*fovy/fovx;\n  float d = dist;\n  float k = x*x/((d+1)*(d+1));\n  float dscr = k*k*d*d - (k+1)*(k*d*d-1);\n  float clon = (-k*d+sqrt(dscr))/(k+1);\n  float S = (d+1)/(d+clon);\n  float lon = atan(x,S*clon);\n  float lat = atan(y,S);\n  return latlon_to_ray(lat, lon);\n}\nvec2 panini_forward(float lat, float lon, float dist) {\n  float d = dist;\n  float S = (d+1)/(d+cos(lon));\n  float x = S*sin(lon);\n  float y = S*tan(lat);\n  return vec2(x,y);\n}\nvec3 panini_ray(vec2 lenscoord, float dist) {\n  float scale = panini_forward(0, radians(fovx)/2, dist).x;\n  return panini_inverse((lenscoord*2-1) * scale, dist);\n}\n//end copy\n\nvoid main(void) {\n	/* Ray-trace a cube */\n	\n	//Anti-aliasing\n	vec4 colorN[16];\n	\n	for (int loop = 0; loop < antialiasing; loop++) {\n		\n		vec2 coord = texcoord + pixelOffset[loop];\n		\n		//create ray\n		vec3 ray;\n		\n		if (fovx < 90) {\n			ray = passthrough(vec2(coord.x, coord.y*fovy/fovx + (1-fovy/fovx)/2));\n		} else if (fovx <= 180) {\n			ray = panini_ray(coord, (fovx-90)/90);\n		} else if (fovx < 320) {\n			float linear = (fovx - 180)/ 140;\n			float expon = linear*pow(M_E, 1-linear);\n			ray = mix(panini_ray(coord, 1), mercator_ray(coord), expon);\n		} else {\n			ray = mercator_ray(coord);\n		}\n		\n		//find which side to use\n		if (abs(ray.x) > abs(ray.y)) {\n			if (abs(ray.x) > abs(ray.z)) {\n				if (ray.x > 0) {\n					//right\n					float x = ray.z / ray.x;\n					float y = ray.y / ray.x;\n					colorN[loop] = vec4(texture(texRight, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//left\n					float x = -ray.z / -ray.x;\n					float y = ray.y / -ray.x;\n					colorN[loop] = vec4(texture(texLeft, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		} else {\n			if (abs(ray.y) > abs(ray.z)) {\n				if (ray.y > 0) {\n					//top\n					float x = ray.x / ray.y;\n					float y = ray.z / ray.y;\n					colorN[loop] = vec4(texture(texTop, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//bottom\n					float x = ray.x / -ray.y;\n					float y = -ray.z / -ray.y;\n					colorN[loop] = vec4(texture(texBottom, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		}\n	}\n	\n	if (antialiasing == 16) {\n	  vec4 corner[4];\n	  corner[0] = mix(mix(colorN[0], colorN[1], 2.0/3.0), mix(colorN[4], colorN[5], 3.0/5.0), 5.0/8.0);\n	  corner[1] = mix(mix(colorN[3], colorN[2], 2.0/3.0), mix(colorN[7], colorN[6], 3.0/5.0), 5.0/8.0);\n	  corner[2] = mix(mix(colorN[12], colorN[13], 2.0/3.0), mix(colorN[8], colorN[9], 3.0/5.0), 5.0/8.0);\n	  corner[3] = mix(mix(colorN[15], colorN[14], 2.0/3.0), mix(colorN[11], colorN[10], 3.0/5.0), 5.0/8.0);\n	  color = mix(mix(corner[0], corner[1], 0.5), mix(corner[2], corner[3], 0.5), 0.5);\n	}\n	else if (antialiasing == 4) {\n		color = mix(mix(colorN[0], colorN[1], 0.5), mix(colorN[2], colorN[3], 0.5), 0.5);\n	}\n	else { //if antialiasing == 1\n		color = colorN[0];\n	}\n}";
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
			int cubeTexture, float partialTicks, long finishTimeNano, int width, int height, float sizeIncrease) {
		if (getFOV() >= 90) {
			super.renderWorld(er, mc, framebuffer, shader, cubeTexture, partialTicks, finishTimeNano, width, height, sizeIncrease);
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
			
			OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, cubeTexture, 0);
			GlStateManager.bindTexture(0);
			RenderUtil.renderPass = 0;
			
			float fov = mc.gameSettings.fovSetting;
			mc.gameSettings.fovSetting = getFOV();
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
				runShader(er, mc, framebuffer, shader, cubeTexture);
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
}
