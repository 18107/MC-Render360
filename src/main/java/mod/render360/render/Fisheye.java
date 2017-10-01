package mod.render360.render;

import java.util.List;

import org.lwjgl.opengl.GL20;

import mod.render360.gui.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.renderer.EntityRenderer;

public class Fisheye extends RenderMethod {
	
	public boolean resizeGui = false;
	
	public float quality = 1;
	
	public int antialiasing = 16;
	
	public boolean skyBackground = false;

	public int fisheyeType = 3;
	public boolean fullFrame = false;
	
	public float fov = 360;

	@Override
	public String getName() {
		return "Fisheye";
	}

	@Override
	public String getFragmentShader() {
		return "#version 130//\n\n#define M_PI 3.14159265//\n\n/* This comes interpolated from the vertex shader */\nin vec2 texcoord;\n/* The 6 textures to be rendered */\nuniform sampler2D texFront;\nuniform sampler2D texBack;\nuniform sampler2D texLeft;\nuniform sampler2D texRight;\nuniform sampler2D texTop;\nuniform sampler2D texBottom;\nuniform int antialiasing;\nuniform vec2 pixelOffset[16];\nuniform float fovx;\nuniform float fovy;\nuniform int fisheyeType;\nuniform bool fullFrame;\nuniform vec4 backgroundColor;\nuniform vec2 cursorPos;\nuniform bool drawCursor;\nout vec4 color;\nvoid main(void) {\n  /* Ray-trace a cube */\n	//Anti-aliasing\n	vec4 colorN[16];\n	for (int loop = 0; loop < antialiasing; loop++) {\n		//create ray\n\n		vec3 ray = vec3(0, 0, -1);\n		//fisheye stuff\n		\n		//point relative to center [0..1] -> [-1..1]\n		float x = (texcoord.x+pixelOffset[loop].x)*2-1;\n		float y = (texcoord.y+pixelOffset[loop].y)*2-1;\n		\n		//scale from square view to window shape view //fcontain\n		float aspectRatio = fovx/fovy;\n		if (aspectRatio > 1) {\n			x *= aspectRatio;\n		} else {\n			y /= aspectRatio;\n		}\n		\n		if (fullFrame) {\n			//scale circle radius [1] up to screen diagonal radius [sqrt(2) or higher]\n			if (aspectRatio > 1) {\n				x /= sqrt(aspectRatio*aspectRatio+1);\n				y /= sqrt(aspectRatio*aspectRatio+1);\n			} else {\n				x /= sqrt((1/aspectRatio)*(1/aspectRatio)+1);\n				y /= sqrt((1/aspectRatio)*(1/aspectRatio)+1);\n			}\n		} else {\n			//only draw center circle\n			if (x*x+y*y > 1) {\n				color = backgroundColor;\n				return;\n			}\n		}\n		\n		//max theta as limited by fov\n		float fovTheta = fovx*M_PI/360;\n		float r;\n		float theta;\n		if (fisheyeType == 4) {//stereographic\n			//forward: r=2f*tan(theta/2)\n			float maxr = 2*tan(fovTheta*0.5);\n				x *= maxr;\n				y *= maxr;\n				r = sqrt(x*x+y*y);\n			//inverse:\n			theta = 2*atan(r*0.5);\n		} else if (fisheyeType == 3) {//equidistant\n			//This is the x scale of the theta= equation. Not related to fov.\n			//it's the result of the forward equation with theta=pi\n			//forward: r=f*theta\n			float maxr = fovTheta;\n				//scale to angle (equidistant) [-1..1] -> [-pi..pi] (orthographic [-0.5..0.5] -> [-pi/2..pi/2]\n				x *= maxr;\n				y *= maxr;\n				//angle from forward <=abs(pi) or <=abs(pi/2)\n				r = sqrt(x*x+y*y);\n			//inverse:\n			theta = r;\n		} else if (fisheyeType == 2) {//equisolid\n			//forward: r=2f*sin(theta/2)\n			float maxr = 2*sin(fovTheta*0.5);\n				x *= maxr;\n				y *= maxr;\n				r = sqrt(x*x+y*y);\n			//inverse:\n			theta = 2*asin(r*0.5);\n		} else if (fisheyeType == 1) {//thoby\n			//it starts shrinking near max fov without this - 256.68 degrees\n			fovTheta = min(fovTheta, M_PI*0.713);\n			\n			//forward: r=1.47*f*sin(0.713*theta)\n			float maxr = 1.47*sin(0.713*fovTheta);\n				x *= maxr;\n				y *= maxr;\n				r = sqrt(x*x+y*y);\n			//inverse:\n			theta = asin(r/1.47)/0.713;\n		} else {// if (fisheyeType == 0) {//orthographic\n			//this projection has a mathematical limit at hemisphere\n			fovTheta = min(fovTheta, M_PI*0.5);\n		\n			//forward: r=f*sin(theta)\n			float maxr = sin(fovTheta);\n				x *= maxr;\n				y *= maxr;\n				r = sqrt(x*x+y*y);\n			//inverse:\n			theta = asin(r);\n		}\n		//rotate ray\n		float s = sin(theta);\n		ray = vec3(x/r*s, y/r*s, -cos(theta));\n		//find which side to use\n\n		if (abs(ray.x) > abs(ray.y)) {\n			if (abs(ray.x) > abs(ray.z)) {\n				if (ray.x > 0) {\n					//right\n\n					float x = ray.z / ray.x;\n					float y = ray.y / ray.x;\n					colorN[loop] = vec4(texture(texRight, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//left\n\n					float x = -ray.z / -ray.x;\n					float y = ray.y / -ray.x;\n					colorN[loop] = vec4(texture(texLeft, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		} else {\n			if (abs(ray.y) > abs(ray.z)) {\n				if (ray.y > 0) {\n					//top\n\n					float x = ray.x / ray.y;\n					float y = ray.z / ray.y;\n					colorN[loop] = vec4(texture(texTop, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//bottom\n\n					float x = ray.x / -ray.y;\n					float y = -ray.z / -ray.y;\n					colorN[loop] = vec4(texture(texBottom, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		}\n		if (drawCursor) {\n			vec2 normalAngle = cursorPos*2 - 1;\n			float x = ray.x / -ray.z;\n			float y = ray.y / -ray.z;\n			if (x <= normalAngle.x + 0.01 && y <= normalAngle.y + 0.01 &&\n				x >= normalAngle.x - 0.01 && y >= normalAngle.y - 0.01 &&\n				ray.z < 0) {\n				colorN[loop] = vec4(1, 1, 1, 1);\n			}\n		}\n	}\n	if (antialiasing == 16) {\n	  vec4 corner[4];\n	  corner[0] = mix(mix(colorN[0], colorN[1], 2.0/3.0), mix(colorN[4], colorN[5], 3.0/5.0), 5.0/8.0);\n	  corner[1] = mix(mix(colorN[3], colorN[2], 2.0/3.0), mix(colorN[7], colorN[6], 3.0/5.0), 5.0/8.0);\n	  corner[2] = mix(mix(colorN[12], colorN[13], 2.0/3.0), mix(colorN[8], colorN[9], 3.0/5.0), 5.0/8.0);\n	  corner[3] = mix(mix(colorN[15], colorN[14], 2.0/3.0), mix(colorN[11], colorN[10], 3.0/5.0), 5.0/8.0);\n	  color = mix(mix(corner[0], corner[1], 0.5), mix(corner[2], corner[3], 0.5), 0.5);\n	}\n	else if (antialiasing == 4) {\n		color = mix(mix(colorN[0], colorN[1], 0.5), mix(colorN[2], colorN[3], 0.5), 0.5);\n	}\n	else { //if antialiasing == 1\n		color = colorN[0];\n	}\n}\n";
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

	@Override
	public float getFOV() {
		return fov;
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
}
