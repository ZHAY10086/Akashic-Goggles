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
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import vazkii.arl.util.ItemNBTHelper;

import javax.annotation.Nonnull;

/**
 * Unlocks held Akashic Goggles.
 * @author jbred
 *
 */
class CommandUnlock extends CommandAkashicGoggles.SubCommand
{
    @Nonnull
    @Override
    public String getName() {
        return "unlock";
    }

    @Nonnull
    @Override
    Object[] execute(@Nonnull final EntityLivingBase entity, @Nonnull final ICommandSender sender, @Nonnull final ItemStack goggles, @Nonnull final String[] args) throws CommandException {
        if(InventoryAkashicGoggles.isMutable(goggles) && InventoryAkashicGoggles.getLocked(goggles) == 0) throw new CommandException(getPrefix() + "noChange");
        ItemNBTHelper.getNBT(goggles).removeTag(InventoryAkashicGoggles.NBT_INVENTORY_LOCKED);
        ItemNBTHelper.getNBT(goggles).removeTag(InventoryAkashicGoggles.NBT_MUTABLE);
        return new Object[0];
    }
}
