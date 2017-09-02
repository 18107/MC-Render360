package core.render360.coretransform.classtransformers;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
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

public class GuiScreenTransformer extends ClassTransformer {

	@Override
	public ClassName getClassName() {return Names.GuiScreen;}

	@Override
	public MethodTransformer[] getMethodTransformers() {
		
		MethodTransformer transformDrawWorldBackground = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.GuiScreen_drawWorldBackground;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					if (instruction.getOpcode() == IFNULL) {
						CLTLog.info("Found IFNULL in method " + getMethodName().debug());
						
						instruction = method.instructions.get(
								method.instructions.indexOf(instruction) + 3);
						
						InsnList toInsert = new InsnList();
						LabelNode label = new LabelNode();
						
						/**
						 * if (!TransformerUtil.onDrawWorldBackground) {
						 *   this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
						 * }
						 */
						
						toInsert.add(new VarInsnNode(ALOAD, 0)); //this
						toInsert.add(new MethodInsnNode(INVOKESTATIC,
								Type.getInternalName(TransformerUtil.class), "onDrawWorldBackground",
								"(L" + Names.GuiScreen.getInternalName(obfuscated) + ";)Z", false));
						
						toInsert.add(new JumpInsnNode(IFNE, label));
						
						method.instructions.insertBefore(instruction, toInsert);
						
						instruction = method.instructions.get(
								method.instructions.indexOf(instruction) + 9);
						
						method.instructions.insert(instruction, label);
						
						break;
					}
				}
			}
		};
		
		MethodTransformer transformDrawBackground = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.GuiScreen_drawBackground;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				
				InsnList toInsert = new InsnList();
				LabelNode label = new LabelNode();
				
				/**
				 * if (TransformerUtil.onDrawBackground(this)) {
				 *   return;
				 * }
				 */
				toInsert.add(new VarInsnNode(ALOAD, 0)); //this
				toInsert.add(new MethodInsnNode(INVOKESTATIC,
						Type.getInternalName(TransformerUtil.class), "onDrawBackground",
						"(L" + Names.GuiScreen.getInternalName(obfuscated) + ";)Z", false));
				toInsert.add(new JumpInsnNode(IFEQ, label));
				toInsert.add(new InsnNode(RETURN));
				toInsert.add(label);
				
				method.instructions.insert(toInsert);
			}
		};
		
		return new MethodTransformer[] {transformDrawWorldBackground, transformDrawBackground};
	}

}
