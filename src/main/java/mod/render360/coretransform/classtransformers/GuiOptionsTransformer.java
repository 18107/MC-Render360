package mod.render360.coretransform.classtransformers;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import mod.render360.coretransform.CLTLog;
import mod.render360.coretransform.classtransformers.name.ClassName;
import mod.render360.coretransform.classtransformers.name.MethodName;
import mod.render360.coretransform.classtransformers.name.Names;
import mod.render360.coretransform.gui.Render360Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;

public class GuiOptionsTransformer extends ClassTransformer {

	@Override
	public ClassName getClassName() {return Names.GuiOptions;}

	@Override
	public MethodTransformer[] getMethodTransformers() {
		
		MethodTransformer transformInitGui = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.GuiOptions_initGui;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				
				AbstractInsnNode instruction = method.instructions.toArray()[2];
				CLTLog.info("Starting at begining of method " + getMethodName().debug());

				InsnList toInsert = new InsnList();
				LabelNode label = new LabelNode();

				//this.buttonList.add(new GuiButton(18107, this.width / 2 - 155, this.height / 6 + 12, 150, 20, "Render 360 Settings"));
				toInsert.add(new VarInsnNode(ALOAD, 0)); //this
				toInsert.add(new FieldInsnNode(GETFIELD, classNode.name, Names.GuiScreen_buttonList.getFullName(), Names.GuiScreen_buttonList.getDesc())); //buttonList
				toInsert.add(new TypeInsnNode(NEW, Type.getInternalName(GuiButton.class)));
				toInsert.add(new InsnNode(DUP)); //not sure why this is here, but it works
				
				toInsert.add(new IntInsnNode(SIPUSH, 18107)); //18107 (buttonID)
				
				toInsert.add(new VarInsnNode(ALOAD, 0)); //this
				toInsert.add(new FieldInsnNode(GETFIELD, classNode.name, Names.GuiScreen_width.getFullName(), Names.GuiScreen_width.getDesc())); //width
				toInsert.add(new InsnNode(ICONST_2));
				toInsert.add(new InsnNode(IDIV));
				toInsert.add(new IntInsnNode(SIPUSH, 155));
				toInsert.add(new InsnNode(ISUB));
				
				toInsert.add(new VarInsnNode(ALOAD, 0)); //this
				toInsert.add(new FieldInsnNode(GETFIELD, classNode.name, Names.GuiScreen_height.getFullName(), Names.GuiScreen_height.getDesc())); //height
				toInsert.add(new IntInsnNode(BIPUSH, 6));
				toInsert.add(new InsnNode(IDIV));
				toInsert.add(new IntInsnNode(BIPUSH, 12));
				toInsert.add(new InsnNode(IADD));
				
				toInsert.add(new IntInsnNode(SIPUSH, 150));
				toInsert.add(new IntInsnNode(BIPUSH, 20));
				toInsert.add(new LdcInsnNode(Render360Settings.screenTitle)); //"Render 360 Settings"
				toInsert.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(GuiButton.class), "<init>", "(IIIIILjava/lang/String;)V", false)); //GuiButton
				toInsert.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(java.util.List.class), "add",  "(Ljava/lang/Object;)Z", true)); //buttonList.add
				toInsert.add(new InsnNode(POP));
				
				method.instructions.insert(instruction, toInsert);
			}
		};
		
		MethodTransformer transformActionPerformed = new MethodTransformer() {
			@Override
			public MethodName getMethodName() {
				return Names.GuiOptions_actionPerformed;
			}
			
			@Override
			public void transform(ClassNode classNode, MethodNode method, boolean obfuscated) {
				CLTLog.info("Found method: " + getMethodName().all());
				CLTLog.info("Starting at begining of method " + getMethodName().debug());
				
				AbstractInsnNode instruction = method.instructions.getFirst();
				InsnList toInsert = new InsnList();
				LabelNode label = new LabelNode();
				
				//if (button.id == 18107)
				toInsert.add(new VarInsnNode(ALOAD, 1)); //button
				toInsert.add(new FieldInsnNode(GETFIELD, Names.GuiButton.getInternalName(),
						Names.GuiButton_id.getFullName(), Names.GuiButton_id.getDesc())); //id
				toInsert.add(new IntInsnNode(SIPUSH, 18107));
				toInsert.add(new JumpInsnNode(IF_ICMPNE, label));
				
				//this.mc.gameSettings.saveOptions();
				toInsert.add(new VarInsnNode(ALOAD, 0));
				toInsert.add(new FieldInsnNode(GETFIELD, classNode.name, Names.GuiScreen_mc.getFullName(), Names.GuiScreen_mc.getDesc())); //mc
				toInsert.add(new FieldInsnNode(GETFIELD, Type.getInternalName(Minecraft.class), Names.Minecraft_gameSettings.getFullName(), Names.Minecraft_gameSettings.getDesc())); //gameSettings
				toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(GameSettings.class), Names.GameSettings_saveOptions.getFullName(), Names.GameSettings_saveOptions.getDesc(), false));
				
				//this.mc.displayGuiScreen(new Render360Settings(this));
				toInsert.add(new VarInsnNode(ALOAD, 0)); //this
				toInsert.add(new FieldInsnNode(GETFIELD, classNode.name, Names.GuiScreen_mc.getFullName(), Names.GuiScreen_mc.getDesc())); //mc
				toInsert.add(new TypeInsnNode(NEW, Type.getInternalName(Render360Settings.class))); //new Render360Settings
				toInsert.add(new InsnNode(DUP));
				toInsert.add(new VarInsnNode(ALOAD, 0)); //this
				toInsert.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(Render360Settings.class), "<init>", "(L" + Type.getInternalName(GuiScreen.class) + ";)V", false));
				toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Minecraft.class), Names.Minecraft_displayGuiScreen.getFullName(), Names.Minecraft_displayGuiScreen.getDesc(), false));
				
				toInsert.add(label);
				method.instructions.insertBefore(instruction, toInsert);
			}
		};
		
		return new MethodTransformer[] {transformInitGui, transformActionPerformed};
	}

}
