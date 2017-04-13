package mod.render360.coretransform.render;

import java.util.List;

import mod.render360.coretransform.gui.Slider;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;

public class Equirectangular extends RenderMethod {
	
	private final String fragmentShader = "#version 130//\n\n\n#define M_PI 3.14159265//\n\n\n/* This comes interpolated from the vertex shader */\nin vec2 texcoord;\n\n/* The 6 textures to be rendered */\nuniform sampler2D texFront;\nuniform sampler2D texBack;\nuniform sampler2D texLeft;\nuniform sampler2D texRight;\nuniform sampler2D texTop;\nuniform sampler2D texBottom;\n\nuniform vec2 pixelOffset[4];\n\n//fovx\n\nuniform float fovx;\n\nuniform vec2 cursorPos;\n\nuniform bool drawCursor;\n\nout vec4 color;\n\nvec3 rotate(vec3 ray, vec2 angle) {\n  \n  //rotate y\n\n  float y = -sin(angle.y)*ray.z;\n  float z = cos(angle.y)*ray.z;\n  ray.y = y;\n  ray.z = z;\n  \n  //rotate x\n\n  float x = -sin(angle.x)*ray.z;\n  z = cos(angle.x)*ray.z;\n  ray.x = x;\n  ray.z = z;\n  \n  return ray;\n}\n\nvoid main(void) {\n  /* Ray-trace a cube */\n	\n	//Anti-aliasing\n	vec4 colorN[4];\n	\n	for (int loop = 0; loop < 4; loop++) {\n		\n		//create ray\n\n		vec3 ray = vec3(0, 0, -1);\n		\n		//rotate ray\n\n		ray = rotate(ray, vec2((texcoord.x+pixelOffset[loop].x-0.5)*2*M_PI*fovx/360, (texcoord.y+pixelOffset[loop].y-0.5)*M_PI*fovx/360)); //x (-pi to pi), y (-pi/2 to pi/2\n\n		\n		//find which side to use\n\n		if (abs(ray.x) > abs(ray.y)) {\n			if (abs(ray.x) > abs(ray.z)) {\n				if (ray.x > 0) {\n					//right\n\n					float x = ray.z / ray.x;\n					float y = ray.y / ray.x;\n					colorN[loop] = vec4(texture(texRight, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//left\n\n					float x = -ray.z / -ray.x;\n					float y = ray.y / -ray.x;\n					colorN[loop] = vec4(texture(texLeft, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		} else {\n			if (abs(ray.y) > abs(ray.z)) {\n				if (ray.y > 0) {\n					//top\n\n					float x = ray.x / ray.y;\n					float y = ray.z / ray.y;\n					colorN[loop] = vec4(texture(texTop, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//bottom\n\n					float x = ray.x / -ray.y;\n					float y = -ray.z / -ray.y;\n					colorN[loop] = vec4(texture(texBottom, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		}\n		\n		if (drawCursor) {\n			vec2 normalAngle = cursorPos*2 - 1;\n			float x = ray.x / -ray.z;\n			float y = ray.y / -ray.z;\n			if (x <= normalAngle.x + 0.01 && y <= normalAngle.y + 0.01 &&\n				x >= normalAngle.x - 0.01 && y >= normalAngle.y - 0.01 &&\n				ray.z < 0) {\n				colorN[loop] = vec4(1, 1, 1, 1);\n			}\n		}\n	}\n	\n	color = mix(mix(colorN[0], colorN[1], 0.5), mix(colorN[2], colorN[3], 0.5), 0.5);\n}\n";
	
	private float fov = 360;
	
	@Override
	public String getName() {
		return "Equirectangular";
	}
	
	@Override
	public String getFragmentShader() {
		return this.fragmentShader;
	}
	
	@Override
	public boolean replaceLoadingScreen() {
		return true;
	}
	
	@Override
	public void addButtonsToGui(List<GuiButton> buttonList, int width, int height) {
		super.addButtonsToGui(buttonList, width, height);
		buttonList.add(new Slider(new Responder(), 18104, width / 2 - 180, height / 6 + 24, 360, 20, "FOV", 0f, 360f, fov, 1f, null));
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
