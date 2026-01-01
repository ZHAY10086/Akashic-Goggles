/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

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
    @Override
    void accept(@Nonnull final InsnList instructions, @Nonnull final AbstractInsnNode insn);
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
            public void accept(@Nonnull final InsnList instructions, @Nonnull final AbstractInsnNode insn) {
                throw new UnsupportedOperationException("Call ASMConsumer.accept(classNode, method, insn) instead.");
            }
        };
    }

    @Nonnull
    static ASMConsumer identity() { return (instructions, insn) -> {}; }
}
