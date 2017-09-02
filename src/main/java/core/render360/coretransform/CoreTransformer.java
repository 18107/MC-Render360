package core.render360.coretransform;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import core.render360.coretransform.classtransformers.ClassTransformer;
import core.render360.coretransform.classtransformers.ClassTransformer.MethodTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class CoreTransformer implements IClassTransformer {
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		//get a list of all class transformers
		ClassTransformer[] classTransformers = ClassTransformer.getClassTransformers();
		
		//for each class transformer
		for (ClassTransformer classTransformer : classTransformers) {
			
			//if the class transformer should modify this class
			if (name.equals(classTransformer.getClassName().getName())) {
				
				CLTLog.info(String.format("Class: %s", classTransformer.getClassName().all()));
				boolean obfuscated = CoreLoader.isObfuscated;

				try {
					ClassNode classNode = new ClassNode();
					ClassReader classReader = new ClassReader(basicClass);
					classReader.accept(classNode, 0);

					//get a list of all method transformers for this class
					MethodTransformer[] mts = classTransformer.getMethodTransformers();

					//transform methods
					for (MethodNode method : classNode.methods) {
						for (MethodTransformer mt : mts) {
							if (method.name.equals(mt.getMethodName().getShortName()) && method.desc.equals(mt.getMethodName().getDesc())) {
								mt.transform(classNode, method, obfuscated);
							}
						}
					}

					ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
					classNode.accept(classWriter);
					return classWriter.toByteArray();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				//The class transformer has been matched to the class, no need to check other transformers
				break;
			}
		}
		return basicClass;
	}
}
