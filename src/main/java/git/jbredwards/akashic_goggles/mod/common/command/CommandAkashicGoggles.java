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

import git.jbredwards.akashic_goggles.Tags;
import git.jbredwards.akashic_goggles.mod.AkashicGoggles;
import net.minecraft.command.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public final class CommandAkashicGoggles extends CommandTreeBase
{
    public CommandAkashicGoggles() {
        super.addSubcommand(new CommandLock());
        super.addSubcommand(new CommandNBT());
        super.addSubcommand(new CommandPop());
        super.addSubcommand(new CommandPush());
        super.addSubcommand(new CommandUnlock());
        super.addSubcommand(new CommandTreeHelp(this));
    }

    @Nonnull
    @Override
    public String getName() {
        return Tags.MOD_ID;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull final ICommandSender sender) {
        return "commands." + getName() + ".usage";
    }

    @Override
    public void addSubcommand(@Nonnull final ICommand command) {
        throw new UnsupportedOperationException(String.format("Don't add sub-commands to /%s, create your own command.", getName()));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Permissions are handled by sub-commands.
    }

    @Override
    public boolean checkPermission(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender) {
        return true; // Permissions are handled by sub-commands.
    }

    // Base class for Akashic Goggles subcommands.
    static abstract class SubCommand extends CommandBase
    {
        @Nonnull
        @Override
        public final String getUsage(@Nonnull final ICommandSender sender) {
            return getPrefix() + "usage";
        }

        @Override
        public final void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender, @Nonnull final String[] args) throws CommandException {
            if(affectsItems()) sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 0);

            @Nonnull final EntityLivingBase entity = args.length == 0 ? getCommandSenderAsPlayer(sender) : getEntity(server, sender, args[0], EntityLivingBase.class);
            @Nonnull final ItemStack goggles = entity.getHeldItemMainhand();

            if(goggles.getItem() != AkashicGoggles.GOGGLES) throw new CommandException("commands." + Tags.MOD_ID + ".invalidItem");
            else notifyCommandListener(sender, this, getPrefix() + "success", execute(entity, sender, goggles, args));

            if(affectsItems()) sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 1);
        }

        @Nonnull
        abstract Object[] execute(@Nonnull final EntityLivingBase entity, @Nonnull final ICommandSender sender, @Nonnull final ItemStack goggles, @Nonnull final String[] args) throws CommandException;
        boolean affectsItems() { return true; }

        @Nonnull
        @Override
        public final List<String> getTabCompletions(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender, @Nonnull final String[] args, @Nullable final BlockPos targetPos) {
            return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : getTabCompletions(args);
        }

        @Nonnull
        List<String> getTabCompletions(@Nonnull final String[] args) { return Collections.emptyList(); }

        @Nonnull
        final String getPrefix() { return "commands." + Tags.MOD_ID + '.' + getName() + '.'; }
    }
}
