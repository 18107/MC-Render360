package core.render360.coretransform.classtransformers.particle;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import core.render360.coretransform.CLTLog;
import core.render360.coretransform.classtransformers.name.ClassName;
import core.render360.coretransform.classtransformers.name.MethodName;
import core.render360.coretransform.classtransformers.name.Names;

import static org.objectweb.asm.Opcodes.*;

public class ParticleSweepAttackTransformer extends ParticleTransformer {

	@Override
	public ClassName getClassName() {return Names.ParticleSweepAttack;}
	
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
					if (instruction.getOpcode() == BIPUSH &&
							instruction.getNext().getOpcode() == GETSTATIC) {
						CLTLog.info("Found BIPUSH in method " + getMethodName().debug());
						
						instruction = instruction.getPrevious();
						
						transformParticle(classNode, method, instruction, 15);
						
						break;
					}
				}
			}
		};
		
		return new MethodTransformer[] {transformRenderParticle};
	}
}
