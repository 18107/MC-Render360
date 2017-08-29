package core.render360.coretransform.classtransformers;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import core.render360.coretransform.CLTLog;
import core.render360.coretransform.RenderUtil;
import core.render360.coretransform.classtransformers.name.ClassName;
import core.render360.coretransform.classtransformers.name.MethodName;
import core.render360.coretransform.classtransformers.name.Names;
import mod.render360.render.RenderMethod;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;

public class GuiScreenTransformer extends ClassTransformer {

	@Override
	public ClassName getClassName() {return Names.GuiScreen;}

	@Override
	public MethodTransformer[] getMethodTransformers() {
		
		/**
		 * Removes the gray background when Resize Gui is on
		 */
		MethodTransformer transformDrawWorldBackground = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.GuiScreen_drawWorldBackground;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					if (instruction.getOpcode() == ICONST_0) {
						CLTLog.info("Found ICONST_0 in method " + getMethodName().debug());
						
						instruction = instruction.getPrevious();
						
						InsnList toInsert = new InsnList();
						LabelNode label = new LabelNode();
						
						//if RenderUtil.renderMethod.getResizeGui() {
						//this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
						//}
						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class),
								"renderMethod", "L" + Type.getInternalName(RenderMethod.class) + ";")); //renderMethod
						toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(RenderMethod.class),
								"getResizeGui", "()Z", false)); //getResizeGui()
						toInsert.add(new JumpInsnNode(IFNE, label));
						
						method.instructions.insertBefore(instruction, toInsert);
						
						for (int i = 0; i < 10; i++) {
							instruction = instruction.getNext();
						}
						
						method.instructions.insertBefore(instruction, label);
						
						break;
					}
				}
				
			}
		};
		
		/**
		 * Changes the options background and some of the loading screens
		 */
		MethodTransformer transformDrawBackground = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.GuiScreen_drawBackground;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				
				InsnList toInsert = new InsnList();
				LabelNode label1 = new LabelNode();
				LabelNode label2 = new LabelNode();
				
				CLTLog.info("Beginning at start of method " + getMethodName().debug());
				
				//if (!RenderUtil.renderMethod.replaceLoadingScreen())
				toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class),
						"renderMethod", "L" + Type.getInternalName(RenderMethod.class) + ";")); //renderMethod
				toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(RenderMethod.class),
						"replaceLoadingScreen", "()Z", false)); //replaceLoadingScreen()
				toInsert.add(new JumpInsnNode(IFNE, label1));
				
				method.instructions.insert(toInsert);
				
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					if (instruction.getOpcode() == RETURN) {
						CLTLog.info("Found RETURN in method " + getMethodName().debug());
						
						//else {
						//RenderUtil.renderMethod.renderLoadingScreen(this)
						//}
						toInsert.add(new JumpInsnNode(GOTO, label2));
						toInsert.add(label1);
						
						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(RenderUtil.class),
								"renderMethod", "L" + Type.getInternalName(RenderMethod.class) + ";")); //renderMwthod
						toInsert.add(new VarInsnNode(ALOAD, 0)); //this
						toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(RenderMethod.class),
								"renderLoadingScreen", "(L" + classNode.name + ";)V", false)); //renderLoadingScreen()
						
						toInsert.add(label2);
						
						method.instructions.insertBefore(instruction, toInsert);
						
						break;
					}
				}
			}
		};
		
		return new MethodTransformer[] {transformDrawWorldBackground, transformDrawBackground};
	}

}
