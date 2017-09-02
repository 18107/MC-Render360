package core.render360.coretransform;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.11")
@IFMLLoadingPlugin.TransformerExclusions(value = "core.render360.coretransform.")
@IFMLLoadingPlugin.Name(CoreLoader.MOD_NAME)
@IFMLLoadingPlugin.SortingIndex(value = 999)
public class CoreLoader implements IFMLLoadingPlugin {
	
	public static final String MOD_ID = "render360core";
    public static final String MOD_NAME = "Render 360 core";
    public static final String MOD_VERSION = "1.0";
	public static boolean isObfuscated;
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{CoreTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		isObfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
