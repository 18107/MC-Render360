package mod.render360.render;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import mod.render360.RenderUtil;
import mod.render360.Shader;
import mod.render360.gui.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;

public class Flex extends RenderMethod {
	
	private float fov = 360;
	private float lastFOV = 360;

	@Override
	public String getName() {
		return "Flex";
	}

	@Override
	public String getFragmentShader() {
		return "#version 130\n\n#define M_PI 3.14159265\n#define M_E 2.718281828\n\n/* This comes interpolated from the vertex shader */\nin vec2 texcoord;\n\n/* The 6 textures to be rendered */\nuniform sampler2D texFront;\nuniform sampler2D texBack;\nuniform sampler2D texLeft;\nuniform sampler2D texRight;\nuniform sampler2D texTop;\nuniform sampler2D texBottom;\n\nuniform int antialiasing;\n\nuniform vec2 pixelOffset[16];\n\nuniform float fovx;\nuniform float fovy;\n\nout vec4 color;\n\nvec3 rotate(vec3 ray, vec2 angle) {\n  \n  //rotate y\n  float y = -sin(angle.y)*ray.z;\n  float z = cos(angle.y)*ray.z;\n  ray.y = y;\n  ray.z = z;\n  \n  //rotate x\n  float x = -sin(angle.x)*ray.z;\n  z = cos(angle.x)*ray.z;\n  ray.x = x;\n  ray.z = z;\n  \n  return ray;\n}\n\nvec3 passthrough(vec2 coord) {\n	return vec3(coord*2-1, -1);\n}\n\n//copied from github.com/shaunlebron/flex-fov\nvec3 latlon_to_ray(float lat, float lon) {\n  return vec3(\n    sin(lon)*cos(lat),\n    sin(lat),\n    -cos(lon)*cos(lat)\n  );\n}\n\nvec3 mercator_inverse(vec2 lenscoord) {\n  float lon = lenscoord.x;\n  float lat = atan(sinh(lenscoord.y*fovy/fovx));\n  return latlon_to_ray(lat, lon);\n}\nvec2 mercator_forward(float lat, float lon) {\n  float x = lon;\n  float y = log(tan(M_PI*0.25+lat*0.5));\n  return vec2(x,y);\n}\nvec3 mercator_ray(vec2 lenscoord) {\n  float scale = mercator_forward(0, radians(fovx)/2).x;\n  return mercator_inverse((lenscoord*2-1) * scale);\n}\n\nvec3 panini_inverse(vec2 lenscoord, float dist) {\n  float x = lenscoord.x;\n  float y = lenscoord.y*fovy/fovx;\n  float d = dist;\n  float k = x*x/((d+1)*(d+1));\n  float dscr = k*k*d*d - (k+1)*(k*d*d-1);\n  float clon = (-k*d+sqrt(dscr))/(k+1);\n  float S = (d+1)/(d+clon);\n  float lon = atan(x,S*clon);\n  float lat = atan(y,S);\n  return latlon_to_ray(lat, lon);\n}\nvec2 panini_forward(float lat, float lon, float dist) {\n  float d = dist;\n  float S = (d+1)/(d+cos(lon));\n  float x = S*sin(lon);\n  float y = S*tan(lat);\n  return vec2(x,y);\n}\nvec3 panini_ray(vec2 lenscoord, float dist) {\n  float scale = panini_forward(0, radians(fovx)/2, dist).x;\n  return panini_inverse((lenscoord*2-1) * scale, dist);\n}\n//end copy\n\nvec3 equirectangular(vec2 coord) {\n	return rotate(vec3(0, 0, -1), vec2((coord.x-0.5)*2*M_PI*fovx/360, (coord.y-0.5)*M_PI*fovx/360));\n}\n\nvoid main(void) {\n	/* Ray-trace a cube */\n	\n	//Anti-aliasing\n	vec4 colorN[16];\n	\n	for (int loop = 0; loop < antialiasing; loop++) {\n		\n		vec2 coord = texcoord + pixelOffset[loop];\n		\n		//create ray\n		vec3 ray;\n		\n		if (fovx < 90) { //TODO\n			ray = passthrough(vec2(coord.x, coord.y*fovy/fovx + (1-fovy/fovx)/2));\n		} else if (fovx <= 180) {\n			ray = panini_ray(coord, (fovx-90)/90);\n		} else if (fovx < 320) {\n			float linear = (fovx - 180)/ 140;\n			float expon = linear*pow(M_E, 1-linear);\n			ray = mix(panini_ray(coord, 1), mercator_ray(coord), expon);\n		} else {\n			ray = mercator_ray(coord);\n		}\n		\n		//find which side to use\n		if (abs(ray.x) > abs(ray.y)) {\n			if (abs(ray.x) > abs(ray.z)) {\n				if (ray.x > 0) {\n					//right\n					float x = ray.z / ray.x;\n					float y = ray.y / ray.x;\n					colorN[loop] = vec4(texture(texRight, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//left\n					float x = -ray.z / -ray.x;\n					float y = ray.y / -ray.x;\n					colorN[loop] = vec4(texture(texLeft, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		} else {\n			if (abs(ray.y) > abs(ray.z)) {\n				if (ray.y > 0) {\n					//top\n					float x = ray.x / ray.y;\n					float y = ray.z / ray.y;\n					colorN[loop] = vec4(texture(texTop, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//bottom\n					float x = ray.x / -ray.y;\n					float y = -ray.z / -ray.y;\n					colorN[loop] = vec4(texture(texBottom, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		}\n	}\n	\n	if (antialiasing == 16) {\n	  vec4 corner[4];\n	  corner[0] = mix(mix(colorN[0], colorN[1], 2.0/3.0), mix(colorN[4], colorN[5], 3.0/5.0), 5.0/8.0);\n	  corner[1] = mix(mix(colorN[3], colorN[2], 2.0/3.0), mix(colorN[7], colorN[6], 3.0/5.0), 5.0/8.0);\n	  corner[2] = mix(mix(colorN[12], colorN[13], 2.0/3.0), mix(colorN[8], colorN[9], 3.0/5.0), 5.0/8.0);\n	  corner[3] = mix(mix(colorN[15], colorN[14], 2.0/3.0), mix(colorN[11], colorN[10], 3.0/5.0), 5.0/8.0);\n	  color = mix(mix(corner[0], corner[1], 0.5), mix(corner[2], corner[3], 0.5), 0.5);\n	}\n	else if (antialiasing == 4) {\n		color = mix(mix(colorN[0], colorN[1], 0.5), mix(colorN[2], colorN[3], 0.5), 0.5);\n	}\n	else { //if antialiasing == 1\n		color = colorN[0];\n	}\n}";
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
			
			OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, framebufferTextures[0], 0);
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
				runShader(er, mc, framebuffer, shader, framebufferTextures);
			}
		}
	}
	
	@Override
	public boolean replaceLoadingScreen() {
		return true;
	}
	
	@Override
	public void addButtonsToGui(List<GuiButton> buttonList, int width, int height) {
		//super.addButtonsToGui(buttonList, width, height); TODO ?
		buttonList.add(new Slider(new Responder(), 18104, width / 2 - 180, height / 6 + 24, 360, 20, "FOV", 0f, 360f, fov, 1f, null));
	}
	
	@Override
	public float getFOV() {
		return fov != 0 ? fov : 0.00001f;
	}
	
	@Override
	public boolean lockDefaultFOV() {
		return fov >= 90;
	}
	
	@Override
	public float getQuality() {
		float quality;
		if (fov < 180) {
			quality = super.getQuality()*2f;
			if (lastFOV >= 180) {
				RenderUtil.forceReload();
				quality = super.getQuality();
			}
		} else {
			quality = super.getQuality();
			if (lastFOV < 180) {
				RenderUtil.forceReload();
				quality = super.getQuality()*2f;
			}
		}
		lastFOV = fov;
		return quality;
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

}
