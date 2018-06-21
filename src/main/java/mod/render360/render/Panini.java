package mod.render360.render;

import mod.render360.Reader;

public class Panini extends RenderMethod {
	
	public float fov = 180;
	
	public float quality = 1;
	
	public int antialiasing = 16;
	
	public boolean resizeGui = false;
	
	public boolean renderHand = false;
	
	public float dist = 1;

	@Override
	public String getName() {
		return "Panini";
	}

	@Override
	public String getFragmentShader() {
		return Reader.read("render360:shaders/panini.fs");
	}
	
	@Override
	public float getFOV() {
		return fov;
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
	public boolean getRenderHand() {
		return renderPass == 0 && renderHand;
	}
	
	@Override
	public float getDist() {
		return dist;
	}
}
