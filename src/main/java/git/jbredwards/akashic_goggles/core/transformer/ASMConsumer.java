package git.jbredwards.akashic_goggles.core.transformer;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.function.BiConsumer;

/**
 *
 * @author jbred
 *
 */
@FunctionalInterface
public interface ASMConsumer extends BiConsumer<InsnList, AbstractInsnNode> {}
