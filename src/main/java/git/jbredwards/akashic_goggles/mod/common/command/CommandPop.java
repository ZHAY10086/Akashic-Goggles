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

import com.google.common.collect.Lists;
import git.jbredwards.akashic_goggles.mod.common.InventoryAkashicGoggles;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.arl.util.ItemNBTHelper;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Pops off the most recently added item in held Akashic Goggles.
 * @author jbred
 *
 */
class CommandPop extends CommandAkashicGoggles.SubCommand
{
    @Nonnull
    @Override
    public String getName() {
        return "pop";
    }

    @Nonnull
    @Override
    Object[] execute(@Nonnull final EntityLivingBase entity, @Nonnull final ICommandSender sender, @Nonnull final ItemStack goggles, @Nonnull final String[] args) throws CommandException {
        @Nonnull final List<NBTTagCompound> poppedStacks = InventoryAkashicGoggles.popItemOff(goggles, args.length < 3 || !args[2].equals("all"), false);
        if(poppedStacks.isEmpty()) throw new CommandException(getPrefix() + "none");
        else if(args.length >= 2 && args[1].equals("give")) Lists.reverse(poppedStacks).forEach(entity instanceof EntityPlayer
                ? nbt -> ItemHandlerHelper.giveItemToPlayer((EntityPlayer)entity, new ItemStack(nbt))
                : nbt -> entity.entityDropItem(new ItemStack(nbt), 0));

        if(args.length >= 3 && args[2].equals("all")) {
            if(ItemNBTHelper.getList(goggles, InventoryAkashicGoggles.NBT_INVENTORY, Constants.NBT.TAG_COMPOUND, false).isEmpty())
                return new Object[] {new TextComponentTranslation(getPrefix() + "all")};

            else if(poppedStacks.size() > 1)
                return new Object[] {new TextComponentTranslation(getPrefix() + "multiple")};
        }

        return new Object[] {new TextComponentTranslation(getPrefix() + "single")};
    }

    @Nonnull
    @Override
    List<String> getTabCompletions(@Nonnull final String[] args) {
        if(args.length == 3) return getListOfStringsMatchingLastWord(args, "all");
        return args.length == 2 ? getListOfStringsMatchingLastWord(args, "destroy", "give") : Collections.emptyList();
    }
}
