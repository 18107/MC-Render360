package core.render360.coretransform;

import core.render360.Render360Event;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.common.MinecraftForge;

public class TransformerUtil {

	public static void onWorldLoad(WorldClient worldClient) {
		if (worldClient != null) {
			MinecraftForge.EVENT_BUS.post(new Render360Event.DimensionLoadEvent());
		} else {
			MinecraftForge.EVENT_BUS.post(new Render360Event.DimensionUnloadEvent());
		}
	}
}
