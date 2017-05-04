package mod.render360.coretransform.render;

import java.util.List;

import org.lwjgl.opengl.GL20;

import mod.render360.coretransform.Shader;
import mod.render360.coretransform.gui.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.shader.Framebuffer;

public class Equirectangular extends RenderMethod {
	
	private final String fragmentShader = "#version 130//\n\n\n#define M_PI 3.14159265//\n\n\n/* This comes interpolated from the vertex shader */\nin vec2 texcoord;\n\n/* The 6 textures to be rendered */\nuniform sampler2D texFront;\nuniform sampler2D texBack;\nuniform sampler2D texLeft;\nuniform sampler2D texRight;\nuniform sampler2D texTop;\nuniform sampler2D texBottom;\n\nuniform int antialiasing;\n\nuniform vec2 pixelOffset[16];\n\nuniform float fovx;\n\nuniform vec2 cursorPos;\n\nuniform bool drawCursor;\n\nuniform bool drawCircle;\n\nuniform vec2 rotation;\n\nout vec4 color;\n\nvec3 rotate(vec3 ray, vec2 angle) {\n  \n  //rotate y\n\n  float y = -sin(angle.y)*ray.z;\n  float z = cos(angle.y)*ray.z;\n  ray.y = y;\n  ray.z = z;\n  \n  //rotate x\n\n  float x = -sin(angle.x)*ray.z;\n  z = cos(angle.x)*ray.z;\n  ray.x = x;\n  ray.z = z;\n  \n  return ray;\n}\n\nvec3 rotate2(vec3 ray, vec2 angle) {\n	//rotate x\n\n	float x = cos(angle.x)*ray.x - sin(angle.x)*ray.z;\n	float z = cos(angle.x)*ray.z + sin(angle.x)*ray.x;\n	ray.x = x;\n	ray.z = z;\n	\n	//rotate y\n\n	float y = cos(angle.y)*ray.y - sin(angle.y)*ray.z;\n	z = cos(angle.y)*ray.z + sin(angle.y)*ray.y;\n	ray.y = y;\n	ray.z = z;\n	\n	return ray;\n}\n\nvoid main(void) {\n  /* Ray-trace a cube */\n	\n	//Anti-aliasing\n	vec4 colorN[16];\n	\n	for (int loop = 0; loop < pow(4, antialiasing); loop++) {\n		\n		//create ray\n\n		vec3 ray = vec3(0, 0, -1);\n		\n		//rotate ray\n\n		ray = rotate(ray, vec2((texcoord.x+pixelOffset[loop].x-0.5)*2*M_PI*fovx/360, (texcoord.y+pixelOffset[loop].y-0.5)*M_PI*fovx/360)); //x (-pi to pi), y (-pi/2 to pi/2\n\n		ray = rotate2(ray, vec2(-rotation.x*M_PI/180, rotation.y*M_PI/180));\n		\n		//find which side to use\n\n		if (abs(ray.x) > abs(ray.y)) {\n			if (abs(ray.x) > abs(ray.z)) {\n				if (ray.x > 0) {\n					//right\n\n					float x = ray.z / ray.x;\n					float y = ray.y / ray.x;\n					colorN[loop] = vec4(texture(texRight, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//left\n\n					float x = -ray.z / -ray.x;\n					float y = ray.y / -ray.x;\n					colorN[loop] = vec4(texture(texLeft, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		} else {\n			if (abs(ray.y) > abs(ray.z)) {\n				if (ray.y > 0) {\n					//top\n\n					float x = ray.x / ray.y;\n					float y = ray.z / ray.y;\n					colorN[loop] = vec4(texture(texTop, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//bottom\n\n					float x = ray.x / -ray.y;\n					float y = -ray.z / -ray.y;\n					colorN[loop] = vec4(texture(texBottom, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		}\n		\n		if (drawCursor) {\n			vec2 normalAngle = cursorPos*2 - 1;\n			float x = ray.x / -ray.z;\n			float y = ray.y / -ray.z;\n			if (x <= normalAngle.x + 0.01 && y <= normalAngle.y + 0.01 &&\n				x >= normalAngle.x - 0.01 && y >= normalAngle.y - 0.01 &&\n				ray.z < 0) {\n				colorN[loop] = vec4(1, 1, 1, 1);\n			}\n		} else if (drawCircle) {\n			float phi = (texcoord.y+pixelOffset[loop].y-0.5)*M_PI;\n			float lambda = (texcoord.x+pixelOffset[loop].x-0.5-rotation.x/360)*2*M_PI;\n			float z = cos(phi)*cos(lambda);\n			float y = sin(phi)*cos(rotation.y*M_PI/180+M_PI/2) + z*sin(rotation.y*M_PI/180+M_PI/2);\n			float radius = asin(1-y);\n			if (radius < 0.0013 && radius > 0.0007) {\n				colorN[loop] = vec4(0, 0, 0, 1);\n			}\n		}\n	}\n	\n	if (antialiasing == 2) {\n	  vec4 corner[4];\n	  corner[0] = mix(mix(colorN[0], colorN[1], 2.0/3.0), mix(colorN[4], colorN[5], 3.0/5.0), 5.0/8.0);\n	  corner[1] = mix(mix(colorN[3], colorN[2], 2.0/3.0), mix(colorN[7], colorN[6], 3.0/5.0), 5.0/8.0);\n	  corner[2] = mix(mix(colorN[12], colorN[13], 2.0/3.0), mix(colorN[8], colorN[9], 3.0/5.0), 5.0/8.0);\n	  corner[3] = mix(mix(colorN[15], colorN[14], 2.0/3.0), mix(colorN[11], colorN[10], 3.0/5.0), 5.0/8.0);\n	  color = mix(mix(corner[0], corner[1], 0.5), mix(corner[2], corner[3], 0.5), 0.5);\n	}\n	else if (antialiasing == 1) {\n		color = mix(mix(colorN[0], colorN[1], 0.5), mix(colorN[2], colorN[3], 0.5), 0.5);\n	}\n	else { //if antialiasing == 0\n		color = colorN[0];\n	}\n}\n";
	
