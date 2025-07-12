package git.jbredwards.akashic_goggles.core.transformer;

import org.apache.logging.log4j.util.TriConsumer;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

/**
 *
 * @author jbred
 *
 */
@FunctionalInterface
public interface ASMConsumer extends BiConsumer<InsnList, AbstractInsnNode>
{
    default void accept(@Nonnull final ClassNode classNode, @Nonnull final MethodNode method, @Nonnull final AbstractInsnNode insn) {
        accept(method.instructions, insn);
    }

    @Nonnull
    static ASMConsumer advanced(@Nonnull final TriConsumer<? super ClassNode, ? super MethodNode, ? super AbstractInsnNode> action) {
        return new ASMConsumer() {
            @Override
            public void accept(@Nonnull final ClassNode classNode, @Nonnull final MethodNode method, @Nonnull final AbstractInsnNode insn) {
                action.accept(classNode, method, insn);
            }

            @Override
            public void accept(@Nonnull final InsnList instructions, AbstractInsnNode insn) {
                throw new UnsupportedOperationException("Call ASMConsumer.accept(classNode, method, insn) instead.");
            }
        };
    }

    @Nonnull
    static ASMConsumer identity() { return (instructions, insn) -> {}; }
}
