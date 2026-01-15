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
import git.jbredwards.akashic_goggles.api.IAkashicGoggles;
import git.jbredwards.akashic_goggles.mod.common.InventoryAkashicGoggles;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import vazkii.arl.interf.IDropInItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pushes an item into held Akashic Goggles.
 * @author jbred
 *
 */
class CommandPush extends CommandAkashicGoggles.SubCommand
{
    @Nullable
    static List akashicItemsCache;

    @Nonnull
    @Override
    public String getName() {
        return "push";
    }

    @Nonnull
    @Override
    Object[] execute(@Nonnull final EntityLivingBase entity, @Nonnull final ICommandSender sender, @Nonnull final ItemStack goggles, @Nonnull final String[] args) throws CommandException {
        @Nonnull final IDropInItem gogglesHandler = new InventoryAkashicGoggles(goggles);
        if(args.length == 3 && args[2].equals("all")) {
            final boolean keep = args[1].equals("keep");
            boolean foundItem = false;

            @Nonnull final IInventory inventory = getCommandSenderAsPlayer(entity).inventory;
            @Nonnull ItemStack filledGoggles = goggles;

            for(int i = 0; i < inventory.getSizeInventory(); i++) {
                @Nonnull final ItemStack stack = keep ? inventory.getStackInSlot(i).copy() : inventory.getStackInSlot(i);
                if(gogglesHandler.canDropItemIn(null, goggles, stack)) {
                    foundItem = true;
                    filledGoggles = gogglesHandler.dropItemIn(null, filledGoggles, stack);
                }
            }

            if(!foundItem) throw new CommandException(getPrefix() + "invalidItemInventory");
            entity.setHeldItem(EnumHand.MAIN_HAND, filledGoggles);
            return new Object[] {new TextComponentTranslation(getPrefix() + "all")};
        }

        else {
            @Nonnull final ItemStack stack;
            final boolean useOffhand = args.length < 2 || args[1].equals("consume") || args[1].equals("keep");
            if(useOffhand) stack = args.length == 2 && args[1].equals("keep") ? entity.getHeldItemOffhand().copy() : entity.getHeldItemOffhand();
            else { // Allow stack to be created via the command.
                stack = new ItemStack(getItemByText(sender, args[1]), 1, args.length >= 3 ? parseInt(args[2]) : 0);
                if(args.length >= 4) {
                    try { stack.setTagCompound(JsonToNBT.getTagFromJson(buildString(args, 3))); }
                    catch(@Nonnull final NBTException e) { throw new CommandException(getPrefix() + "tagError", e.getMessage()); }
                }
            }

            if(!gogglesHandler.canDropItemIn(null, goggles, stack)) throw new CommandException(getPrefix() + (useOffhand ? "invalidItem" : "invalidItemProvided"));
            entity.setHeldItem(EnumHand.MAIN_HAND, gogglesHandler.dropItemIn(null, goggles, stack));
            return new Object[] {new TextComponentTranslation(getPrefix() + "single")};
        }
    }

    @Nonnull
    @Override
    List<String> getTabCompletions(@Nonnull final String[] args) {
        if(akashicItemsCache == null) {
            akashicItemsCache = Lists.newArrayList("consume", "keep");
            akashicItemsCache.addAll(ForgeRegistries.ITEMS.getEntries().stream().filter(entry -> IAkashicGoggles.get(entry.getValue()) != null
                            && (!(entry.getValue() instanceof ItemArmor) || ((ItemArmor)entry.getValue()).armorType == EntityEquipmentSlot.HEAD))
                    .map(Map.Entry::getKey).collect(Collectors.toSet()));
        }

        if(args.length == 3 && (args[1].equals("consume") || args[1].equals("keep"))) return getListOfStringsMatchingLastWord(args, "all");
        return args.length == 2 ? getListOfStringsMatchingLastWord(args, akashicItemsCache) : Collections.emptyList();
    }
}
