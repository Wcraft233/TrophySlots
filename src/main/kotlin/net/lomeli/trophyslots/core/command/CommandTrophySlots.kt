package net.lomeli.trophyslots.core.command

import com.google.common.collect.Lists
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import java.util.*

class CommandTrophySlots : CommandBase {
    private val modCommands: ArrayList<CommandBase>
    private val commands: ArrayList<String>

    constructor() {
        modCommands = Lists.newArrayList()
        commands = Lists.newArrayList()

        modCommands.add(CommandUnlockAll())
        modCommands.add(CommandGetSlots())
        modCommands.add(CommandRemoveAll())
        modCommands.add(CommandRemoveSlots())
        modCommands.add(CommandSetSlots())
        modCommands.add(CommandUnlockSlots())

        var i = 0;
        while (i < modCommands.size) {
            commands.add(modCommands[i].commandName)
            ++i
        }
    }

    override fun getCommandName(): String? = "tslots"

    override fun getRequiredPermissionLevel(): Int = 0

    override fun getCommandUsage(sender: ICommandSender?): String? = "command.trophyslots.usage";

    override fun execute(server: MinecraftServer?, sender: ICommandSender?, args: Array<out String>?) {
        if (sender != null) {
            if (args != null && args.size >= 1) {
                for (i in modCommands.indices) {
                    val commandBase: CommandBase = modCommands[i];
                    if (commandBase.commandName.equals(args[0], true) && commandBase.checkPermission(server, sender))
                        commandBase.execute(server, sender, args)
                }
            } else
                sender.addChatMessage(TextComponentTranslation("command.trophyslots.usage"))
        }
    }

    override fun getTabCompletionOptions(server: MinecraftServer?, sender: ICommandSender?, args: Array<out String>?, pos: BlockPos?): MutableList<String>? {
        if (sender != null && args != null) {
            if (args.size == 1)
                return CommandBase.getListOfStringsMatchingLastWord(args, commands);
            else if (args.size >= 2) {
                for (i in modCommands.indices) {
                    val command: CommandBase = modCommands[i];
                    if (command.commandName.equals(args[0], true))
                        return command.getTabCompletionOptions(server, sender, args, pos)
                }
            }
        }
        return null;
    }
}