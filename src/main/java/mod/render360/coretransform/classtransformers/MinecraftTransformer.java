package mod.render360.coretransform.classtransformers;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import mod.render360.coretransform.CLTLog;
import mod.render360.coretransform.RenderUtil;
import mod.render360.coretransform.classtransformers.name.ClassName;
import mod.render360.coretransform.classtransformers.name.MethodName;
import mod.render360.coretransform.classtransformers.name.Names;
import net.minecraft.client.multiplayer.WorldClient;

import static org.objectweb.asm.Opcodes.*;

public class MinecraftTransformer extends ClassTransformer {
	
	@Override
	public ClassName getClassName() {return Names.Minecraft;}

	@Override
	public MethodTransformer[] getMethodTransformers() {
		MethodTransformer loadWorldTransformer = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.Minecraft_loadWorld;
			}
			
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				CLTLog.info("Begining at start of method " + getMethodName().debug());
				
				InsnList toInsert = new InsnList();
				toInsert.add(new VarInsnNode(ALOAD, 1)); //worldClientIn
				toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(RenderUtil.class),
						"onWorldLoad", "(L" + Type.getInternalName(WorldClient.class) + ";)V", false));
				method.instructions.insertBefore(method.instructions.getFirst(), toInsert);
			}
		};
		
		return new MethodTransformer[] {loadWorldTransformer};
	}

}
