package net.lomeli.trophyslots.core.command;


import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.lomeli.trophyslots.TrophySlots;
import net.lomeli.trophyslots.core.ServerConfig;
import net.lomeli.trophyslots.core.handlers.AdvancementHandler;
import net.lomeli.trophyslots.core.network.MessageServerConfig;
import net.lomeli.trophyslots.core.network.PacketHandler;
import net.lomeli.trophyslots.utils.InventoryUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.command.EnumArgument;

public class TSConfigCommand implements ISubCommand {
    private static final SimpleCommandExceptionType CONFIG_ERROR =
            new SimpleCommandExceptionType(new TranslationTextComponent("command.trophyslots.config.error"));

    private static final String[] CONFIG_OPTIONS = {"loseSlotOnDeathAmount", "startingSlots", "listmode", "advancementUnlock",
            "useTrophies", "buyTrophies", "reverseUnlockOrder", "loseSlotsOnDeath"};

    @Override
    public void registerSubCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder) {
        LiteralArgumentBuilder<CommandSource> base = Commands.literal(getName());

        for (int i = 0; i < CONFIG_OPTIONS.length; i++) {
            String config = CONFIG_OPTIONS[i];
            if (i < 2) {
                IntegerArgumentType intArg = IntegerArgumentType.integer(
                        config.equalsIgnoreCase("loseSlotOnDeathAmount") ? -1 : 9,
                        InventoryUtils.getMaxUnlockableSlots()
                );
                base.then(Commands.literal(config).requires((source -> source.hasPermissionLevel(2)))
                        .then(Commands.argument("amount", intArg)
                                .executes(context -> setConfigValue(context.getSource(), config,
                                        IntegerArgumentType.getInteger(context, "amount")))));
            } else if (i == 2) {
                base.then(Commands.literal(config).requires((source -> source.hasPermissionLevel(2)))
                        .then(Commands.argument("mode", EnumArgument.enumArgument(AdvancementHandler.ListMode.class))
                                .executes(context -> setConfigValue(context.getSource(), config,
                                        context.getArgument("mode", AdvancementHandler.ListMode.class)))));
            } else {
                base.then(Commands.literal(config).requires((source -> source.hasPermissionLevel(2)))
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(context -> setConfigValue(context.getSource(), config,
                                        BoolArgumentType.getBool(context, "value")))));
            }
        }
        argumentBuilder.then(base);
    }

    private int setConfigValue(CommandSource source, String config, Object value) throws CommandSyntaxException {
        boolean advancementUnlock = ServerConfig.unlockViaAdvancements;
        boolean useTrophies = ServerConfig.canUseTrophy;
        boolean buyTrophies = ServerConfig.canBuyTrophy;
        boolean reverseOrder = ServerConfig.reverseOrder;
        boolean loseSlots = ServerConfig.loseSlots;
        int losingSlots = ServerConfig.loseSlotNum;
        int startingSlots = ServerConfig.startingSlots;
        AdvancementHandler.ListMode mode = ServerConfig.listMode;

        switch (config.toLowerCase()) {
            case "advancementunlock":
                advancementUnlock = (boolean) value;
                break;
            case "usetrophies":
                useTrophies = (boolean) value;
                break;
            case "buytrophies":
                buyTrophies = (boolean) value;
                break;
            case "loseslotsondeath":
                loseSlots = (boolean) value;
                break;
            case "reverseunlockorder":
                reverseOrder = (boolean) value;
                break;
            case "startingslots":
                startingSlots = (int) value;
                break;
            case "loseslotondeathamount":
                losingSlots = (int) value;
                break;
            case "listmode":
                mode = (AdvancementHandler.ListMode) value;
            default:
                TrophySlots.log.error("How the hell did you get here?!!");
                throw CONFIG_ERROR.create();
        }

        PacketHandler.sendToServer(new MessageServerConfig(advancementUnlock, useTrophies, buyTrophies, reverseOrder,
                loseSlots, losingSlots, startingSlots, mode));
        source.sendFeedback(new TranslationTextComponent("command.trophyslots.config.success", config,
                value.toString()), false);
        return 0;
    }

    @Override
    public String getName() {
        return "server_config";
    }
}
