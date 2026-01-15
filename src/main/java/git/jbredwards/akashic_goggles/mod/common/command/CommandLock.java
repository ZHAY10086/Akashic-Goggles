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
 * Locks held Akashic Goggles, with the option to lock a number of slots or all of them.
 * @author jbred
 *
 */
class CommandLock extends CommandAkashicGoggles.SubCommand
{
    @Nonnull
    @Override
    public String getName() {
        return "lock";
    }

    @Nonnull
    @Override
    Object[] execute(@Nonnull final EntityLivingBase entity, @Nonnull final ICommandSender sender, @Nonnull final ItemStack goggles, @Nonnull final String[] args) throws CommandException {
        final int slots = args.length == 2 ? parseInt(args[1], 0) : 0;
        if(slots == 0) {
            if(!InventoryAkashicGoggles.isMutable(goggles)) throw new CommandException(getPrefix() + "noChange");
            ItemNBTHelper.setBoolean(goggles, InventoryAkashicGoggles.NBT_MUTABLE, false);
        }

        else {
            if(InventoryAkashicGoggles.isMutable(goggles) && InventoryAkashicGoggles.getLocked(goggles) == slots) throw new CommandException(getPrefix() + "noChange");
            // Lock # of slots, and make the goggles mutable if they aren't already.
            ItemNBTHelper.getNBT(goggles).removeTag(InventoryAkashicGoggles.NBT_MUTABLE);
            ItemNBTHelper.setInt(goggles, InventoryAkashicGoggles.NBT_INVENTORY_LOCKED, slots);
        }

        return new Object[0];
    }
}
