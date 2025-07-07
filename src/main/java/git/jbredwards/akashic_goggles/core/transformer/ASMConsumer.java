package git.jbredwards.akashic_goggles.core.transformer;

import org.objectweb.asm.tree.AbstractInsnNode;
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
    default void acceptMethod(@Nonnull final MethodNode method, @Nonnull final AbstractInsnNode insn) {
        accept(method.instructions, insn);
    }

    @Nonnull
    static ASMConsumer method(@Nonnull final BiConsumer<? super MethodNode, ? super AbstractInsnNode> action) {
        return new ASMConsumer() {
            @Override
            public void acceptMethod(@Nonnull final MethodNode method, @Nonnull final AbstractInsnNode insn) {
                action.accept(method, insn);
            }

            @Override
            public void accept(@Nonnull final InsnList instructions, AbstractInsnNode insn) {}
        };
    }
}
