package net.lomeli.trophyslots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.Achievement;
import net.minecraft.util.IChatComponent;

import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import net.lomeli.trophyslots.core.*;
import net.lomeli.trophyslots.core.command.CommandTrophySlots;
import net.lomeli.trophyslots.core.network.MessageOpenWhitelist;
import net.lomeli.trophyslots.core.network.MessageSlotsClient;
import net.lomeli.trophyslots.core.network.MessageUpdateWhitelist;
import net.lomeli.trophyslots.core.version.VersionChecker;

@Mod(modid = TrophySlots.MOD_ID, name = TrophySlots.MOD_NAME, version = TrophySlots.VERSION, modLanguageAdapter = TrophySlots.KOTLIN_ADAPTER, guiFactory = TrophySlots.FACTORY)
public class TrophySlots {
    public static final String FACTORY = "net.lomeli.trophyslots.client.config.TrophySlotsFactory";
    public static final String MOD_ID = "trophyslots";
    public static final String MOD_NAME = "Trophy Slots";
    public static final String KOTLIN_ADAPTER = "net.lomeli.trophyslots.KotlinAdapter";
    public static final int MAJOR = 3, MINOR = 0, REV = 0;
    public static final String VERSION = MAJOR + "." + MINOR + "." + REV;
    public static final String updateUrl = "https://raw.githubusercontent.com/Lomeli12/TrophySlots/master/update.json";
    public static final String slotsUnlocked = MOD_ID + "_slotsUnlocked";

    @SidedProxy(clientSide = "net.lomeli.trophyslots.client.ClientProxy", serverSide = "net.lomeli.trophyslots.core.Proxy")
    public static Proxy proxy;

    public static int slotRenderType = 0;
    public static int loseSlotNum = 1;
    public static boolean unlockViaAchievements = true;
    public static boolean canUseTrophy = true;
    public static boolean canBuyTrophy = false;
    public static boolean disable3 = false;
    public static boolean checkForUpdates = true;
    public static boolean xmas = true;
    public static boolean useWhiteList = false;
    public static boolean loseSlots = false;

    public static SimpleNetworkWrapper packetHandler;
    public static Config modConfig;
    public static VersionChecker versionHandler;

    public static Achievement firstSlot;
    public static Achievement maxCapcity;
    public static AchievementPage achievementPage;
    public static boolean debug;
    public static Logger log;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log = new Logger();
        try {
            EntityPlayer.class.getMethod("addChatComponentMessage", IChatComponent.class);
            debug = true;
            log.logInfo("Dev environment, enabled logging!");
        } catch (Exception e) {
            debug = false;
            log.logError(e);
        }

        modConfig = new Config(event.getSuggestedConfigurationFile());
        modConfig.loadConfig();
        versionHandler = new VersionChecker(updateUrl, MOD_NAME, MAJOR, MINOR, REV);
        if (checkForUpdates)
            versionHandler.checkForUpdates();

        packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
        packetHandler.registerMessage(MessageSlotsClient.class, MessageSlotsClient.class, 0, Side.CLIENT);
        packetHandler.registerMessage(MessageOpenWhitelist.class, MessageOpenWhitelist.class, 1, Side.CLIENT);
        packetHandler.registerMessage(MessageUpdateWhitelist.class, MessageUpdateWhitelist.class, 2, Side.CLIENT);

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();

        firstSlot = new Achievement("achievement.trophyslots.firstSlot", "firstSlotAchievement", 0, 0, Blocks.chest, null).registerStat();
        maxCapcity = new  Achievement("achievement.trophyslots.maximumCapacity", "maximumCapacityAchievement", 2, 0, ModItems.trophy, firstSlot).registerStat();

        achievementPage = new AchievementPage(MOD_NAME, firstSlot, maxCapcity);
        AchievementPage.registerAchievementPage(achievementPage);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        proxy.reset();
    }

    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        proxy.resetConfig();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTrophySlots());
    }
}
