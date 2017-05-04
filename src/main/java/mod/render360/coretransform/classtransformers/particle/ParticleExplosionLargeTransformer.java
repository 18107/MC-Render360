package mod.render360.coretransform.classtransformers.particle;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import mod.render360.coretransform.CLTLog;
import mod.render360.coretransform.classtransformers.name.ClassName;
import mod.render360.coretransform.classtransformers.name.MethodName;
import mod.render360.coretransform.classtransformers.name.Names;

import static org.objectweb.asm.Opcodes.*;

public class ParticleExplosionLargeTransformer extends ParticleTransformer {

	@Override
	public ClassName getClassName() {return Names.ParticleExplosionLarge;}

	@Override
	public MethodTransformer[] getMethodTransformers() {
		
		MethodTransformer transformRenderParticle = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.Particle_renderParticle;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + method.name + " " + method.desc);
				
				for (AbstractInsnNode instruction : method.instructions.toArray()) {
					if (instruction.getOpcode() == FCONST_1) {
						CLTLog.info("Found FCONST_1 in method " + getMethodName().getShortName());
						
						transformParticle(classNode, method, instruction, 15);
						
						break;
					}
				}
			}
		};
		
		return new MethodTransformer[] {transformRenderParticle};
	}

}
