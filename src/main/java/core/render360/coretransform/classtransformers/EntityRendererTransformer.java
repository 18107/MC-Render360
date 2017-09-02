package core.render360.coretransform.classtransformers;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import core.render360.coretransform.CLTLog;
import core.render360.coretransform.TransformerUtil;
import core.render360.coretransform.classtransformers.name.ClassName;
import core.render360.coretransform.classtransformers.name.MethodName;
import core.render360.coretransform.classtransformers.name.Names;

import static org.objectweb.asm.Opcodes.*;

public class EntityRendererTransformer extends ClassTransformer {
	
	@Override
	public ClassName getClassName() {return Names.EntityRenderer;}

	@Override
	public MethodTransformer[] getMethodTransformers() {
		//TODO
		/*MethodTransformer hurtCameraEffectTransformer = new MethodTransformer() {
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
		};*/
		
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
						LabelNode label = new LabelNode();
						
						/**
						 * if (TransformerUtil.renderWorld(partialTicks, finishTimeNano)) {
						 *   this.renderWorldPass(2, partialTicks, finishTimeNano);
						 * }
						 */
						toInsert.add(new VarInsnNode(FLOAD, 1)); //p_78471_1_
						toInsert.add(new VarInsnNode(LLOAD, 2)); //p_78471_2_
						toInsert.add(new MethodInsnNode(INVOKESTATIC,
								Type.getInternalName(TransformerUtil.class),
								"renderWorld", "(FJ)Z", false));
						toInsert.add(new JumpInsnNode(IFNE, label));
						
						//Insert method call
						method.instructions.insertBefore(instruction, toInsert);
						
						//this.renderWorldPass(2, partialTicks, finishTimeNano);
						for (int i = 0; i < 4; i++) {
							instruction = instruction.getNext();
						}
						method.instructions.insert(instruction, label);
						
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
						
						for (int i = 0; i < 3; i++) {
							instruction = instruction.getNext();
						}
						
						//if optifine is installed
						if (instruction.getNext().getOpcode() == ILOAD) {
							instruction = method.instructions.get(
									method.instructions.indexOf(instruction) + 17);
						}
						//assume no other coremods are installed
						
						/**
						 * Change from
						 *   GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
						 * to
						 *   TransformerUtil.setViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
						 */
						instruction = method.instructions.get(
								method.instructions.indexOf(instruction) + 8);
						method.instructions.remove(instruction.getNext()); //GlStateManager.viewport
						method.instructions.insert(instruction, new MethodInsnNode(INVOKESTATIC,
								Type.getInternalName(TransformerUtil.class),
								"setViewport", "(IIII)V", false));
						
						break;
					}
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
						instruction = instruction.getNext().getNext();
						
						InsnList toInsert = new InsnList();
						LabelNode label = new LabelNode();
						
						/**
						 * if (TransformerUtil.sunsetFog()) {
						 *   f5 = 0;
						 * }
						 */
						toInsert.add(new MethodInsnNode(INVOKESTATIC,
								Type.getInternalName(TransformerUtil.class),
								"sunsetFog", "()Z", false));
						
						toInsert.add(new JumpInsnNode(IFEQ, label));
						toInsert.add(new InsnNode(FCONST_0));
						toInsert.add(new VarInsnNode(FSTORE, 13));
						toInsert.add(label);
						
						method.instructions.insert(instruction, toInsert);
						
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
				
				InsnList toInsert = new InsnList();
				/**
				 * TransformerUtil.setNameplateOrientation(x, y, z, viewerYaw, viewerPitch);
				 */
				toInsert.add(new VarInsnNode(FLOAD, 2)); //x
				toInsert.add(new VarInsnNode(FLOAD, 3)); //y
				toInsert.add(new VarInsnNode(FLOAD, 4)); //z
				toInsert.add(new VarInsnNode(FLOAD, 6)); //viewerYaw
				toInsert.add(new VarInsnNode(FLOAD, 7)); //viewerPitch
				toInsert.add(new MethodInsnNode(INVOKESTATIC,
						Type.getInternalName(TransformerUtil.class),
						"setNameplateOrientation", "(FFFFF)V", false));
				
				toInsert.add(new FieldInsnNode(GETSTATIC,Type.getInternalName(TransformerUtil.class),
						"nameplateX", "F"));
				toInsert.add(new VarInsnNode(FSTORE, 2));
				toInsert.add(new FieldInsnNode(GETSTATIC,Type.getInternalName(TransformerUtil.class),
						"nameplateY", "F"));
				toInsert.add(new VarInsnNode(FSTORE, 3));
				toInsert.add(new FieldInsnNode(GETSTATIC,Type.getInternalName(TransformerUtil.class),
						"nameplateZ", "F"));
				toInsert.add(new VarInsnNode(FSTORE, 4));
				toInsert.add(new FieldInsnNode(GETSTATIC,Type.getInternalName(TransformerUtil.class),
						"nameplateYaw", "F"));
				toInsert.add(new VarInsnNode(FSTORE, 6));
				toInsert.add(new FieldInsnNode(GETSTATIC,Type.getInternalName(TransformerUtil.class),
						"nameplatePitch", "F"));
				toInsert.add(new VarInsnNode(FSTORE, 7));
				
				method.instructions.insert(toInsert);
			}
		};
		
		return new MethodTransformer[] {/*hurtCameraEffectTransformer,*/ transformSetupCameraTransform, transformUpdateCameraAndRender, transformRenderWorld, transformRenderWorldPass, updateFogColorTransformer, drawNameplateTransformer};
	}

}
