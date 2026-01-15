/*
 * Copyright (C) <2026 to Present> <jbredwards>
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

package git.jbredwards.akashic_goggles.mod.common.command;

import git.jbredwards.akashic_goggles.mod.common.InventoryAkashicGoggles;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import vazkii.arl.util.ItemNBTHelper;

import javax.annotation.Nonnull;

/**
 * Prints serialized Akashic Goggles data.
 * @author jbred
 *
 */
class CommandNBT extends CommandAkashicGoggles.SubCommand
{
    @Nonnull
    @Override
    public String getName() {
        return "nbt";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Nonnull
    @Override
    Object[] execute(@Nonnull EntityLivingBase entity, @Nonnull final ICommandSender sender, @Nonnull final ItemStack goggles, @Nonnull final String[] args) {
        @Nonnull final ItemStack cloned = goggles.copy();
        InventoryAkashicGoggles.setInvalid(cloned);
        return new Object[] {ClickToCopyHandler.apply(ItemNBTHelper.getNBT(cloned).toString())};
    }

    @Override
    boolean affectsItems() {
        return false;
    }
}
