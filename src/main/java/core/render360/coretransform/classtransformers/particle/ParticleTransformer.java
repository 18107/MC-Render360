package core.render360.coretransform.classtransformers.particle;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import core.render360.coretransform.CLTLog;
import core.render360.coretransform.TransformerUtil;
import core.render360.coretransform.classtransformers.ClassTransformer;
import core.render360.coretransform.classtransformers.name.ClassName;
import core.render360.coretransform.classtransformers.name.MethodName;
import core.render360.coretransform.classtransformers.name.Names;

import static org.objectweb.asm.Opcodes.*;

public class ParticleTransformer extends ClassTransformer {
	
	private static ClassTransformer[] transformers;
	
	static {
		transformers = new ClassTransformer[] {new ParticleDiggingTransformer(), new ParticleBreakingTransformer(), new BarrierTransformer(), new ParticleExplosionLargeTransformer(), new ParticleSweepAttackTransformer()};
	}

	@Override
	public ClassName getClassName() {return Names.Particle;}

	@Override
	public MethodTransformer[] getMethodTransformers() {
		
		MethodTransformer transformRenderParticle = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.Particle_renderParticle;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					if (instruction.getOpcode() == ANEWARRAY) {
						CLTLog.info("Found ANEWARRAY in method " + getMethodName().debug());
						
						instruction = instruction.getPrevious();
						
						transformParticle(classNode, method, instruction, 14);
						
						break;
					}
				}
			}
		};
		
		return new MethodTransformer[] {transformRenderParticle};
	}
	
	protected void transformParticle(ClassNode classNode, MethodNode method, AbstractInsnNode instruction, int firstInsn) {
		InsnList toInsert = new InsnList();
		
		toInsert.add(new VarInsnNode(FLOAD, 4));
		toInsert.add(new VarInsnNode(FLOAD, 5));
		toInsert.add(new VarInsnNode(FLOAD, 6));
		toInsert.add(new VarInsnNode(FLOAD, 7));
		toInsert.add(new VarInsnNode(FLOAD, 8));
		toInsert.add(new VarInsnNode(FLOAD, firstInsn));
		toInsert.add(new VarInsnNode(FLOAD, firstInsn+1));
		toInsert.add(new VarInsnNode(FLOAD, firstInsn+2));
		toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(TransformerUtil.class),
				"transformParticle", "(FFFFFFFF)V", false));
		
		toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(TransformerUtil.class),
				"rotationX", "F"));
		toInsert.add(new VarInsnNode(FSTORE, 4));
		toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(TransformerUtil.class),
				"rotationZ", "F"));
		toInsert.add(new VarInsnNode(FSTORE, 5));
		toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(TransformerUtil.class),
				"rotationYZ", "F"));
		toInsert.add(new VarInsnNode(FSTORE, 6));
		toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(TransformerUtil.class),
				"rotationXY", "F"));
		toInsert.add(new VarInsnNode(FSTORE, 7));
		toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(TransformerUtil.class),
				"rotationXZ", "F"));
		toInsert.add(new VarInsnNode(FSTORE, 8));
		toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(TransformerUtil.class),
				"posX", "F"));
		toInsert.add(new VarInsnNode(FSTORE, firstInsn));
		toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(TransformerUtil.class),
				"posY", "F"));
		toInsert.add(new VarInsnNode(FSTORE, firstInsn+1));
		toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(TransformerUtil.class),
				"posZ", "F"));
		toInsert.add(new VarInsnNode(FSTORE, firstInsn+2));
		
		method.instructions.insertBefore(instruction, toInsert);
	}
	
	public static ClassTransformer[] getParticleTransformers() {
		return transformers;
	}
}