	private float fov = 360;
	
	private boolean drawCircle = false;
	
	private boolean stabilizeYaw = false;
	private boolean stabilizePitch = false;
	
	@Override
	public String getName() {
		return "Equirectangular";
	}
	
	@Override
	public String getFragmentShader() {
		return this.fragmentShader;
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
	public boolean replaceLoadingScreen() {
		return true;
	}
	
	@Override
	public void addButtonsToGui(List<GuiButton> buttonList, int width, int height) {
		super.addButtonsToGui(buttonList, width, height);
		//buttonList.add(new Slider(new Responder(), 18104, width / 2 - 180, height / 6 + 24, 360, 20, "FOV", 0f, 360f, fov, 1f, null));
		buttonList.add(new GuiButton(18107, width / 2 - 155, height / 6 + 96, 150, 20, "Stabilize Yaw: " + (stabilizeYaw ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18108, width / 2 + 5, height / 6 + 96, 150, 20, "Stabilize Pitch: " + (stabilizePitch ? "ON" : "OFF")));
		buttonList.add(new GuiButton(18109, width / 2 - 155, height / 6 + 120, 150, 20, "Draw Circle: " + (drawCircle ? "ON" : "OFF")));
	}
	
	@Override
	public void onButtonPress(GuiButton button) {
		super.onButtonPress(button);
		//Draw circle
		if (button.id == 18107) {
			stabilizeYaw = !stabilizeYaw;
			button.displayString = "Stabilize Yaw: " + (stabilizeYaw ? "ON" : "OFF");
		}
		else if (button.id == 18108) {
			stabilizePitch = !stabilizePitch;
			button.displayString = "Stabilize Pitch: " + (stabilizePitch ? "ON" : "OFF");
		}
		else if (button.id == 18109) {
			drawCircle = !drawCircle;
			button.displayString = "Draw Circle: " + (drawCircle ? "ON" : "OFF");
		}
	}
	
	@Override
	public float getFOV() {
		return fov;
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
