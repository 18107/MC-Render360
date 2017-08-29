package core.render360.coretransform.classtransformers;

import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import core.render360.coretransform.classtransformers.name.ClassName;
import core.render360.coretransform.classtransformers.name.MethodName;
import core.render360.coretransform.classtransformers.particle.ParticleTransformer;

/**
 * Holds all of the class transformers.
 *
 */
public abstract class ClassTransformer {

	private static ClassTransformer[] transformers;
	
	static {
		//Put all of the class transformers here
		ClassTransformer[] classTransformers = new ClassTransformer[] {new MinecraftTransformer(), new GuiScreenTransformer(), new LoadingScreenRendererTransformer(), new EntityRendererTransformer(), new ParticleTransformer()};
		
		transformers = ArrayUtils.addAll(classTransformers, ParticleTransformer.getParticleTransformers());
	}
	
	//Template for a method transformer
	public static abstract class MethodTransformer {
		public abstract void transform(ClassNode classNode, MethodNode method, boolean obfuscated);
		public abstract MethodName getMethodName();
	}
	
	/**
	 * @return the name of the class
	 */
	public abstract ClassName getClassName();
	
	/**
	 * @return an array containing all method transformers for this class transformer
	 */
	public abstract MethodTransformer[] getMethodTransformers();
	
	/**
	 * @return an array containing all class transformers
	 */
	public static ClassTransformer[] getClassTransformers() {
		return transformers;
	}
}
