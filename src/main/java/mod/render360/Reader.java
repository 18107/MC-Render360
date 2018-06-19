package mod.render360;

import java.io.IOException;
import java.io.InputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class Reader {

	public static String read(String resourceIn) {
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		IResource resource = null;
		try {
			resource = resourceManager.getResource(new ResourceLocation(resourceIn));
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		InputStream is = resource.getInputStream();
		if (is == null) {
			Log.info("Shader not found");
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		int i;
		
		try {
			i = is.read();
			while (i != -1) {
				sb.append((char) i);
				i = is.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "";
			
		}
		
		return sb.toString();
	}
}
