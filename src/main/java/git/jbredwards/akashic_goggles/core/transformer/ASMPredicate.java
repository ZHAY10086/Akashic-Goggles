package git.jbredwards.akashic_goggles.core.transformer;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;

/**
 *
 * @author jbred
 *
 */
@FunctionalInterface
public interface ASMPredicate extends BiPredicate<MethodNode, AbstractInsnNode>
{
    @Nonnull
    default ASMPredicate andPrev(@Nonnull final BiPredicate<? super MethodNode, ? super AbstractInsnNode> other) {
        return (t, u) -> u.getPrevious() != null && test(t, u) && other.test(t, u.getPrevious());
    }
}
