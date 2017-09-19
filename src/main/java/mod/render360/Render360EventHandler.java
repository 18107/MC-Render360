package mod.render360;

import org.lwjgl.opengl.Display;

import core.render360.Render360Event;
import mod.render360.gui.SettingsGui;
import mod.render360.render.RenderMethod;
import mod.render360.render.Standard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Optional;

public class Render360EventHandler {
	
	private boolean disabled = false;

	@SubscribeEvent
	public void initGui(InitGuiEvent.Post e) {
		if (!disabled) {
			if (e.getGui() instanceof GuiOptions) {
				e.getButtonList().add(new GuiButton(18107,e.getGui().width / 2 - 155,
						e.getGui().height / 6 + 12, 150, 20, "Render 360 Settings"));
			}
		}
	}
	
	@SubscribeEvent
	public void actionPerformed(ActionPerformedEvent.Pre e) {
		if (!disabled) {
			if (e.getGui() instanceof GuiOptions && e.getButton().id == 18107) {
				e.getGui().mc.gameSettings.saveOptions();
				e.getGui().mc.displayGuiScreen(new SettingsGui(e.getGui()));
			}
		}
	}
	
	@SubscribeEvent
	public void getFOVModifier(EntityViewRenderEvent.FOVModifier e) {
		if (!disabled) {
			if (RenderUtil.renderMethod.lockDefaultFOV()) {
				e.setFOV(90);
			}
		}
	}
	
	@SubscribeEvent
	public void rotateCamera(Render360Event.RotateCameraEvent e) {
		if (!disabled) {
			RenderUtil.renderMethod.rotateCamera(e);
		} else {
			GlStateManager.translate(0, 0, 0.05f); //FIXME
		}
	}
	
	@SubscribeEvent
	public void rotatePlayer(EntityViewRenderEvent.CameraSetup e) {
		if (!disabled) {
			RenderUtil.renderMethod.rotatePlayer();
		}
	}
	
	@SubscribeEvent
	public void worldLoad(Render360Event.DimensionLoadEvent e) {
		if (!disabled) {
			RenderUtil.onWorldLoad();
		}
	}
	
	@SubscribeEvent
	public void worldUnload(Render360Event.DimensionUnloadEvent e) {
		if (!disabled) {
			RenderUtil.onWorldUnload();
		}
	}
	
	@SubscribeEvent
	public void setLoadingProgressBackground(Render360Event.LoadingProgressBackgroundEvent e) {
		if (!disabled) {
			if (RenderUtil.renderMethod.replaceLoadingScreen()) {
				e.setCanceled(true);
				RenderUtil.renderMethod.renderLoadingScreen(e.guiScreen, e.framebuffer);
			}
		}
	}
	
	@SubscribeEvent
	public void drawWorldBackground(Render360Event.DrawWorldBackgroundEvent e) {
		if (!disabled) {
			if (RenderUtil.renderMethod.getResizeGui()) {
				e.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void drawBackground(Render360Event.DrawBackgroundEvent e) {
		if (!disabled) {
			if (RenderUtil.renderMethod.replaceLoadingScreen()) {
				e.setCanceled(true);
				RenderUtil.renderMethod.renderLoadingScreen(e.guiScreen);
			}
		}
	}
	
	@SubscribeEvent
	public void renderOverlayPre(Render360Event.RenderOverlayEvent.Pre e) {
		if (!disabled) {
			RenderUtil.renderGuiStart();
		}
	}
	
	@SubscribeEvent
	public void renderOverlayPost(Render360Event.RenderOverlayEvent.Post e) {
		if (!disabled) {
			RenderUtil.renderGuiEnd();
		}
	}
	
	@SubscribeEvent
	public void drawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre e) {
		if (!disabled) {
			RenderUtil.renderGuiStart2();
		}
	}
	
	@SubscribeEvent
	public void drawScreenPos(GuiScreenEvent.DrawScreenEvent.Post e) {
		if (!disabled) {
			RenderUtil.renderGuiEnd2();
		}
	}
	
	@SubscribeEvent
	public void renderWorld(Render360Event.RenderWorldEvent e) {
		if (!disabled) {
			Minecraft mc = Minecraft.getMinecraft();
			e.setCanceled(true);
			RenderUtil.setupRenderWorld(mc.entityRenderer, mc, e.partialTicks, e.finishTimeNano);
		}
	}
	
	@SubscribeEvent
	public void setViewport(Render360Event.SetViewportEvent e) {
		if (!disabled) {
			e.x = 0;
			e.y = 0;
			e.width = RenderUtil.partialWidth;
			e.height = RenderUtil.partialHeight;
		}
	}
	
	@SubscribeEvent
	public void renderHand(RenderHandEvent e) {
		if (!disabled) {
			if (RenderUtil.render360) {
				e.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void sunsetColor(Render360Event.SunsetFogEvent e) {
		if (!disabled) {
			if (RenderUtil.render360) {
				e.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void drawNameplate(Render360Event.DrawNameplateEvent e) {
		if (!disabled) {
			if (RenderUtil.render360) {
				e.yaw = (float) -(Math.atan(e.x/e.z)*180/Math.PI);
				if (e.z < 0) {
					e.yaw += 180;
				}
				float distance = (float) (Math.sqrt(e.x*e.x + e.z*e.z));
				e.pitch = (float) -(Math.atan((e.y-Minecraft.getMinecraft().getRenderViewEntity().height)/distance)*180/Math.PI);
			}
		}
	}
	
	@SubscribeEvent
	public void rotateParticle(Render360Event.RotateParticleEvent e) {
		if (!disabled) {
			if (RenderUtil.render360) {
				float posX = e.posX;
				float posY = e.posY - Minecraft.getMinecraft().thePlayer.eyeHeight;
				float posZ = e.posZ;

				float hDist = (float) (Math.sqrt(posZ*posZ + posX*posX));
				float dist = (float) (Math.sqrt(posZ*posZ + posY*posY + posX*posX));

				e.rotationX = posZ/hDist;
				e.rotationZ = 1-Math.abs(posY/dist);
				e.rotationYZ = -posX/hDist;
				e.rotationXY = -posY/dist * posX/hDist;
				e.rotationXZ = -posY/dist * posZ/hDist;
			}
		}
	}
	
	@Optional.Method(modid="replaymod")
	@SubscribeEvent
	public void replayModRenderPre(com.replaymod.render.events.ReplayRenderEvent.Pre e) {
		Log.info("Pre render");
		disabled = true;
	}
	
	@Optional.Method(modid="replaymod")
	@SubscribeEvent
	public void replayModRenderPost(com.replaymod.render.events.ReplayRenderEvent.Post e) {
		Log.info("Post render");
		disabled = false;
	}
}
