package git.jbredwards.akashic_goggles.core;

import com.google.common.collect.ImmutableMap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * @author jbred
 *
 */
public final class Transformer implements IClassTransformer
{
    @Nonnull
    public static final Map<String, Map<String, Map<String, BiConsumer<InsnList, AbstractInsnNode>>>> MAPPINGS = new HashMap<>();
    static {
        // Slot.getSlotIndex() is usually wrong, at least when using that value to find a slot.
        // new MessageDropIn(under.getSlotIndex(), held) -> new MessageDropIn(under.slotNumber, held)
        addMapping("vazkii/arl/util/DropInHandler onRightClick(Lnet/minecraftforge/client/event/GuiScreenEvent$MouseInputEvent$Pre;)V", builder -> {
            builder.put("net/minecraft/inventory/Slot getSlotIndex()I", (instructions, insn) -> {
                instructions.insert(insn, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/inventory/Slot", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "slotNumber" : "field_75222_d", "I"));
                instructions.remove(insn);
            });
        });
    }

    private static void addMapping(@Nonnull final String targetMethod, @Nonnull final Consumer<ImmutableMap.Builder<String, BiConsumer<InsnList, AbstractInsnNode>>> mapper) {
        @Nonnull final ImmutableMap.Builder<String, BiConsumer<InsnList, AbstractInsnNode>> builder = ImmutableMap.builder();
        mapper.accept(builder);

        @Nonnull final String[] mapping = targetMethod.split(" ");
        MAPPINGS.computeIfAbsent(mapping[0].replace('/', '.'), key -> new HashMap<>()).put(mapping[1], new HashMap<>(builder.build()));
    }

    @Nonnull
    private static MethodInsnNode hook(@Nonnull final String name, @Nonnull final String desc) {
        return new MethodInsnNode(Opcodes.INVOKESTATIC, "git/jbredwards/akashic_goggles/core/ASMHooks", name, desc, false);
    }

    @Nullable
    @Override
    public byte[] transform(@Nullable final String name, @Nullable final String transformedName, @Nullable final byte[] basicClass) {
        if(basicClass == null || transformedName == null) return basicClass;

        @Nullable final Map<String, Map<String, BiConsumer<InsnList, AbstractInsnNode>>> methodMappings = MAPPINGS.get(transformedName);
        if(methodMappings == null) return basicClass;

        @Nonnull final ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        for(@Nonnull final MethodNode method : classNode.methods) {
            @Nullable final Map<String, BiConsumer<InsnList, AbstractInsnNode>> actionMappings = methodMappings.get(method.name + method.desc);
            if(actionMappings != null) for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                @Nullable final BiConsumer<InsnList, AbstractInsnNode> action;
                if(insn instanceof MethodInsnNode) action = actionMappings.get(((MethodInsnNode)insn).owner + ' ' + ((MethodInsnNode)insn).name + ((MethodInsnNode)insn).desc);
                else if(insn instanceof FieldInsnNode) action = actionMappings.get(((FieldInsnNode)insn).owner + ' ' + ((FieldInsnNode)insn).name);
                else action = null;

                if(action != null) action.accept(method.instructions, insn);
            }
        }

        @Nonnull final ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
