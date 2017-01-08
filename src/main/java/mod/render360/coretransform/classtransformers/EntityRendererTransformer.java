package mod.render360.coretransform.classtransformers;

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
import org.objectweb.asm.tree.VarInsnNode;

import mod.render360.coretransform.CLTLog;
import mod.render360.coretransform.CoreLoader;
import mod.render360.coretransform.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;

public class EntityRendererTransformer extends ClassTransformer {
	
	protected EntityRendererTransformer() {
		//Prevents this class from accidentally being created twice
	}
	
	@Override
	public String getObfuscatedClassName() {return "bqc";}
	
	@Override
	public String getClassName() {return "net.minecraft.client.renderer.EntityRenderer";}

	@Override
	public MethodTransformer[] getMethodTransformers() {
		MethodTransformer transformGetFOVModifier = new MethodTransformer() {
			public String getMethodName() {return CoreLoader.isObfuscated ? "a" : "getFOVModifier";}
			public String getDescName() {return "(FZ)F";}
			
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + method.name + " " + method.desc);
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					
					if (instruction.getOpcode() == ALOAD) {
						CLTLog.info("Found ALOAD in method " + getMethodName());
						
						InsnList toInsert = new InsnList();
						LabelNode label = new LabelNode();
						
						// change "if (this.debugView)" to "if (RenderUtil.render360 || this.debugView)"
						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class), "render360", "Z"));
						toInsert.add(new JumpInsnNode(IFNE, label));
						method.instructions.insertBefore(instruction, toInsert);
						
						//Find LDC 90.0
						for (int i = 0; i < 3; i++) {
							instruction = instruction.getNext();
						}
						method.instructions.insertBefore(instruction, label);
						
						break;
					}
				}
			}
		};
		
		/**
		 * Fixes screen tearing caused by the camera being forward of center.
		 * This is not being used. FIXME when MCP updates
		 */
		MethodTransformer transformOrientCamera = new MethodTransformer() {
			public String getMethodName() {return CoreLoader.isObfuscated ? "a" : "orientCamera";}
			public String getDescName() {return "(F)V";}
			
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + method.name + " " + method.desc);
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					
					if (instruction.getOpcode() == INSTANCEOF) {
						CLTLog.info("Found INSTANCEOF in method " + getMethodName());
						
						instruction = instruction.getPrevious();
						
						//RenderUtil.rotateCamera()
						method.instructions.insertBefore(instruction, new MethodInsnNode(INVOKESTATIC,
								Type.getInternalName(RenderUtil.class), "rotateCamera", "()V", false));
						
						break;
					}
				}
			}
		};
		
		MethodTransformer transformSetupCameraTransform = new MethodTransformer() {
			public String getMethodName() {return CoreLoader.isObfuscated ? "a" : "setupCameraTransform";}
			public String getDescName() {return "(FI)V";}
			
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + method.name + " " + method.desc);
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					if (instruction.getOpcode() == TABLESWITCH) {
						CLTLog.info("Found tableswitch in method " + getMethodName());
						
						//Go back to orientCamera
						for (int i = 0; i < 8+4; i++) {
							instruction = instruction.getPrevious();
						}
						InsnList toInsert = new InsnList();
						
						//GlStateManager.rotate(RenderUtil.rotation, 0, 0, 1);
						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class), "rotation", "F")); //rotation
						toInsert.add(new InsnNode(FCONST_0)); //0
						toInsert.add(new InsnNode(FCONST_0)); //0
						toInsert.add(new InsnNode(FCONST_1)); //1
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(GlStateManager.class),
								obfuscated ? "func_179114_b" : "rotate", "(FFFF)V", false));
						
						//Fixes screen tearing. FIXME move when MCP updates
						//GlStateManager.translate(0, 0, -0.05F);
						toInsert.add(new InsnNode(FCONST_0)); //0
						toInsert.add(new InsnNode(FCONST_0)); //0
						toInsert.add(new LdcInsnNode(-0.05f)); //-0.05
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(GlStateManager.class),
								obfuscated ? "func_179109_b" : "translate", "(FFF)V", false));
						
						method.instructions.insertBefore(instruction, toInsert);
						
						break;
					}
				}
			}
		};
		
		MethodTransformer transformUpdateCameraAndRender = new MethodTransformer() {
			
			@Override
			public String getMethodName() {return CoreLoader.isObfuscated ? "a" : "updateCameraAndRender";}
			
			@Override
			public String getDescName() {return "(FJ)V";}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + method.name + " " + method.desc);
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					
					if (instruction.getOpcode() == SIPUSH &&
							instruction.getNext().getOpcode() == LDC) {
						CLTLog.info("Found SIPUSH in method " + getMethodName());
						
						//go to start of method call
						for (int i = 0; i < 16; i++) {
							instruction = instruction.getNext();
						}
						
						InsnList toInsert = new InsnList();
						
						//RenderUtil.renderGuiStart()
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(RenderUtil.class), "renderGuiStart", "()V", false));
						method.instructions.insertBefore(instruction, toInsert);
						
						//go to after method call
						for (int i = 0; i < 5; i++) {
							instruction = instruction.getNext();
						}
						
						//RenderUtil.renderGuiEnd()
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(RenderUtil.class), "renderGuiEnd", "()V", false));
						method.instructions.insertBefore(instruction, toInsert);
						
						break;
					}
				}
				
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					
					if (instruction.getOpcode() == ILOAD &&
							instruction.getPrevious().getOpcode() == GETFIELD &&
							instruction.getNext().getOpcode() == ILOAD) {
						CLTLog.info("Found ILOAD in method " + getMethodName());
						
						InsnList toInsert = new InsnList();
						
						//go to start of method call
						for (int i = 0; i < 3; i++) {
							instruction = instruction.getNext();
						}
						
						if (instruction.getOpcode() != INVOKESTATIC) {
							//assume optifine is installed
							
							AbstractInsnNode hold = instruction;
							for (int i = 0; i < 40; i++) {
								instruction = instruction.getPrevious();
							}
							
							//RenderUtil.renderGuiStart2()
							toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(RenderUtil.class), "renderGuiStart2", "()V", false));
							method.instructions.insertBefore(instruction, toInsert);
							
							instruction = hold;
							for (int i = 0; i < 4; i++) {
								instruction = instruction.getNext();
							}
							
							//RenderUtil.renderGuiEnd2()
							toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(RenderUtil.class), "renderGuiEnd2", "()V", false));
							method.instructions.insertBefore(instruction, toInsert);
							
							break;
						}
						//assume no other coremods installed
						
						for (int i = 0; i < 6; i++) {
							instruction = instruction.getPrevious();
						}
						
						//RenderUtil.renderGuiStart2()
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(RenderUtil.class), "renderGuiStart2", "()V", false));
						method.instructions.insertBefore(instruction, toInsert);
						
						//go to after method call
						for (int i = 0; i < 7; i++) {
							instruction = instruction.getNext();
						}
						
						//RenderUtil.renderGuiEnd2()
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(RenderUtil.class), "renderGuiEnd2", "()V", false));
						method.instructions.insertBefore(instruction, toInsert);
						
						break;
					}
				}
			}
		};
		
		MethodTransformer transformRenderWorld = new MethodTransformer() {
			public String getMethodName() {return CoreLoader.isObfuscated ? "b" : "renderWorld";}
			public String getDescName() {return "(FJ)V";}
			
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + method.name + " " + method.desc);
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					
					if (instruction.getOpcode() == ALOAD &&
							instruction.getNext().getOpcode() == ICONST_2) {
						CLTLog.info("Found ALOAD in method " + getMethodName());
						
						InsnList toInsert = new InsnList();
						
						// void RenderUtil.setupRenderWorld(EntityRenderer, mc, p_78471_1_, p_78471_2_);
						toInsert.add(new VarInsnNode(ALOAD, 0)); //this
						toInsert.add(new VarInsnNode(ALOAD, 0)); //this
						toInsert.add(new FieldInsnNode(GETFIELD, classNode.name, obfuscated ? "field_78531_r" : "mc", "L" + Type.getInternalName(Minecraft.class) + ";")); //mc
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
			public String getMethodName() {return CoreLoader.isObfuscated ? "a" : "renderWorldPass";}
			public String getDescName() {return "(IFJ)V";}
			
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + method.name + " " + method.desc);
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					
					if (instruction.getOpcode() == LDC) {
						CLTLog.info("found LDC in method " + getMethodName());
						
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
						//to GlStateManager.viewport(RenderUtil.partialXPos, RenderUtil.partialYPos, RenderUtil.partialWidth, RenderUtil.partialHeight);
						for (int i = 0; i < 8; i++) {
							method.instructions.remove(instruction.getNext()); //remove 0, 0, this.mc.displayWidth, this.mc.displayHeight
						}
						//toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class), "partialScreenXPos", "I"));
						//toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class), "partialScreenYPos", "I"));
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
				CLTLog.info("reached end of method " + getMethodName());
				
				
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
		
		MethodTransformer drawNameplateTransformer = new MethodTransformer() {
			public String getMethodName() {return CoreLoader.isObfuscated ? "a" : "drawNameplate";}
			public String getDescName() {
				String descName;
				if (CoreLoader.isObfuscated) {
					descName = "(Lbfe;L" + Type.getInternalName(String.class) + ";FFFIFFZZ)V";
				} else {
					descName = "(L" + Type.getInternalName(FontRenderer.class) + ";L" +
							Type.getInternalName(String.class) + ";FFFIFFZZ)V";
				}
				return descName;
			}
			
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + method.name + " " + method.desc);
				CLTLog.info("begining at start of method " + getMethodName());
				
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
		
		return new MethodTransformer[] {transformGetFOVModifier, transformOrientCamera, transformSetupCameraTransform, transformUpdateCameraAndRender, transformRenderWorld, transformRenderWorldPass, drawNameplateTransformer};
	}

}
