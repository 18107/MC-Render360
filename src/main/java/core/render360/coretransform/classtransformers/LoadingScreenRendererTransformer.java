package core.render360.coretransform.classtransformers;

import org.objectweb.asm.Type;
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
import core.render360.coretransform.TransformerUtil;
import core.render360.coretransform.classtransformers.name.ClassName;
import core.render360.coretransform.classtransformers.name.MethodName;
import core.render360.coretransform.classtransformers.name.Names;

import static org.objectweb.asm.Opcodes.*;

public class LoadingScreenRendererTransformer extends ClassTransformer {

	@Override
	public ClassName getClassName() {return Names.LoadingScreenRenderer;}

	@Override
	public MethodTransformer[] getMethodTransformers() {
		
		MethodTransformer transformSetLoadingProgress = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.LoadingScreenRenderer_setLoadingProgress;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					if (instruction.getOpcode() == FSTORE) {
						CLTLog.info("Found FSTORE in method " + getMethodName().debug());
						
						for (int i = 0; i < 3; i++) {
							instruction = instruction.getNext();
						}
						
						/**
						 * if (!TransformerUtil.setLoadingProgressBackground(framebuffer)) {
						 *   vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
						 *   ...
						 *   tessellator.draw();
						 * }
						 */
						
						InsnList toInsert = new InsnList();
						LabelNode label = new LabelNode();
						
						toInsert.add(new VarInsnNode(ALOAD, 0)); //this
						toInsert.add(new FieldInsnNode(GETFIELD, classNode.name,
								Names.LoadingScreenRenderer_framebuffer.getFullName(obfuscated),
								Names.LoadingScreenRenderer_framebuffer.getDesc(obfuscated))); //framebuffer
						toInsert.add(new MethodInsnNode(INVOKESTATIC,
								Type.getInternalName(TransformerUtil.class),
								"setLoadingProgressBackground",
								"(L" + Names.Framebuffer.getInternalName(obfuscated) + ";)Z", false));
						
						toInsert.add(new JumpInsnNode(IFNE, label));
						
						method.instructions.insertBefore(instruction, toInsert);
						
						//go to tessellator.draw();
						instruction = method.instructions.get(
								method.instructions.indexOf(instruction)+91);
						
						method.instructions.insert(instruction, label);
						
						break;
					}
				}
			}
		};
		
		return new MethodTransformer[] {transformSetLoadingProgress};
	}

}
