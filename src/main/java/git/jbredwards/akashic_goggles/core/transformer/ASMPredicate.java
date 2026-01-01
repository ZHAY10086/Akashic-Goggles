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
    default ASMPredicate and(@Nonnull final BiPredicate<? super MethodNode, ? super AbstractInsnNode> other) {
        return (t, u) -> test(t, u) && other.test(t, u);
    }

    @Nonnull
    default ASMPredicate andPrev(@Nonnull final BiPredicate<? super MethodNode, ? super AbstractInsnNode> other) {
        return (t, u) -> u.getPrevious() != null && test(t, u) && other.test(t, u.getPrevious());
    }
}
