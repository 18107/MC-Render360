package core.render360.coretransform.classtransformers;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import core.render360.coretransform.CLTLog;
import core.render360.coretransform.RenderUtil;
import core.render360.coretransform.TransformerUtil;
import core.render360.coretransform.classtransformers.name.ClassName;
import core.render360.coretransform.classtransformers.name.MethodName;
import core.render360.coretransform.classtransformers.name.Names;
import mod.render360.render.RenderMethod;
import mod.render360.render.Standard;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;

public class EntityRendererTransformer extends ClassTransformer {
	
	@Override
	public ClassName getClassName() {return Names.EntityRenderer;}

	@Override
	public MethodTransformer[] getMethodTransformers() {
		//TODO
		MethodTransformer hurtCameraEffectTransformer = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.EntityRenderer_hurtCameraEffect;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				CLTLog.info("begining at start of method " + getMethodName().debug());
				
				InsnList toInsert = new InsnList();
				LabelNode label = new LabelNode();
				
				//if (!(RenderUtil.renderMethod instanceof Standard)) {
				//	return;
				//}
				toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class),
						"renderMethod", "L" + Type.getInternalName(RenderMethod.class) + ";"));
				toInsert.add(new TypeInsnNode(INSTANCEOF, Type.getInternalName(Standard.class)));
				toInsert.add(new JumpInsnNode(IFNE, label));
				toInsert.add(new InsnNode(RETURN));
				toInsert.add(label);
				
				method.instructions.insert(toInsert);
			}
		};
		
		MethodTransformer transformSetupCameraTransform = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.EntityRenderer_setupCameraTransform;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				int count = 0;
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					if (instruction instanceof MethodInsnNode) {
						MethodInsnNode methodCall = (MethodInsnNode)instruction;
						if (methodCall.name.equals(Names.GLStateManager_loadIdentity.getShortName(obfuscated)) &&
								methodCall.desc.equals(Names.GLStateManager_loadIdentity.getDesc(obfuscated))) {
							count++;
							if (count == 2) {
								CLTLog.info("Found: " + Names.GLStateManager_loadIdentity.debug());
								method.instructions.insert(instruction, new MethodInsnNode(INVOKESTATIC,
										Type.getInternalName(TransformerUtil.class), "rotateCamera", "()V", false));
								break;
							}
						}
					}
				}
			}
		};
		
		MethodTransformer transformUpdateCameraAndRender = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.EntityRenderer_updateCameraAndRender;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					
					if (instruction.getOpcode() == SIPUSH &&
							instruction.getNext().getOpcode() == LDC) {
						CLTLog.info("Found SIPUSH in method " + getMethodName().debug());
						
						//go to renderGameOverlay
						instruction = method.instructions.get(
								method.instructions.indexOf(instruction) + 16);
						
						method.instructions.insertBefore(instruction, new MethodInsnNode(INVOKESTATIC,
								Type.getInternalName(TransformerUtil.class), "renderOverlayPre", "()V", false));
						
						//go to after method call
						instruction = method.instructions.get(
								method.instructions.indexOf(instruction) + 5);
						
						method.instructions.insertBefore(instruction, new MethodInsnNode(INVOKESTATIC,
								Type.getInternalName(TransformerUtil.class), "renderOverlayPost", "()V", false));
						
						break;
					}
				}
			}
		};
		
		MethodTransformer transformRenderWorld = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.EntityRenderer_renderWorld;
			}
			
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					
					if (instruction.getOpcode() == ALOAD &&
							instruction.getNext().getOpcode() == ICONST_2) {
						CLTLog.info("Found ALOAD in method " + getMethodName().debug());
						
						InsnList toInsert = new InsnList();
						
						// void RenderUtil.setupRenderWorld(EntityRenderer, mc, p_78471_1_, p_78471_2_);
						toInsert.add(new VarInsnNode(ALOAD, 0)); //this
						toInsert.add(new VarInsnNode(ALOAD, 0)); //this
						toInsert.add(new FieldInsnNode(GETFIELD, classNode.name, Names.EntityRenderer_mc.getFullName(), Names.EntityRenderer_mc.getDesc())); //mc
						toInsert.add(new VarInsnNode(FLOAD, 1));
						toInsert.add(new VarInsnNode(LLOAD, 2));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(RenderUtil.class), "setupRenderWorld", 
								"(L" + classNode.name + ";L" + Type.getInternalName(Minecraft.class) + ";FJ)V", false));
						
						//Insert method call
						method.instructions.insertBefore(instruction, toInsert);
						
						//Remove this.renderWorldPass(2, partialTicks, finishTimeNano);
						for (int i = 0; i < 4; i++) {
							method.instructions.remove(instruction.getNext());
						}
						method.instructions.remove(instruction);
						
						break;
					}
				}
			}
			
		};
		
		MethodTransformer transformRenderWorldPass = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.EntityRenderer_renderWorldPass;
			}
			
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					
					if (instruction.getOpcode() == LDC) {
						CLTLog.info("found LDC in method " + getMethodName().debug());
						
						InsnList toInsert = new InsnList();
						LabelNode clearNode = new LabelNode();
						
						for (int i = 0; i < 3; i++) {
							instruction = instruction.getNext();
						}
						
						//if optifine is installed
						if (instruction.getNext().getOpcode() == ILOAD) {
							
							for (int i = 0; i < 17; i++) {
								instruction = instruction.getNext();
							}
						}
						//assume no other coremods are installed
						
						//Change from GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
						//to GlStateManager.viewport(0, 0, RenderUtil.partialWidth, RenderUtil.partialHeight);
						for (int i = 0; i < 8; i++) {
							method.instructions.remove(instruction.getNext()); //remove 0, 0, this.mc.displayWidth, this.mc.displayHeight
						}
						toInsert.add(new InsnNode(ICONST_0));
						toInsert.add(new InsnNode(ICONST_0));
						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class), "partialWidth", "I"));
						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class), "partialHeight", "I"));
						instruction = instruction.getNext();
						method.instructions.insertBefore(instruction, toInsert);
						
						break;
					}
				}
				
				AbstractInsnNode instruction = method.instructions.getLast();
				CLTLog.info("reached end of method " + getMethodName().debug());
				
				
				instruction = instruction.getPrevious();
				LabelNode handNode = new LabelNode();
				InsnList toInsert = new InsnList();
				
				//set handNode
				method.instructions.insertBefore(instruction, handNode);
				
				for (int i = 0; i < 10+9; i++) {
					instruction = instruction.getPrevious();
				}
				
				//if optifine is installed
				if (instruction.getPrevious().getOpcode() == GOTO) {
					LabelNode newLabel = new LabelNode(); //Ignore handNode
					instruction = instruction.getNext().getNext().getNext(); //find this.renderHand
					//if (!RenderUtil.render360)
					toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class), "render360", "Z"));
					toInsert.add(new JumpInsnNode(IFNE, newLabel));
					method.instructions.insertBefore(instruction, toInsert);
					
					instruction = instruction.getNext().getNext().getNext(); //find end of this.renderHand
					method.instructions.insert(instruction, newLabel); //insert after this.renderHand
				}
				//assume no other coremods are installed
				else {
					//if (&& !RenderUtil.render360)
					toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class), "render360", "Z"));
					toInsert.add(new JumpInsnNode(IFNE, handNode));
					method.instructions.insertBefore(instruction, toInsert);
				}
			}
		};
		
		//Fix sunset color
		MethodTransformer updateFogColorTransformer = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.EntityRenderer_updateFogColor;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					
					if (instruction.getOpcode() == D2F &&
							instruction.getPrevious().getOpcode() == INVOKEVIRTUAL &&
							instruction.getNext().getOpcode() == FSTORE) {
						CLTLog.info("found D2F in method " + getMethodName().debug());
						
						//after float f5 = (float)entity.getLook(partialTicks).dotProduct(vec3d2);
						for (int i = 0; i < 3; i++) {
							instruction = instruction.getNext();
						}
						
						InsnList toInsert = new InsnList();
						LabelNode label = new LabelNode();
						
						//if (RenderUtil.renderMethod.getName() != "Standard") {
							//f5 = 1;
						//}
						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class),
								"renderMethod", "L" + Type.getInternalName(RenderMethod.class) + ";"));
						toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(RenderMethod.class),
								"getName", "()L" + Type.getInternalName(String.class) + ";", false));
						toInsert.add(new LdcInsnNode("Standard"));
						toInsert.add(new JumpInsnNode(IF_ACMPEQ, label));
						
						toInsert.add(new InsnNode(FCONST_1));
						toInsert.add(new VarInsnNode(FSTORE, 13)); //f5
						toInsert.add(label);
						
						method.instructions.insertBefore(instruction, toInsert);
						
						break;
					}
				}
			}
		};
		
		MethodTransformer drawNameplateTransformer = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.EntityRenderer_drawNameplate;
			}
			
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				CLTLog.info("begining at start of method " + getMethodName().debug());
				
				//viewerYaw = RenderUtil.setViewerYaw(x, z)
				InsnList toInsert = new InsnList();
				toInsert.add(new VarInsnNode(FLOAD, 2)); //x
				toInsert.add(new VarInsnNode(FLOAD, 4));
				toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(RenderUtil.class), "setViewerYaw", "(FF)F", false));
				toInsert.add(new VarInsnNode(FSTORE, 6)); //viewerYaw
				
				//viewerPitch = RenderUtil.setViewerPitch(x, y, z)
				toInsert.add(new VarInsnNode(FLOAD, 2)); //x
				toInsert.add(new VarInsnNode(FLOAD, 3)); //y
				toInsert.add(new VarInsnNode(FLOAD, 4)); //z
				toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(RenderUtil.class), "setViewerPitch", "(FFF)F", false));
				toInsert.add(new VarInsnNode(FSTORE, 7)); //viewerPitch
				
				method.instructions.insert(toInsert);
			}
		};
		
		return new MethodTransformer[] {/*hurtCameraEffectTransformer,*/ transformSetupCameraTransform, transformUpdateCameraAndRender, transformRenderWorld, transformRenderWorldPass, updateFogColorTransformer, drawNameplateTransformer};
	}

}
