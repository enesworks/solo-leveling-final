package dev.eness.sololevelingfinal.core.network;

import java.util.ArrayList;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;

@EventBusSubscriber(bus = Bus.MOD)
public class SololevelingModVariables {
   public static final Capability<SololevelingModVariables.PlayerVariables> PLAYER_VARIABLES_CAPABILITY = CapabilityManager.get(
      new CapabilityToken<SololevelingModVariables.PlayerVariables>() {}
   );

   @SubscribeEvent
   public static void init(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         SololevelingModVariables.SavedDataSyncMessage.class,
         SololevelingModVariables.SavedDataSyncMessage::buffer,
         SololevelingModVariables.SavedDataSyncMessage::new,
         SololevelingModVariables.SavedDataSyncMessage::handler
      );
      SololevelingMod.addNetworkMessage(
         SololevelingModVariables.PlayerVariablesSyncMessage.class,
         SololevelingModVariables.PlayerVariablesSyncMessage::buffer,
         SololevelingModVariables.PlayerVariablesSyncMessage::new,
         SololevelingModVariables.PlayerVariablesSyncMessage::handler
      );
   }

   @SubscribeEvent
   public static void init(RegisterCapabilitiesEvent event) {
      event.register(SololevelingModVariables.PlayerVariables.class);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         SololevelingModVariables.PlayerVariablesSyncMessage.class,
         SololevelingModVariables.PlayerVariablesSyncMessage::buffer,
         SololevelingModVariables.PlayerVariablesSyncMessage::new,
         SololevelingModVariables.PlayerVariablesSyncMessage::handler
      );
   }

   @EventBusSubscriber
   public static class EventBusVariableHandlers {
      @SubscribeEvent
      public static void onPlayerLoggedInSyncPlayerVariables(PlayerLoggedInEvent event) {
         if (!event.getEntity().level().isClientSide()) {
            event.getEntity()
               .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables())
               .syncPlayerVariables(event.getEntity());
         }
      }

      @SubscribeEvent
      public static void onPlayerRespawnedSyncPlayerVariables(PlayerRespawnEvent event) {
         if (!event.getEntity().level().isClientSide()) {
            event.getEntity()
               .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables())
               .syncPlayerVariables(event.getEntity());
         }
      }

      @SubscribeEvent
      public static void onPlayerChangedDimensionSyncPlayerVariables(PlayerChangedDimensionEvent event) {
         if (!event.getEntity().level().isClientSide()) {
            event.getEntity()
               .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables())
               .syncPlayerVariables(event.getEntity());
         }
      }

      @SubscribeEvent(priority = EventPriority.LOWEST)
      public static void flushPlayerVariablesSyncs(PlayerTickEvent event) {
         if (event.phase == Phase.END) {
            event.player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(cap -> cap.flushSyncIfPending(event.player));
         }
      }

      @SubscribeEvent
      public static void clonePlayer(Clone event) {
         event.getOriginal().revive();
         SololevelingModVariables.PlayerVariables original = event.getOriginal()
            .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         SololevelingModVariables.PlayerVariables clone = event.getEntity()
            .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         clone.shopitem5 = original.shopitem5;
         clone.shopitem1 = original.shopitem1;
         clone.shopitem2 = original.shopitem2;
         clone.shopitem3 = original.shopitem3;
         clone.shopitem4 = original.shopitem4;
         clone.shopitem6 = original.shopitem6;
         clone.Ab1 = original.Ab1;
         clone.Ab2 = original.Ab2;
         clone.Ab3 = original.Ab3;
         clone.Ab4 = original.Ab4;
         clone.abilities = original.abilities;
         clone.ActiveDaily = original.ActiveDaily;
         clone.alivestatus = original.alivestatus;
         clone.beru = original.beru;
         clone.berumax = original.berumax;
         clone.boss = original.boss;
         clone.BossKilled = original.BossKilled;
         clone.Call4Death = original.Call4Death;
         clone.Classes = original.Classes;
         clone.mageSpecialization = original.mageSpecialization;
         clone.combatmode = original.combatmode;
         clone.commanddeath = original.commanddeath;
         clone.dailykilltyppe = original.dailykilltyppe;
         clone.dailysecrettrans = original.dailysecrettrans;
         clone.dailytasks = original.dailytasks;
         clone.dailytimer = original.dailytimer;
         clone.dailyQuestSchema = original.dailyQuestSchema;
         clone.dailyMinedBlocks = original.dailyMinedBlocks;
         clone.dailyThreatPoints = original.dailyThreatPoints;
         clone.lastDailyQuestDay = original.lastDailyQuestDay;
         clone.dailyCombatWaived = original.dailyCombatWaived;
         clone.DeathX = original.DeathX;
         clone.DeathY = original.DeathY;
         clone.DeathZ = original.DeathZ;
         clone.Dialogue = original.Dialogue;
         clone.domain = original.domain;
         clone.dungeoning = original.dungeoning;
         clone.DungeonNum = original.DungeonNum;
         clone.DunX = original.DunX;
         clone.DunY = original.DunY;
         clone.DunZ = original.DunZ;
         clone.Durability = original.Durability;
         clone.giftstatus = original.giftstatus;
         clone.GobShadow = original.GobShadow;
         clone.GobShadowMax = original.GobShadowMax;
         clone.golds = original.golds;
         clone.guardbar = original.guardbar;
         clone.GuildCode = original.GuildCode;
         clone.HunterEyes = original.HunterEyes;
         clone.HunterRank = original.HunterRank;
         clone.igris = original.igris;
         clone.IgrisSpawned = original.IgrisSpawned;
         clone.Intelligence = original.Intelligence;
         clone.Mana = original.Mana;
         clone.investvalue = original.investvalue;
         clone.JOB = original.JOB;
         clone.vesselType = original.vesselType;
         clone.vesselIdentity = original.vesselIdentity;
         clone.vesselGrantedAuthority = original.vesselGrantedAuthority;
         clone.jobkey = original.jobkey;
         clone.killmission = original.killmission;
         clone.LastKilled = original.LastKilled;
         clone.Level = original.Level;
         clone.MainQuest = original.MainQuest;
         clone.QuestProgression = original.QuestProgression;
         clone.manaregen = original.manaregen;
         clone.MaxXP = original.MaxXP;
         clone.orcmax = original.orcmax;
         clone.orcspawned = original.orcspawned;
         clone.OrdShadow = original.OrdShadow;
         clone.ordshadowmax = original.ordshadowmax;
         clone.overridefeet = original.overridefeet;
         clone.overridehead = original.overridehead;
         clone.overridelegs = original.overridelegs;
         clone.overridetorso = original.overridetorso;
         clone.perception = original.perception;
         clone.Player = original.Player;
         clone.polarbear = original.polarbear;
         clone.polarbearmax = original.polarbearmax;
         clone.pushup = original.pushup;
         clone.ranking = original.ranking;
         clone.rankingnum = original.rankingnum;
         clone.resistance = original.resistance;
         clone.RUN = original.RUN;
         clone.RX = original.RX;
         clone.RZ = original.RZ;
         clone.ShadowExchange = original.ShadowExchange;
         clone.ShadowSelect = original.ShadowSelect;
         clone.shadowstorage = original.shadowstorage;
         clone.shadowstorageusage = original.shadowstorageusage;
         clone.situp = original.situp;
         clone.SkillPoints = original.SkillPoints;
         clone.slashfury = original.slashfury;
         clone.Speed = original.Speed;
         clone.speedpercent = original.speedpercent;
         clone.squat = original.squat;
         clone.statshown = original.statshown;
         clone.Strength = original.Strength;
         clone.summonlimit = original.summonlimit;
         clone.summonlimitusage = original.summonlimitusage;
         clone.tj = original.tj;
         clone.tjonoff = original.tjonoff;
         clone.Vitality = original.Vitality;
         clone.WolfShadow = original.WolfShadow;
         clone.WolfShadowMax = original.WolfShadowMax;
         clone.Xp = original.Xp;
         clone.xpmultiplier = original.xpmultiplier;
         clone.Money = original.Money;
         clone.CustomHUD = original.CustomHUD;
         clone.pvpUrgentQuests = original.pvpUrgentQuests;
         clone.ShadowGoblinArcherAmount = original.ShadowGoblinArcherAmount;
         clone.ShadowGoblinMageAmount = original.ShadowGoblinMageAmount;
         clone.ShadowGoblinArcherMax = original.ShadowGoblinArcherMax;
         clone.ShadowGoblinMageMax = original.ShadowGoblinMageMax;
         clone.shadowdragonnum = original.shadowdragonnum;
         clone.shadowdragonmax = original.shadowdragonmax;
         clone.packetCounter = original.packetCounter;
         clone.daily_refreshes = original.daily_refreshes;
         clone.selection = original.selection;
         clone.party = original.party;
         clone.prevRank = original.prevRank;
         clone.prevLevel = original.prevLevel;
         clone.idcd = original.idcd;
         clone.title = original.title;
         clone.unlockedTitles = original.unlockedTitles;
         clone.wolfAssassinKills = original.wolfAssassinKills;
         clone.Plist = original.Plist;
         clone.Pslot1 = original.Pslot1;
         clone.Pslot2 = original.Pslot2;
         clone.Pslot3 = original.Pslot3;
         clone.Pslot4 = original.Pslot4;
         clone.Pslot5 = original.Pslot5;
         clone.Pslot6 = original.Pslot6;
         clone.Pslot7 = original.Pslot7;
         clone.Pslot8 = original.Pslot8;
         clone.Pslot9 = original.Pslot9;
         clone.Pslot10 = original.Pslot10;
         clone.Pslot11 = original.Pslot11;
         clone.Pslot12 = original.Pslot12;
         clone.Pslot13 = original.Pslot13;
         clone.Pslot14 = original.Pslot14;
         clone.Pslot15 = original.Pslot15;
         clone.Pslot16 = original.Pslot16;
         clone.PskillPage = original.PskillPage;
         clone.PselectedPower = original.PselectedPower;
         clone.progression_assassin = original.progression_assassin;
         clone.progression_mage = original.progression_mage;
         clone.progression_fighter = original.progression_fighter;
         clone.progression_tanker = original.progression_tanker;
         clone.progression_healer = original.progression_healer;
         clone.progression_ranger = original.progression_ranger;
         clone.JobSkills = original.JobSkills;
         clone.ExchangeDimensions = original.ExchangeDimensions;
         clone.ExchangeCords = original.ExchangeCords;
         clone.ShadowBody = original.ShadowBody;
         clone.progression_multiplier_assassin = original.progression_multiplier_assassin;
         clone.progression_multiplier_mage = original.progression_multiplier_mage;
         clone.progression_multiplier_fighter = original.progression_multiplier_fighter;
         clone.progression_multiplier_tanker = original.progression_multiplier_tanker;
         clone.progression_multiplier_healer = original.progression_multiplier_healer;
         clone.progression_multiplier_ranger = original.progression_multiplier_ranger;
         clone.overlay_alpha_welcome = original.overlay_alpha_welcome;
         clone.progression_multiplier_dagger = original.progression_multiplier_dagger;
         clone.progression_dagger = original.progression_dagger;
         clone.overlay_alpha_dailyquestwarning = original.overlay_alpha_dailyquestwarning;
         clone.dkc_unlocked = original.dkc_unlocked;
         clone.unlocked_quests = original.unlocked_quests;
         clone.finished_quests = original.finished_quests;
         clone.highorcmax = original.highorcmax;
         clone.highorcspawned = original.highorcspawned;
         clone.tuskmax = original.tuskmax;
         clone.tuskspawned = original.tuskspawned;
         clone.Kaisel = original.Kaisel;
         clone.KaiselSpawned = original.KaiselSpawned;
         clone.reward_1 = original.reward_1;
         clone.reward_2 = original.reward_2;
         clone.reward_3 = original.reward_3;
         clone.reward_extra = original.reward_extra;
         clone.dkc_cleared = original.dkc_cleared;
         clone.dkc_x = original.dkc_x;
         clone.dkc_y = original.dkc_y;
         clone.dkc_z = original.dkc_z;
         clone.dkc_started = original.dkc_started;
         clone.radiru_pact = original.radiru_pact;
         clone.radiru_slaughtered = original.radiru_slaughtered;
         clone.radiru_side_quest_unlocked = original.radiru_side_quest_unlocked;
         clone.jobadvpoint = original.jobadvpoint;
         clone.JobChange_timer = original.JobChange_timer;
         clone.jobtimer = original.jobtimer;
         clone.randplayerx = original.randplayerx;
         clone.randplayery = original.randplayery;
         clone.randplayerz = original.randplayerz;
         if (!event.isWasDeath()) {
            clone.LoreAccurateRankStart = original.LoreAccurateRankStart;
            clone.ariset = original.ariset;
            clone.berserk = original.berserk;
            clone.daggermelee = original.daggermelee;
            clone.daggermeleetimer = original.daggermeleetimer;
            clone.dash = original.dash;
            clone.domainef = original.domainef;
            clone.DunRank = original.DunRank;
            clone.Fatigue = original.Fatigue;
            clone.firecharge = original.firecharge;
            clone.firestr = original.firestr;
            clone.FireVar = original.FireVar;
            clone.FRing = original.FRing;
            clone.frostcharge = original.frostcharge;
            clone.FX = original.FX;
            clone.FY = original.FY;
            clone.FZ = original.FZ;
            clone.GolemRage = original.GolemRage;
            clone.guard = original.guard;
            clone.guarding = original.guarding;
            clone.Imbuement = original.Imbuement;
            clone.impct1 = original.impct1;
            clone.instancecomplete = original.instancecomplete;
            clone.inv = original.inv;
            clone.istraining = original.istraining;
            clone.JP = original.JP;
            clone.kamishcharge = original.kamishcharge;
            clone.leapjump = original.leapjump;
            clone.leftpunch = original.leftpunch;
            clone.MP = original.MP;
            clone.paralyzenot = original.paralyzenot;
            clone.PhantomName = original.PhantomName;
            clone.punishment = original.punishment;
            clone.questinfo = original.questinfo;
            clone.radius1 = original.radius1;
            clone.rushattack = original.rushattack;
            clone.shieldbash = original.shieldbash;
            clone.Skillcycle = original.Skillcycle;
            clone.slashfur = original.slashfur;
            clone.slashfurrybroad = original.slashfurrybroad;
            clone.slashfurtimer = original.slashfurtimer;
            clone.spiderstat = original.spiderstat;
            clone.tpd = original.tpd;
            clone.TX = original.TX;
            clone.TY = original.TY;
            clone.TZ = original.TZ;
            clone.upforceslash = original.upforceslash;
            clone.wp = original.wp;
            clone.rangerleapnum = original.rangerleapnum;
            clone.rangerleaptimer = original.rangerleaptimer;
            clone.sl_EVA = original.sl_EVA;
            clone.traintype = original.traintype;
            clone.isdailytraining = original.isdailytraining;
            clone.instance_query_timer = original.instance_query_timer;
            clone.monarchbeam = original.monarchbeam;
            clone.baranlightningstrike = original.baranlightningstrike;
            clone.PslotSelecting = original.PslotSelecting;
            clone.FireRingTimer = original.FireRingTimer;
         }

         if (!event.getEntity().level().isClientSide()) {
            for (Entity entityiterator : new ArrayList<>(event.getEntity().level().players())) {
               entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
                  .syncPlayerVariables(entityiterator);
            }
         }
      }

      @SubscribeEvent
      public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
         if (!event.getEntity().level().isClientSide()) {
            SavedData mapdata = SololevelingModVariables.MapVariables.get(event.getEntity().level());
            SavedData worlddata = SololevelingModVariables.WorldVariables.get(event.getEntity().level());
            if (mapdata != null) {
               SololevelingMod.PACKET_HANDLER
                  .send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)event.getEntity()), new SololevelingModVariables.SavedDataSyncMessage(0, mapdata));
            }

            if (worlddata != null) {
               SololevelingMod.PACKET_HANDLER
                  .send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)event.getEntity()), new SololevelingModVariables.SavedDataSyncMessage(1, worlddata));
            }
         }
      }

      @SubscribeEvent
      public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
         if (!event.getEntity().level().isClientSide()) {
            SavedData worlddata = SololevelingModVariables.WorldVariables.get(event.getEntity().level());
            if (worlddata != null) {
               SololevelingMod.PACKET_HANDLER
                  .send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)event.getEntity()), new SololevelingModVariables.SavedDataSyncMessage(1, worlddata));
            }
         }
      }
   }

   public static class MapVariables extends SavedData {
      public static final String DATA_NAME = "sololeveling_mapvars";
      public boolean RedGate = false;
      public double gatetimer = 0.0;
      public double shmlimit = 0.0;
      public boolean portalreset = false;
      public boolean firstNaturalGateSpawned = false;
      public String GatesCleared = "";
      public String ActiveGateInstances = "\"\"";
      public String GateBreakMemory = "";
      static SololevelingModVariables.MapVariables clientSide = new SololevelingModVariables.MapVariables();

      public static SololevelingModVariables.MapVariables load(CompoundTag tag) {
         SololevelingModVariables.MapVariables data = new SololevelingModVariables.MapVariables();
         data.read(tag);
         return data;
      }

      public void read(CompoundTag nbt) {
         if (nbt == null) {
            nbt = this.save(new CompoundTag());
         }

         this.RedGate = nbt.getBoolean("RedGate");
         this.gatetimer = nbt.getDouble("gatetimer");
         this.shmlimit = nbt.getDouble("shmlimit");
         this.portalreset = nbt.getBoolean("portalreset");
         this.firstNaturalGateSpawned = nbt.getBoolean("firstNaturalGateSpawned");
         this.GatesCleared = nbt.getString("GatesCleared");
         this.ActiveGateInstances = nbt.getString("ActiveGateInstances");
         this.GateBreakMemory = nbt.getString("GateBreakMemory");
      }

      @Override
      public CompoundTag save(CompoundTag nbt) {
         nbt.putBoolean("RedGate", this.RedGate);
         nbt.putDouble("gatetimer", this.gatetimer);
         nbt.putDouble("shmlimit", this.shmlimit);
         nbt.putBoolean("portalreset", this.portalreset);
         nbt.putBoolean("firstNaturalGateSpawned", this.firstNaturalGateSpawned);
         nbt.putString("GatesCleared", this.GatesCleared);
         nbt.putString("ActiveGateInstances", this.ActiveGateInstances);
         nbt.putString("GateBreakMemory", this.GateBreakMemory);
         return nbt;
      }

      public void syncData(LevelAccessor world) {
         this.setDirty();
         if (world instanceof Level && !world.isClientSide()) {
            SololevelingMod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new SololevelingModVariables.SavedDataSyncMessage(0, this));
         }
      }

      public static SololevelingModVariables.MapVariables get(LevelAccessor world) {
         return world instanceof ServerLevelAccessor serverLevelAcc
            ? serverLevelAcc.getLevel()
               .getServer()
               .getLevel(Level.OVERWORLD)
               .getDataStorage()
               .computeIfAbsent(e -> load(e), SololevelingModVariables.MapVariables::new, "sololeveling_mapvars")
            : clientSide;
      }
   }

   public static class PlayerVariables {
      public ItemStack shopitem5 = ItemStack.EMPTY;
      public double LoreAccurateRankStart = 1.0;
      public double ariset = 0.0;
      public boolean berserk = false;
      public boolean daggermelee = false;
      public double daggermeleetimer = 0.0;
      public double dash = 1.0;
      public boolean domainef = false;
      public double DunRank = 0.0;
      public double Fatigue = 0.0;
      public double firecharge = 0.0;
      public double firestr = 0.0;
      public double FireVar = 0.0;
      public boolean FRing = false;
      public double frostcharge = 0.0;
      public double FX = 0.0;
      public double FY = 0.0;
      public double FZ = 0.0;
      public boolean GolemRage = false;
      public boolean guard = false;
      public double guarding = 50.0;
      public boolean Imbuement = false;
      public double impct1 = 0.0;
      public boolean instancecomplete = false;
      public boolean inv = false;
      public boolean istraining = false;
      public double jobadvpoint = 0.0;
      public double JobChange_timer = 0.0;
      public double jobtimer = 0.0;
      public boolean JP = false;
      public double kamishcharge = 0.0;
      public boolean leapjump = false;
      public boolean leftpunch = false;
      public double Mana = 0.0;
      public double MP = 0.0;
      public boolean paralyzenot = false;
      public String PhantomName = "\"\"";
      public double punishment = 0.0;
      public boolean questinfo = false;
      public double QuestProgression = 0.0;
      public double radius1 = 0.0;
      public boolean rushattack = false;
      public boolean shieldbash = false;
      public double Skillcycle = 0.0;
      public boolean slashfur = false;
      public boolean slashfurrybroad = false;
      public double slashfurtimer = 0.0;
      public boolean spiderstat = false;
      public boolean tpd = false;
      public double TX = 0.0;
      public double TY = 0.0;
      public double TZ = 0.0;
      public boolean upforceslash = false;
      public double wp = 0.0;
      public double rangerleapnum = 0.0;
      public double rangerleaptimer = 0.0;
      public double sl_EVA = 0.0;
      public double randplayerx = 0.0;
      public double randplayery = 0.0;
      public double randplayerz = 0.0;
      public String traintype = "";
      public boolean isdailytraining = false;
      public ItemStack shopitem1 = ItemStack.EMPTY;
      public ItemStack shopitem2 = ItemStack.EMPTY;
      public ItemStack shopitem3 = ItemStack.EMPTY;
      public ItemStack shopitem4 = ItemStack.EMPTY;
      public ItemStack shopitem6 = ItemStack.EMPTY;
      public boolean Ab1 = true;
      public boolean Ab2 = true;
      public boolean Ab3 = true;
      public boolean Ab4 = true;
      public String abilities = "\"\"";
      public boolean ActiveDaily = false;
      public boolean alivestatus = false;
      public double beru = 0.0;
      public double berumax = 0.0;
      public double boss = 0.0;
      public boolean BossKilled = false;
      public boolean Call4Death = false;
      public double Classes = 0.0;
      public String mageSpecialization = "";
      public boolean combatmode = false;
      public boolean commanddeath = false;
      public double dailykilltyppe = 0.0;
      public double dailysecrettrans = 1.0;
      public double dailytasks = 0.0;
      public double dailytimer = 0.0;
      public int dailyQuestSchema = 0;
      public double dailyMinedBlocks = 0.0;
      public double dailyThreatPoints = 0.0;
      public long lastDailyQuestDay = Long.MIN_VALUE;
      public boolean dailyCombatWaived = false;
      public double DeathX = 0.0;
      public double DeathY = 0.0;
      public double DeathZ = 0.0;
      public String Dialogue = "";
      public double domain = 0.0;
      public boolean dungeoning = false;
      public double DungeonNum = 0.0;
      public double DunX = 0.0;
      public double DunY = 0.0;
      public double DunZ = 0.0;
      public double Durability = 0.0;
      public boolean giftstatus = false;
      public double GobShadow = 0.0;
      public double GobShadowMax = 0.0;
      public double golds = 0.0;
      public double guardbar = 0.0;
      public double GuildCode = 0.0;
      public boolean HunterEyes = false;
      public double HunterRank = 0.0;
      public double igris = 0.0;
      public double IgrisSpawned = 0.0;
      public double Intelligence = 0.0;
      public double investvalue = 1.0;
      public double JOB = 0.0;
      public String vesselType = "";
      public String vesselIdentity = "";
      public boolean vesselGrantedAuthority = false;
      public boolean jobkey = false;
      public double killmission = 9.0;
      public double LastKilled = 0.0;
      public double Level = 0.0;
      public String MainQuest = "";
      public double manaregen = 0.0;
      public double MaxXP = 10.0;
      public double orcmax = 0.0;
      public double orcspawned = 0.0;
      public double OrdShadow = 0.0;
      public double ordshadowmax = 0.0;
      public ItemStack overridefeet = ItemStack.EMPTY;
      public ItemStack overridehead = ItemStack.EMPTY;
      public ItemStack overridelegs = ItemStack.EMPTY;
      public ItemStack overridetorso = ItemStack.EMPTY;
      public double perception = 0.0;
      public boolean Player = false;
      public double polarbear = 0.0;
      public double polarbearmax = 0.0;
      public double pushup = 0.0;
      public String ranking = "";
      public double rankingnum = 0.0;
      public boolean resistance = false;
      public double RUN = 0.0;
      public double RX = 0.0;
      public double RZ = 0.0;
      public boolean ShadowExchange = false;
      public double ShadowSelect = 1.0;
      public double shadowstorage = 10.0;
      public double shadowstorageusage = 0.0;
      public double situp = 0.0;
      public double SkillPoints = 0.0;
      public double slashfury = 0.0;
      public double Speed = 0.0;
      public double speedpercent = 100.0;
      public double squat = 0.0;
      public double statshown = 0.0;
      public double Strength = 0.0;
      public double summonlimit = 0.0;
      public double summonlimitusage = 0.0;
      public double tj = 0.0;
      public boolean tjonoff = true;
      public double Vitality = 0.0;
      public double WolfShadow = 0.0;
      public double WolfShadowMax = 0.0;
      public double Xp = 0.0;
      public double xpmultiplier = 1.0;
      public double Money = 0.0;
      public boolean CustomHUD = true;
      public boolean pvpUrgentQuests = true;
      public double ShadowGoblinArcherAmount = 0.0;
      public double ShadowGoblinMageAmount = 0.0;
      public double ShadowGoblinArcherMax = 0.0;
      public double ShadowGoblinMageMax = 0.0;
      public double shadowdragonnum = 0.0;
      public double shadowdragonmax = 0.0;
      public double packetCounter = 0.0;
      public double instance_query_timer = 0.0;
      public double daily_refreshes = 0.0;
      public boolean selection = false;
      public String party = "";
      public boolean monarchbeam = false;
      public double baranlightningstrike = 0.0;
      public double prevRank = 0.0;
      public double prevLevel = 0.0;
      public double idcd = 0.0;
      public double title = 0.0;
      public String unlockedTitles = "";
      public double wolfAssassinKills = 0.0;
      public String Plist = ".";
      public String Pslot1 = "";
      public String Pslot2 = "";
      public String Pslot3 = "";
      public String Pslot4 = "";
      public String Pslot5 = "";
      public String Pslot6 = "";
      public String Pslot7 = "";
      public String Pslot8 = "";
      public String Pslot9 = "";
      public String Pslot10 = "";
      public String Pslot11 = "";
      public String Pslot12 = "";
      public String Pslot13 = "";
      public String Pslot14 = "";
      public String Pslot15 = "";
      public String Pslot16 = "";
      public double PskillPage = 1.0;
      public double PslotSelecting = 0.0;
      public String PselectedPower = "";
      public double progression_assassin = 0.0;
      public double progression_mage = 0.0;
      public double progression_fighter = 0.0;
      public double progression_tanker = 0.0;
      public double progression_healer = 0.0;
      public double progression_ranger = 0.0;
      public String JobSkills = "\"\"";
      public String ExchangeDimensions = ".";
      public String ExchangeCords = ".";
      public boolean ShadowBody = false;
      public double progression_multiplier_assassin = 1.0;
      public double progression_multiplier_mage = 1.0;
      public double progression_multiplier_fighter = 1.0;
      public double progression_multiplier_tanker = 1.0;
      public double progression_multiplier_healer = 1.0;
      public double progression_multiplier_ranger = 1.0;
      public double overlay_alpha_welcome = 0.0;
      public double progression_multiplier_dagger = 1.0;
      public double progression_dagger = 0.0;
      public double overlay_alpha_dailyquestwarning = 0.0;
      public double dkc_unlocked = 0.0;
      public String unlocked_quests = "\"\"";
      public String finished_quests = "\"\"";
      public double highorcmax = 0.0;
      public double highorcspawned = 0.0;
      public double tuskmax = 0.0;
      public double tuskspawned = 0.0;
      public double Kaisel = 0.0;
      public double KaiselSpawned = 0.0;
      public double FireRingTimer = 0.0;
      public String reward_1 = "\"\"";
      public String reward_2 = "\"\"";
      public String reward_3 = "\"\"";
      public String reward_extra = "";
      public double dkc_cleared = 0.0;
      public double dkc_x = 0.0;
      public double dkc_y = 0.0;
      public double dkc_z = 0.0;
      public boolean dkc_started = false;
      public boolean radiru_pact = false;
      public boolean radiru_slaughtered = false;
      public boolean radiru_side_quest_unlocked = false;
      public String cooldownData = "";
      private transient long lastSyncTick = -1L;
      private transient boolean pendingSync = false;

      public void syncPlayerVariables(Entity entity) {
         if (entity instanceof ServerPlayer serverPlayer) {
            long currentTick = serverPlayer.level().getGameTime();
            if (currentTick == this.lastSyncTick) {
               this.pendingSync = true;
            } else {
               this.lastSyncTick = currentTick;
               this.pendingSync = false;
               SololevelingMod.PACKET_HANDLER
                  .send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SololevelingModVariables.PlayerVariablesSyncMessage(this, serverPlayer.getId()));
            }
         }
      }

      public void flushSyncIfPending(Entity entity) {
         if (this.pendingSync && entity instanceof ServerPlayer serverPlayer) {
            this.pendingSync = false;
            this.lastSyncTick = serverPlayer.level().getGameTime();
            SololevelingMod.PACKET_HANDLER
               .send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SololevelingModVariables.PlayerVariablesSyncMessage(this, serverPlayer.getId()));
         }
      }

      public Tag writeNBT() {
         CompoundTag nbt = new CompoundTag();
         nbt.put("shopitem5", this.shopitem5.save(new CompoundTag()));
         nbt.putDouble("LoreAccurateRankStart", this.LoreAccurateRankStart);
         nbt.putDouble("ariset", this.ariset);
         nbt.putBoolean("berserk", this.berserk);
         nbt.putBoolean("daggermelee", this.daggermelee);
         nbt.putDouble("daggermeleetimer", this.daggermeleetimer);
         nbt.putDouble("dash", this.dash);
         nbt.putBoolean("domainef", this.domainef);
         nbt.putDouble("DunRank", this.DunRank);
         nbt.putDouble("Fatigue", this.Fatigue);
         nbt.putDouble("firecharge", this.firecharge);
         nbt.putDouble("firestr", this.firestr);
         nbt.putDouble("FireVar", this.FireVar);
         nbt.putBoolean("FRing", this.FRing);
         nbt.putDouble("frostcharge", this.frostcharge);
         nbt.putDouble("FX", this.FX);
         nbt.putDouble("FY", this.FY);
         nbt.putDouble("FZ", this.FZ);
         nbt.putBoolean("GolemRage", this.GolemRage);
         nbt.putBoolean("guard", this.guard);
         nbt.putDouble("guarding", this.guarding);
         nbt.putBoolean("Imbuement", this.Imbuement);
         nbt.putDouble("impct1", this.impct1);
         nbt.putBoolean("instancecomplete", this.instancecomplete);
         nbt.putBoolean("inv", this.inv);
         nbt.putBoolean("istraining", this.istraining);
         nbt.putDouble("jobadvpoint", this.jobadvpoint);
         nbt.putDouble("JobChange_timer", this.JobChange_timer);
         nbt.putDouble("jobtimer", this.jobtimer);
         nbt.putBoolean("JP", this.JP);
         nbt.putDouble("kamishcharge", this.kamishcharge);
         nbt.putBoolean("leapjump", this.leapjump);
         nbt.putBoolean("leftpunch", this.leftpunch);
         nbt.putDouble("Mana", this.Mana);
         nbt.putDouble("MP", this.MP);
         nbt.putBoolean("paralyzenot", this.paralyzenot);
         nbt.putString("PhantomName", this.PhantomName);
         nbt.putDouble("punishment", this.punishment);
         nbt.putBoolean("questinfo", this.questinfo);
         nbt.putDouble("QuestProgression", this.QuestProgression);
         nbt.putDouble("radius1", this.radius1);
         nbt.putBoolean("rushattack", this.rushattack);
         nbt.putBoolean("shieldbash", this.shieldbash);
         nbt.putDouble("Skillcycle", this.Skillcycle);
         nbt.putBoolean("slashfur", this.slashfur);
         nbt.putBoolean("slashfurrybroad", this.slashfurrybroad);
         nbt.putDouble("slashfurtimer", this.slashfurtimer);
         nbt.putBoolean("spiderstat", this.spiderstat);
         nbt.putBoolean("tpd", this.tpd);
         nbt.putDouble("TX", this.TX);
         nbt.putDouble("TY", this.TY);
         nbt.putDouble("TZ", this.TZ);
         nbt.putBoolean("upforceslash", this.upforceslash);
         nbt.putDouble("wp", this.wp);
         nbt.putDouble("rangerleapnum", this.rangerleapnum);
         nbt.putDouble("rangerleaptimer", this.rangerleaptimer);
         nbt.putDouble("sl_EVA", this.sl_EVA);
         nbt.putDouble("randplayerx", this.randplayerx);
         nbt.putDouble("randplayery", this.randplayery);
         nbt.putDouble("randplayerz", this.randplayerz);
         nbt.putString("traintype", this.traintype);
         nbt.putBoolean("isdailytraining", this.isdailytraining);
         nbt.put("shopitem1", this.shopitem1.save(new CompoundTag()));
         nbt.put("shopitem2", this.shopitem2.save(new CompoundTag()));
         nbt.put("shopitem3", this.shopitem3.save(new CompoundTag()));
         nbt.put("shopitem4", this.shopitem4.save(new CompoundTag()));
         nbt.put("shopitem6", this.shopitem6.save(new CompoundTag()));
         nbt.putBoolean("Ab1", this.Ab1);
         nbt.putBoolean("Ab2", this.Ab2);
         nbt.putBoolean("Ab3", this.Ab3);
         nbt.putBoolean("Ab4", this.Ab4);
         nbt.putString("abilities", this.abilities);
         nbt.putBoolean("ActiveDaily", this.ActiveDaily);
         nbt.putBoolean("alivestatus", this.alivestatus);
         nbt.putDouble("beru", this.beru);
         nbt.putDouble("berumax", this.berumax);
         nbt.putDouble("boss", this.boss);
         nbt.putBoolean("BossKilled", this.BossKilled);
         nbt.putBoolean("Call4Death", this.Call4Death);
         nbt.putDouble("Classes", this.Classes);
         nbt.putString("mageSpecialization", this.mageSpecialization);
         nbt.putBoolean("combatmode", this.combatmode);
         nbt.putBoolean("commanddeath", this.commanddeath);
         nbt.putDouble("dailykilltyppe", this.dailykilltyppe);
         nbt.putDouble("dailysecrettrans", this.dailysecrettrans);
         nbt.putDouble("dailytasks", this.dailytasks);
         nbt.putDouble("dailytimer", this.dailytimer);
         nbt.putInt("dailyQuestSchema", this.dailyQuestSchema);
         nbt.putDouble("dailyMinedBlocks", this.dailyMinedBlocks);
         nbt.putDouble("dailyThreatPoints", this.dailyThreatPoints);
         nbt.putLong("lastDailyQuestDay", this.lastDailyQuestDay);
         nbt.putBoolean("dailyCombatWaived", this.dailyCombatWaived);
         nbt.putDouble("DeathX", this.DeathX);
         nbt.putDouble("DeathY", this.DeathY);
         nbt.putDouble("DeathZ", this.DeathZ);
         nbt.putString("Dialogue", this.Dialogue);
         nbt.putDouble("domain", this.domain);
         nbt.putBoolean("dungeoning", this.dungeoning);
         nbt.putDouble("DungeonNum", this.DungeonNum);
         nbt.putDouble("DunX", this.DunX);
         nbt.putDouble("DunY", this.DunY);
         nbt.putDouble("DunZ", this.DunZ);
         nbt.putDouble("Durability", this.Durability);
         nbt.putBoolean("giftstatus", this.giftstatus);
         nbt.putDouble("GobShadow", this.GobShadow);
         nbt.putDouble("GobShadowMax", this.GobShadowMax);
         nbt.putDouble("golds", this.golds);
         nbt.putDouble("guardbar", this.guardbar);
         nbt.putDouble("GuildCode", this.GuildCode);
         nbt.putBoolean("HunterEyes", this.HunterEyes);
         nbt.putDouble("HunterRank", this.HunterRank);
         nbt.putDouble("igris", this.igris);
         nbt.putDouble("IgrisSpawned", this.IgrisSpawned);
         nbt.putDouble("Intelligence", this.Intelligence);
         nbt.putDouble("investvalue", this.investvalue);
         nbt.putDouble("JOB", this.JOB);
         nbt.putString("vesselType", this.vesselType);
         nbt.putString("vesselIdentity", this.vesselIdentity);
         nbt.putBoolean("vesselGrantedAuthority", this.vesselGrantedAuthority);
         nbt.putBoolean("jobkey", this.jobkey);
         nbt.putDouble("killmission", this.killmission);
         nbt.putDouble("LastKilled", this.LastKilled);
         nbt.putDouble("Level", this.Level);
         nbt.putString("MainQuest", this.MainQuest);
         nbt.putDouble("manaregen", this.manaregen);
         nbt.putDouble("MaxXP", this.MaxXP);
         nbt.putDouble("orcmax", this.orcmax);
         nbt.putDouble("orcspawned", this.orcspawned);
         nbt.putDouble("OrdShadow", this.OrdShadow);
         nbt.putDouble("ordshadowmax", this.ordshadowmax);
         nbt.put("overridefeet", this.overridefeet.save(new CompoundTag()));
         nbt.put("overridehead", this.overridehead.save(new CompoundTag()));
         nbt.put("overridelegs", this.overridelegs.save(new CompoundTag()));
         nbt.put("overridetorso", this.overridetorso.save(new CompoundTag()));
         nbt.putDouble("perception", this.perception);
         nbt.putBoolean("Player", this.Player);
         nbt.putDouble("polarbear", this.polarbear);
         nbt.putDouble("polarbearmax", this.polarbearmax);
         nbt.putDouble("pushup", this.pushup);
         nbt.putString("ranking", this.ranking);
         nbt.putDouble("rankingnum", this.rankingnum);
         nbt.putBoolean("resistance", this.resistance);
         nbt.putDouble("RUN", this.RUN);
         nbt.putDouble("RX", this.RX);
         nbt.putDouble("RZ", this.RZ);
         nbt.putBoolean("ShadowExchange", this.ShadowExchange);
         nbt.putDouble("ShadowSelect", this.ShadowSelect);
         nbt.putDouble("shadowstorage", this.shadowstorage);
         nbt.putDouble("shadowstorageusage", this.shadowstorageusage);
         nbt.putDouble("situp", this.situp);
         nbt.putDouble("SkillPoints", this.SkillPoints);
         nbt.putDouble("slashfury", this.slashfury);
         nbt.putDouble("Speed", this.Speed);
         nbt.putDouble("speedpercent", this.speedpercent);
         nbt.putDouble("squat", this.squat);
         nbt.putDouble("statshown", this.statshown);
         nbt.putDouble("Strength", this.Strength);
         nbt.putDouble("summonlimit", this.summonlimit);
         nbt.putDouble("summonlimitusage", this.summonlimitusage);
         nbt.putDouble("tj", this.tj);
         nbt.putBoolean("tjonoff", this.tjonoff);
         nbt.putDouble("Vitality", this.Vitality);
         nbt.putDouble("WolfShadow", this.WolfShadow);
         nbt.putDouble("WolfShadowMax", this.WolfShadowMax);
         nbt.putDouble("Xp", this.Xp);
         nbt.putDouble("xpmultiplier", this.xpmultiplier);
         nbt.putDouble("Money", this.Money);
         nbt.putBoolean("CustomHUD", this.CustomHUD);
         nbt.putBoolean("pvpUrgentQuests", this.pvpUrgentQuests);
         nbt.putDouble("ShadowGoblinArcherAmount", this.ShadowGoblinArcherAmount);
         nbt.putDouble("ShadowGoblinMageAmount", this.ShadowGoblinMageAmount);
         nbt.putDouble("ShadowGoblinArcherMax", this.ShadowGoblinArcherMax);
         nbt.putDouble("ShadowGoblinMageMax", this.ShadowGoblinMageMax);
         nbt.putDouble("shadowdragonnum", this.shadowdragonnum);
         nbt.putDouble("shadowdragonmax", this.shadowdragonmax);
         nbt.putDouble("packetCounter", this.packetCounter);
         nbt.putDouble("instance_query_timer", this.instance_query_timer);
         nbt.putDouble("daily_refreshes", this.daily_refreshes);
         nbt.putBoolean("selection", this.selection);
         nbt.putString("party", this.party);
         nbt.putBoolean("monarchbeam", this.monarchbeam);
         nbt.putDouble("baranlightningstrike", this.baranlightningstrike);
         nbt.putDouble("prevRank", this.prevRank);
         nbt.putDouble("prevLevel", this.prevLevel);
         nbt.putDouble("idcd", this.idcd);
         nbt.putDouble("title", this.title);
         nbt.putString("unlockedTitles", this.unlockedTitles);
         nbt.putDouble("wolfAssassinKills", this.wolfAssassinKills);
         nbt.putString("Plist", this.Plist);
         nbt.putString("Pslot1", this.Pslot1);
         nbt.putString("Pslot2", this.Pslot2);
         nbt.putString("Pslot3", this.Pslot3);
         nbt.putString("Pslot4", this.Pslot4);
         nbt.putString("Pslot5", this.Pslot5);
         nbt.putString("Pslot6", this.Pslot6);
         nbt.putString("Pslot7", this.Pslot7);
         nbt.putString("Pslot8", this.Pslot8);
         nbt.putString("Pslot9", this.Pslot9);
         nbt.putString("Pslot10", this.Pslot10);
         nbt.putString("Pslot11", this.Pslot11);
         nbt.putString("Pslot12", this.Pslot12);
         nbt.putString("Pslot13", this.Pslot13);
         nbt.putString("Pslot14", this.Pslot14);
         nbt.putString("Pslot15", this.Pslot15);
         nbt.putString("Pslot16", this.Pslot16);
         nbt.putDouble("PskillPage", this.PskillPage);
         nbt.putDouble("PslotSelecting", this.PslotSelecting);
         nbt.putString("PselectedPower", this.PselectedPower);
         nbt.putDouble("progression_assassin", this.progression_assassin);
         nbt.putDouble("progression_mage", this.progression_mage);
         nbt.putDouble("progression_fighter", this.progression_fighter);
         nbt.putDouble("progression_tanker", this.progression_tanker);
         nbt.putDouble("progression_healer", this.progression_healer);
         nbt.putDouble("progression_ranger", this.progression_ranger);
         nbt.putString("JobSkills", this.JobSkills);
         nbt.putString("ExchangeDimensions", this.ExchangeDimensions);
         nbt.putString("ExchangeCords", this.ExchangeCords);
         nbt.putBoolean("ShadowBody", this.ShadowBody);
         nbt.putDouble("progression_multiplier_assassin", this.progression_multiplier_assassin);
         nbt.putDouble("progression_multiplier_mage", this.progression_multiplier_mage);
         nbt.putDouble("progression_multiplier_fighter", this.progression_multiplier_fighter);
         nbt.putDouble("progression_multiplier_tanker", this.progression_multiplier_tanker);
         nbt.putDouble("progression_multiplier_healer", this.progression_multiplier_healer);
         nbt.putDouble("progression_multiplier_ranger", this.progression_multiplier_ranger);
         nbt.putDouble("overlay_alpha_welcome", this.overlay_alpha_welcome);
         nbt.putDouble("progression_multiplier_dagger", this.progression_multiplier_dagger);
         nbt.putDouble("progression_dagger", this.progression_dagger);
         nbt.putDouble("overlay_alpha_dailyquestwarning", this.overlay_alpha_dailyquestwarning);
         nbt.putDouble("dkc_unlocked", this.dkc_unlocked);
         nbt.putString("unlocked_quests", this.unlocked_quests);
         nbt.putString("finished_quests", this.finished_quests);
         nbt.putDouble("highorcmax", this.highorcmax);
         nbt.putDouble("highorcspawned", this.highorcspawned);
         nbt.putDouble("tuskmax", this.tuskmax);
         nbt.putDouble("tuskspawned", this.tuskspawned);
         nbt.putDouble("Kaisel", this.Kaisel);
         nbt.putDouble("KaiselSpawned", this.KaiselSpawned);
         nbt.putDouble("FireRingTimer", this.FireRingTimer);
         nbt.putString("reward_1", this.reward_1);
         nbt.putString("reward_2", this.reward_2);
         nbt.putString("reward_3", this.reward_3);
         nbt.putString("reward_extra", this.reward_extra);
         nbt.putDouble("dkc_cleared", this.dkc_cleared);
         nbt.putDouble("dkc_x", this.dkc_x);
         nbt.putDouble("dkc_y", this.dkc_y);
         nbt.putDouble("dkc_z", this.dkc_z);
         nbt.putBoolean("dkc_started", this.dkc_started);
         nbt.putBoolean("radiru_pact", this.radiru_pact);
         nbt.putBoolean("radiru_slaughtered", this.radiru_slaughtered);
         nbt.putBoolean("radiru_side_quest_unlocked", this.radiru_side_quest_unlocked);
         nbt.putString("cooldownData", this.cooldownData);
         return nbt;
      }

      public void readNBT(Tag tag) {
         if (tag == null) {
            tag = this.writeNBT();
         }

         CompoundTag nbt = (CompoundTag)tag;
         if (nbt == null) {
            nbt = (CompoundTag)this.writeNBT();
         }

         this.shopitem5 = ItemStack.of(nbt.getCompound("shopitem5"));
         this.LoreAccurateRankStart = nbt.getDouble("LoreAccurateRankStart");
         this.ariset = nbt.getDouble("ariset");
         this.berserk = nbt.getBoolean("berserk");
         this.daggermelee = nbt.getBoolean("daggermelee");
         this.daggermeleetimer = nbt.getDouble("daggermeleetimer");
         this.dash = nbt.getDouble("dash");
         this.domainef = nbt.getBoolean("domainef");
         this.DunRank = nbt.getDouble("DunRank");
         this.Fatigue = nbt.getDouble("Fatigue");
         this.firecharge = nbt.getDouble("firecharge");
         this.firestr = nbt.getDouble("firestr");
         this.FireVar = nbt.getDouble("FireVar");
         this.FRing = nbt.getBoolean("FRing");
         this.frostcharge = nbt.getDouble("frostcharge");
         this.FX = nbt.getDouble("FX");
         this.FY = nbt.getDouble("FY");
         this.FZ = nbt.getDouble("FZ");
         this.GolemRage = nbt.getBoolean("GolemRage");
         this.guard = nbt.getBoolean("guard");
         this.guarding = nbt.getDouble("guarding");
         this.Imbuement = nbt.getBoolean("Imbuement");
         this.impct1 = nbt.getDouble("impct1");
         this.instancecomplete = nbt.getBoolean("instancecomplete");
         this.inv = nbt.getBoolean("inv");
         this.istraining = nbt.getBoolean("istraining");
         this.jobadvpoint = nbt.getDouble("jobadvpoint");
         this.JobChange_timer = nbt.getDouble("JobChange_timer");
         this.jobtimer = nbt.getDouble("jobtimer");
         this.JP = nbt.getBoolean("JP");
         this.kamishcharge = nbt.getDouble("kamishcharge");
         this.leapjump = nbt.getBoolean("leapjump");
         this.leftpunch = nbt.getBoolean("leftpunch");
         this.Mana = nbt.getDouble("Mana");
         this.MP = nbt.getDouble("MP");
         this.paralyzenot = nbt.getBoolean("paralyzenot");
         this.PhantomName = nbt.getString("PhantomName");
         this.punishment = nbt.getDouble("punishment");
         this.questinfo = nbt.getBoolean("questinfo");
         this.QuestProgression = nbt.getDouble("QuestProgression");
         this.radius1 = nbt.getDouble("radius1");
         this.rushattack = nbt.getBoolean("rushattack");
         this.shieldbash = nbt.getBoolean("shieldbash");
         this.Skillcycle = nbt.getDouble("Skillcycle");
         this.slashfur = nbt.getBoolean("slashfur");
         this.slashfurrybroad = nbt.getBoolean("slashfurrybroad");
         this.slashfurtimer = nbt.getDouble("slashfurtimer");
         this.spiderstat = nbt.getBoolean("spiderstat");
         this.tpd = nbt.getBoolean("tpd");
         this.TX = nbt.getDouble("TX");
         this.TY = nbt.getDouble("TY");
         this.TZ = nbt.getDouble("TZ");
         this.upforceslash = nbt.getBoolean("upforceslash");
         this.wp = nbt.getDouble("wp");
         this.rangerleapnum = nbt.getDouble("rangerleapnum");
         this.rangerleaptimer = nbt.getDouble("rangerleaptimer");
         this.sl_EVA = nbt.getDouble("sl_EVA");
         this.randplayerx = nbt.getDouble("randplayerx");
         this.randplayery = nbt.getDouble("randplayery");
         this.randplayerz = nbt.getDouble("randplayerz");
         this.traintype = nbt.getString("traintype");
         this.isdailytraining = nbt.getBoolean("isdailytraining");
         this.shopitem1 = ItemStack.of(nbt.getCompound("shopitem1"));
         this.shopitem2 = ItemStack.of(nbt.getCompound("shopitem2"));
         this.shopitem3 = ItemStack.of(nbt.getCompound("shopitem3"));
         this.shopitem4 = ItemStack.of(nbt.getCompound("shopitem4"));
         this.shopitem6 = ItemStack.of(nbt.getCompound("shopitem6"));
         this.Ab1 = nbt.getBoolean("Ab1");
         this.Ab2 = nbt.getBoolean("Ab2");
         this.Ab3 = nbt.getBoolean("Ab3");
         this.Ab4 = nbt.getBoolean("Ab4");
         this.abilities = nbt.getString("abilities");
         this.ActiveDaily = nbt.getBoolean("ActiveDaily");
         this.alivestatus = nbt.getBoolean("alivestatus");
         this.beru = nbt.getDouble("beru");
         this.berumax = nbt.getDouble("berumax");
         this.boss = nbt.getDouble("boss");
         this.BossKilled = nbt.getBoolean("BossKilled");
         this.Call4Death = nbt.getBoolean("Call4Death");
         this.Classes = nbt.getDouble("Classes");
         this.mageSpecialization = nbt.getString("mageSpecialization");
         this.combatmode = nbt.getBoolean("combatmode");
         this.commanddeath = nbt.getBoolean("commanddeath");
         this.dailykilltyppe = nbt.getDouble("dailykilltyppe");
         this.dailysecrettrans = nbt.contains("dailysecrettrans") ? nbt.getDouble("dailysecrettrans") : 1.0;
         this.dailytasks = nbt.getDouble("dailytasks");
         this.dailytimer = nbt.getDouble("dailytimer");
         this.dailyQuestSchema = nbt.contains("dailyQuestSchema") ? nbt.getInt("dailyQuestSchema") : 0;
         this.dailyMinedBlocks = nbt.getDouble("dailyMinedBlocks");
         this.dailyThreatPoints = nbt.getDouble("dailyThreatPoints");
         this.lastDailyQuestDay = nbt.contains("lastDailyQuestDay") ? nbt.getLong("lastDailyQuestDay") : Long.MIN_VALUE;
         this.dailyCombatWaived = nbt.getBoolean("dailyCombatWaived");
         this.DeathX = nbt.getDouble("DeathX");
         this.DeathY = nbt.getDouble("DeathY");
         this.DeathZ = nbt.getDouble("DeathZ");
         this.Dialogue = nbt.getString("Dialogue");
         this.domain = nbt.getDouble("domain");
         this.dungeoning = nbt.getBoolean("dungeoning");
         this.DungeonNum = nbt.getDouble("DungeonNum");
         this.DunX = nbt.getDouble("DunX");
         this.DunY = nbt.getDouble("DunY");
         this.DunZ = nbt.getDouble("DunZ");
         this.Durability = nbt.getDouble("Durability");
         this.giftstatus = nbt.getBoolean("giftstatus");
         this.GobShadow = nbt.getDouble("GobShadow");
         this.GobShadowMax = nbt.getDouble("GobShadowMax");
         this.golds = nbt.getDouble("golds");
         this.guardbar = nbt.getDouble("guardbar");
         this.GuildCode = nbt.getDouble("GuildCode");
         this.HunterEyes = nbt.getBoolean("HunterEyes");
         this.HunterRank = nbt.getDouble("HunterRank");
         this.igris = nbt.getDouble("igris");
         this.IgrisSpawned = nbt.getDouble("IgrisSpawned");
         this.Intelligence = nbt.getDouble("Intelligence");
         this.investvalue = nbt.getDouble("investvalue");
         this.JOB = nbt.getDouble("JOB");
         this.vesselType = nbt.getString("vesselType");
         this.vesselIdentity = nbt.getString("vesselIdentity");
         this.vesselGrantedAuthority = nbt.getBoolean("vesselGrantedAuthority");
         this.jobkey = nbt.getBoolean("jobkey");
         this.killmission = nbt.getDouble("killmission");
         this.LastKilled = nbt.getDouble("LastKilled");
         this.Level = nbt.getDouble("Level");
         this.MainQuest = nbt.getString("MainQuest");
         this.manaregen = nbt.getDouble("manaregen");
         this.MaxXP = nbt.getDouble("MaxXP");
         this.orcmax = nbt.getDouble("orcmax");
         this.orcspawned = nbt.getDouble("orcspawned");
         this.OrdShadow = nbt.getDouble("OrdShadow");
         this.ordshadowmax = nbt.getDouble("ordshadowmax");
         this.overridefeet = ItemStack.of(nbt.getCompound("overridefeet"));
         this.overridehead = ItemStack.of(nbt.getCompound("overridehead"));
         this.overridelegs = ItemStack.of(nbt.getCompound("overridelegs"));
         this.overridetorso = ItemStack.of(nbt.getCompound("overridetorso"));
         this.perception = nbt.getDouble("perception");
         this.Player = nbt.getBoolean("Player");
         this.polarbear = nbt.getDouble("polarbear");
         this.polarbearmax = nbt.getDouble("polarbearmax");
         this.pushup = nbt.getDouble("pushup");
         this.ranking = nbt.getString("ranking");
         this.rankingnum = nbt.getDouble("rankingnum");
         this.resistance = nbt.getBoolean("resistance");
         this.RUN = nbt.getDouble("RUN");
         this.RX = nbt.getDouble("RX");
         this.RZ = nbt.getDouble("RZ");
         this.ShadowExchange = nbt.getBoolean("ShadowExchange");
         this.ShadowSelect = nbt.getDouble("ShadowSelect");
         this.shadowstorage = nbt.getDouble("shadowstorage");
         this.shadowstorageusage = nbt.getDouble("shadowstorageusage");
         this.situp = nbt.getDouble("situp");
         this.SkillPoints = nbt.getDouble("SkillPoints");
         this.slashfury = nbt.getDouble("slashfury");
         this.Speed = nbt.getDouble("Speed");
         this.speedpercent = nbt.getDouble("speedpercent");
         this.squat = nbt.getDouble("squat");
         this.statshown = nbt.getDouble("statshown");
         this.Strength = nbt.getDouble("Strength");
         this.summonlimit = nbt.getDouble("summonlimit");
         this.summonlimitusage = nbt.getDouble("summonlimitusage");
         this.tj = nbt.getDouble("tj");
         this.tjonoff = nbt.getBoolean("tjonoff");
         this.Vitality = nbt.getDouble("Vitality");
         this.WolfShadow = nbt.getDouble("WolfShadow");
         this.WolfShadowMax = nbt.getDouble("WolfShadowMax");
         this.Xp = nbt.getDouble("Xp");
         this.xpmultiplier = nbt.getDouble("xpmultiplier");
         this.Money = nbt.getDouble("Money");
         this.CustomHUD = nbt.getBoolean("CustomHUD");
         this.pvpUrgentQuests = !nbt.contains("pvpUrgentQuests") || nbt.getBoolean("pvpUrgentQuests");
         this.ShadowGoblinArcherAmount = nbt.getDouble("ShadowGoblinArcherAmount");
         this.ShadowGoblinMageAmount = nbt.getDouble("ShadowGoblinMageAmount");
         this.ShadowGoblinArcherMax = nbt.getDouble("ShadowGoblinArcherMax");
         this.ShadowGoblinMageMax = nbt.getDouble("ShadowGoblinMageMax");
         this.shadowdragonnum = nbt.getDouble("shadowdragonnum");
         this.shadowdragonmax = nbt.getDouble("shadowdragonmax");
         this.packetCounter = nbt.getDouble("packetCounter");
         this.instance_query_timer = nbt.getDouble("instance_query_timer");
         this.daily_refreshes = nbt.getDouble("daily_refreshes");
         this.selection = nbt.getBoolean("selection");
         this.party = nbt.getString("party");
         this.monarchbeam = nbt.getBoolean("monarchbeam");
         this.baranlightningstrike = nbt.getDouble("baranlightningstrike");
         this.prevRank = nbt.getDouble("prevRank");
         this.prevLevel = nbt.getDouble("prevLevel");
         this.idcd = nbt.getDouble("idcd");
         this.title = nbt.getDouble("title");
         this.unlockedTitles = nbt.getString("unlockedTitles");
         this.wolfAssassinKills = nbt.getDouble("wolfAssassinKills");
         if (this.title == 1.0 && !this.unlockedTitles.contains("wolf_assassin")) {
            this.unlockedTitles = this.unlockedTitles.isEmpty() ? "wolf_assassin" : this.unlockedTitles + ",wolf_assassin";
         }

         this.Plist = nbt.getString("Plist");
         this.Pslot1 = nbt.getString("Pslot1");
         this.Pslot2 = nbt.getString("Pslot2");
         this.Pslot3 = nbt.getString("Pslot3");
         this.Pslot4 = nbt.getString("Pslot4");
         this.Pslot5 = nbt.getString("Pslot5");
         this.Pslot6 = nbt.getString("Pslot6");
         this.Pslot7 = nbt.getString("Pslot7");
         this.Pslot8 = nbt.getString("Pslot8");
         this.Pslot9 = nbt.getString("Pslot9");
         this.Pslot10 = nbt.getString("Pslot10");
         this.Pslot11 = nbt.getString("Pslot11");
         this.Pslot12 = nbt.getString("Pslot12");
         this.Pslot13 = nbt.getString("Pslot13");
         this.Pslot14 = nbt.getString("Pslot14");
         this.Pslot15 = nbt.getString("Pslot15");
         this.Pslot16 = nbt.getString("Pslot16");
         this.PskillPage = nbt.contains("PskillPage") ? nbt.getDouble("PskillPage") : 1.0;
         this.PslotSelecting = nbt.getDouble("PslotSelecting");
         this.PselectedPower = nbt.getString("PselectedPower");
         this.progression_assassin = nbt.getDouble("progression_assassin");
         this.progression_mage = nbt.getDouble("progression_mage");
         this.progression_fighter = nbt.getDouble("progression_fighter");
         this.progression_tanker = nbt.getDouble("progression_tanker");
         this.progression_healer = nbt.getDouble("progression_healer");
         this.progression_ranger = nbt.getDouble("progression_ranger");
         this.JobSkills = nbt.getString("JobSkills");
         this.ExchangeDimensions = nbt.getString("ExchangeDimensions");
         this.ExchangeCords = nbt.getString("ExchangeCords");
         this.ShadowBody = nbt.getBoolean("ShadowBody");
         this.progression_multiplier_assassin = nbt.getDouble("progression_multiplier_assassin");
         this.progression_multiplier_mage = nbt.getDouble("progression_multiplier_mage");
         this.progression_multiplier_fighter = nbt.getDouble("progression_multiplier_fighter");
         this.progression_multiplier_tanker = nbt.getDouble("progression_multiplier_tanker");
         this.progression_multiplier_healer = nbt.getDouble("progression_multiplier_healer");
         this.progression_multiplier_ranger = nbt.getDouble("progression_multiplier_ranger");
         this.overlay_alpha_welcome = nbt.getDouble("overlay_alpha_welcome");
         this.progression_multiplier_dagger = nbt.getDouble("progression_multiplier_dagger");
         this.progression_dagger = nbt.getDouble("progression_dagger");
         this.overlay_alpha_dailyquestwarning = nbt.getDouble("overlay_alpha_dailyquestwarning");
         this.dkc_unlocked = nbt.getDouble("dkc_unlocked");
         this.unlocked_quests = nbt.getString("unlocked_quests");
         this.finished_quests = nbt.getString("finished_quests");
         this.highorcmax = nbt.getDouble("highorcmax");
         this.highorcspawned = nbt.getDouble("highorcspawned");
         this.tuskmax = nbt.getDouble("tuskmax");
         this.tuskspawned = nbt.getDouble("tuskspawned");
         this.Kaisel = nbt.getDouble("Kaisel");
         this.KaiselSpawned = nbt.getDouble("KaiselSpawned");
         this.FireRingTimer = nbt.getDouble("FireRingTimer");
         this.reward_1 = nbt.getString("reward_1");
         this.reward_2 = nbt.getString("reward_2");
         this.reward_3 = nbt.getString("reward_3");
         this.reward_extra = nbt.getString("reward_extra");
         this.dkc_cleared = nbt.getDouble("dkc_cleared");
         this.dkc_x = nbt.getDouble("dkc_x");
         this.dkc_y = nbt.getDouble("dkc_y");
         this.dkc_z = nbt.getDouble("dkc_z");
         this.dkc_started = nbt.getBoolean("dkc_started");
         this.radiru_pact = nbt.getBoolean("radiru_pact");
         this.radiru_slaughtered = nbt.getBoolean("radiru_slaughtered");
         this.radiru_side_quest_unlocked = nbt.getBoolean("radiru_side_quest_unlocked");
         this.cooldownData = nbt.getString("cooldownData");
      }
   }

   @EventBusSubscriber
   private static class PlayerVariablesProvider implements ICapabilitySerializable<Tag> {
      private final SololevelingModVariables.PlayerVariables playerVariables = new SololevelingModVariables.PlayerVariables();
      private final LazyOptional<SololevelingModVariables.PlayerVariables> instance = LazyOptional.of(() -> this.playerVariables);

      @SubscribeEvent
      public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
         if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer)) {
            event.addCapability(new ResourceLocation("sololeveling", "player_variables"), new SololevelingModVariables.PlayerVariablesProvider());
         }
      }

      @Override
      public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
         return cap == SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY ? this.instance.cast() : LazyOptional.empty();
      }

      @Override
      public Tag serializeNBT() {
         return this.playerVariables.writeNBT();
      }

      @Override
      public void deserializeNBT(Tag nbt) {
         this.playerVariables.readNBT(nbt);
      }
   }

   public static class PlayerVariablesSyncMessage {
      private final int target;
      private final SololevelingModVariables.PlayerVariables data;

      public PlayerVariablesSyncMessage(FriendlyByteBuf buffer) {
         this.data = new SololevelingModVariables.PlayerVariables();
         this.data.readNBT(buffer.readNbt());
         this.target = buffer.readInt();
      }

      public PlayerVariablesSyncMessage(SololevelingModVariables.PlayerVariables data, int entityid) {
         this.data = data;
         this.target = entityid;
      }

      public static void buffer(SololevelingModVariables.PlayerVariablesSyncMessage message, FriendlyByteBuf buffer) {
         buffer.writeNbt((CompoundTag)message.data.writeNBT());
         buffer.writeInt(message.target);
      }

      public static void handler(SololevelingModVariables.PlayerVariablesSyncMessage message, Supplier<Context> contextSupplier) {
         Context context = contextSupplier.get();
         context.enqueueWork(
            () -> {
               if (!context.getDirection().getReceptionSide().isServer()) {
                  SololevelingModVariables.PlayerVariables variables = Minecraft.getInstance()
                     .player
                     .level()
                     .getEntity(message.target)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables());
                  variables.shopitem5 = message.data.shopitem5;
                  variables.LoreAccurateRankStart = message.data.LoreAccurateRankStart;
                  variables.ariset = message.data.ariset;
                  variables.berserk = message.data.berserk;
                  variables.daggermelee = message.data.daggermelee;
                  variables.daggermeleetimer = message.data.daggermeleetimer;
                  variables.dash = message.data.dash;
                  variables.domainef = message.data.domainef;
                  variables.DunRank = message.data.DunRank;
                  variables.Fatigue = message.data.Fatigue;
                  variables.firecharge = message.data.firecharge;
                  variables.firestr = message.data.firestr;
                  variables.FireVar = message.data.FireVar;
                  variables.FRing = message.data.FRing;
                  variables.frostcharge = message.data.frostcharge;
                  variables.FX = message.data.FX;
                  variables.FY = message.data.FY;
                  variables.FZ = message.data.FZ;
                  variables.GolemRage = message.data.GolemRage;
                  variables.guard = message.data.guard;
                  variables.guarding = message.data.guarding;
                  variables.Imbuement = message.data.Imbuement;
                  variables.impct1 = message.data.impct1;
                  variables.instancecomplete = message.data.instancecomplete;
                  variables.inv = message.data.inv;
                  variables.istraining = message.data.istraining;
                  variables.jobadvpoint = message.data.jobadvpoint;
                  variables.JobChange_timer = message.data.JobChange_timer;
                  variables.jobtimer = message.data.jobtimer;
                  variables.JP = message.data.JP;
                  variables.kamishcharge = message.data.kamishcharge;
                  variables.leapjump = message.data.leapjump;
                  variables.leftpunch = message.data.leftpunch;
                  variables.Mana = message.data.Mana;
                  variables.MP = message.data.MP;
                  variables.paralyzenot = message.data.paralyzenot;
                  variables.PhantomName = message.data.PhantomName;
                  variables.punishment = message.data.punishment;
                  variables.questinfo = message.data.questinfo;
                  variables.QuestProgression = message.data.QuestProgression;
                  variables.radius1 = message.data.radius1;
                  variables.rushattack = message.data.rushattack;
                  variables.shieldbash = message.data.shieldbash;
                  variables.Skillcycle = message.data.Skillcycle;
                  variables.slashfur = message.data.slashfur;
                  variables.slashfurrybroad = message.data.slashfurrybroad;
                  variables.slashfurtimer = message.data.slashfurtimer;
                  variables.spiderstat = message.data.spiderstat;
                  variables.tpd = message.data.tpd;
                  variables.TX = message.data.TX;
                  variables.TY = message.data.TY;
                  variables.TZ = message.data.TZ;
                  variables.upforceslash = message.data.upforceslash;
                  variables.wp = message.data.wp;
                  variables.rangerleapnum = message.data.rangerleapnum;
                  variables.rangerleaptimer = message.data.rangerleaptimer;
                  variables.sl_EVA = message.data.sl_EVA;
                  variables.randplayerx = message.data.randplayerx;
                  variables.randplayery = message.data.randplayery;
                  variables.randplayerz = message.data.randplayerz;
                  variables.traintype = message.data.traintype;
                  variables.isdailytraining = message.data.isdailytraining;
                  variables.shopitem1 = message.data.shopitem1;
                  variables.shopitem2 = message.data.shopitem2;
                  variables.shopitem3 = message.data.shopitem3;
                  variables.shopitem4 = message.data.shopitem4;
                  variables.shopitem6 = message.data.shopitem6;
                  variables.Ab1 = message.data.Ab1;
                  variables.Ab2 = message.data.Ab2;
                  variables.Ab3 = message.data.Ab3;
                  variables.Ab4 = message.data.Ab4;
                  variables.abilities = message.data.abilities;
                  variables.ActiveDaily = message.data.ActiveDaily;
                  variables.alivestatus = message.data.alivestatus;
                  variables.beru = message.data.beru;
                  variables.berumax = message.data.berumax;
                  variables.boss = message.data.boss;
                  variables.BossKilled = message.data.BossKilled;
                  variables.Call4Death = message.data.Call4Death;
                  variables.Classes = message.data.Classes;
                  variables.mageSpecialization = message.data.mageSpecialization;
                  variables.combatmode = message.data.combatmode;
                  variables.commanddeath = message.data.commanddeath;
                  variables.dailykilltyppe = message.data.dailykilltyppe;
                  variables.dailysecrettrans = message.data.dailysecrettrans;
                  variables.dailytasks = message.data.dailytasks;
                  variables.dailytimer = message.data.dailytimer;
                  variables.dailyQuestSchema = message.data.dailyQuestSchema;
                  variables.dailyMinedBlocks = message.data.dailyMinedBlocks;
                  variables.dailyThreatPoints = message.data.dailyThreatPoints;
                  variables.lastDailyQuestDay = message.data.lastDailyQuestDay;
                  variables.dailyCombatWaived = message.data.dailyCombatWaived;
                  variables.DeathX = message.data.DeathX;
                  variables.DeathY = message.data.DeathY;
                  variables.DeathZ = message.data.DeathZ;
                  variables.Dialogue = message.data.Dialogue;
                  variables.domain = message.data.domain;
                  variables.dungeoning = message.data.dungeoning;
                  variables.DungeonNum = message.data.DungeonNum;
                  variables.DunX = message.data.DunX;
                  variables.DunY = message.data.DunY;
                  variables.DunZ = message.data.DunZ;
                  variables.Durability = message.data.Durability;
                  variables.giftstatus = message.data.giftstatus;
                  variables.GobShadow = message.data.GobShadow;
                  variables.GobShadowMax = message.data.GobShadowMax;
                  variables.golds = message.data.golds;
                  variables.guardbar = message.data.guardbar;
                  variables.GuildCode = message.data.GuildCode;
                  variables.HunterEyes = message.data.HunterEyes;
                  variables.HunterRank = message.data.HunterRank;
                  variables.igris = message.data.igris;
                  variables.IgrisSpawned = message.data.IgrisSpawned;
                  variables.Intelligence = message.data.Intelligence;
                  variables.investvalue = message.data.investvalue;
                  variables.JOB = message.data.JOB;
                  variables.vesselType = message.data.vesselType;
                  variables.vesselIdentity = message.data.vesselIdentity;
                  variables.vesselGrantedAuthority = message.data.vesselGrantedAuthority;
                  variables.jobkey = message.data.jobkey;
                  variables.killmission = message.data.killmission;
                  variables.LastKilled = message.data.LastKilled;
                  variables.Level = message.data.Level;
                  variables.MainQuest = message.data.MainQuest;
                  variables.manaregen = message.data.manaregen;
                  variables.MaxXP = message.data.MaxXP;
                  variables.orcmax = message.data.orcmax;
                  variables.orcspawned = message.data.orcspawned;
                  variables.OrdShadow = message.data.OrdShadow;
                  variables.ordshadowmax = message.data.ordshadowmax;
                  variables.overridefeet = message.data.overridefeet;
                  variables.overridehead = message.data.overridehead;
                  variables.overridelegs = message.data.overridelegs;
                  variables.overridetorso = message.data.overridetorso;
                  variables.perception = message.data.perception;
                  variables.Player = message.data.Player;
                  variables.polarbear = message.data.polarbear;
                  variables.polarbearmax = message.data.polarbearmax;
                  variables.pushup = message.data.pushup;
                  variables.ranking = message.data.ranking;
                  variables.rankingnum = message.data.rankingnum;
                  variables.resistance = message.data.resistance;
                  variables.RUN = message.data.RUN;
                  variables.RX = message.data.RX;
                  variables.RZ = message.data.RZ;
                  variables.ShadowExchange = message.data.ShadowExchange;
                  variables.ShadowSelect = message.data.ShadowSelect;
                  variables.shadowstorage = message.data.shadowstorage;
                  variables.shadowstorageusage = message.data.shadowstorageusage;
                  variables.situp = message.data.situp;
                  variables.SkillPoints = message.data.SkillPoints;
                  variables.slashfury = message.data.slashfury;
                  variables.Speed = message.data.Speed;
                  variables.speedpercent = message.data.speedpercent;
                  variables.squat = message.data.squat;
                  variables.statshown = message.data.statshown;
                  variables.Strength = message.data.Strength;
                  variables.summonlimit = message.data.summonlimit;
                  variables.summonlimitusage = message.data.summonlimitusage;
                  variables.tj = message.data.tj;
                  variables.tjonoff = message.data.tjonoff;
                  variables.Vitality = message.data.Vitality;
                  variables.WolfShadow = message.data.WolfShadow;
                  variables.WolfShadowMax = message.data.WolfShadowMax;
                  variables.Xp = message.data.Xp;
                  variables.xpmultiplier = message.data.xpmultiplier;
                  variables.Money = message.data.Money;
                  variables.CustomHUD = message.data.CustomHUD;
                  variables.pvpUrgentQuests = message.data.pvpUrgentQuests;
                  variables.ShadowGoblinArcherAmount = message.data.ShadowGoblinArcherAmount;
                  variables.ShadowGoblinMageAmount = message.data.ShadowGoblinMageAmount;
                  variables.ShadowGoblinArcherMax = message.data.ShadowGoblinArcherMax;
                  variables.ShadowGoblinMageMax = message.data.ShadowGoblinMageMax;
                  variables.shadowdragonnum = message.data.shadowdragonnum;
                  variables.shadowdragonmax = message.data.shadowdragonmax;
                  variables.packetCounter = message.data.packetCounter;
                  variables.instance_query_timer = message.data.instance_query_timer;
                  variables.daily_refreshes = message.data.daily_refreshes;
                  variables.selection = message.data.selection;
                  variables.party = message.data.party;
                  variables.monarchbeam = message.data.monarchbeam;
                  variables.baranlightningstrike = message.data.baranlightningstrike;
                  variables.prevRank = message.data.prevRank;
                  variables.prevLevel = message.data.prevLevel;
                  variables.idcd = message.data.idcd;
                  variables.title = message.data.title;
                  variables.unlockedTitles = message.data.unlockedTitles;
                  variables.wolfAssassinKills = message.data.wolfAssassinKills;
                  variables.Plist = message.data.Plist;
                  variables.Pslot1 = message.data.Pslot1;
                  variables.Pslot2 = message.data.Pslot2;
                  variables.Pslot3 = message.data.Pslot3;
                  variables.Pslot4 = message.data.Pslot4;
                  variables.Pslot5 = message.data.Pslot5;
                  variables.Pslot6 = message.data.Pslot6;
                  variables.Pslot7 = message.data.Pslot7;
                  variables.Pslot8 = message.data.Pslot8;
                  variables.Pslot9 = message.data.Pslot9;
                  variables.Pslot10 = message.data.Pslot10;
                  variables.Pslot11 = message.data.Pslot11;
                  variables.Pslot12 = message.data.Pslot12;
                  variables.Pslot13 = message.data.Pslot13;
                  variables.Pslot14 = message.data.Pslot14;
                  variables.Pslot15 = message.data.Pslot15;
                  variables.Pslot16 = message.data.Pslot16;
                  variables.PskillPage = message.data.PskillPage;
                  variables.PslotSelecting = message.data.PslotSelecting;
                  variables.PselectedPower = message.data.PselectedPower;
                  variables.progression_assassin = message.data.progression_assassin;
                  variables.progression_mage = message.data.progression_mage;
                  variables.progression_fighter = message.data.progression_fighter;
                  variables.progression_tanker = message.data.progression_tanker;
                  variables.progression_healer = message.data.progression_healer;
                  variables.progression_ranger = message.data.progression_ranger;
                  variables.JobSkills = message.data.JobSkills;
                  variables.ExchangeDimensions = message.data.ExchangeDimensions;
                  variables.ExchangeCords = message.data.ExchangeCords;
                  variables.ShadowBody = message.data.ShadowBody;
                  variables.progression_multiplier_assassin = message.data.progression_multiplier_assassin;
                  variables.progression_multiplier_mage = message.data.progression_multiplier_mage;
                  variables.progression_multiplier_fighter = message.data.progression_multiplier_fighter;
                  variables.progression_multiplier_tanker = message.data.progression_multiplier_tanker;
                  variables.progression_multiplier_healer = message.data.progression_multiplier_healer;
                  variables.progression_multiplier_ranger = message.data.progression_multiplier_ranger;
                  variables.overlay_alpha_welcome = message.data.overlay_alpha_welcome;
                  variables.progression_multiplier_dagger = message.data.progression_multiplier_dagger;
                  variables.progression_dagger = message.data.progression_dagger;
                  variables.overlay_alpha_dailyquestwarning = message.data.overlay_alpha_dailyquestwarning;
                  variables.dkc_unlocked = message.data.dkc_unlocked;
                  variables.unlocked_quests = message.data.unlocked_quests;
                  variables.finished_quests = message.data.finished_quests;
                  variables.highorcmax = message.data.highorcmax;
                  variables.highorcspawned = message.data.highorcspawned;
                  variables.tuskmax = message.data.tuskmax;
                  variables.tuskspawned = message.data.tuskspawned;
                  variables.Kaisel = message.data.Kaisel;
                  variables.KaiselSpawned = message.data.KaiselSpawned;
                  variables.FireRingTimer = message.data.FireRingTimer;
                  variables.reward_1 = message.data.reward_1;
                  variables.reward_2 = message.data.reward_2;
                  variables.reward_3 = message.data.reward_3;
                  variables.reward_extra = message.data.reward_extra;
                  variables.dkc_cleared = message.data.dkc_cleared;
                  variables.dkc_x = message.data.dkc_x;
                  variables.dkc_y = message.data.dkc_y;
                  variables.dkc_z = message.data.dkc_z;
                  variables.dkc_started = message.data.dkc_started;
                  variables.radiru_pact = message.data.radiru_pact;
                  variables.radiru_slaughtered = message.data.radiru_slaughtered;
                  variables.radiru_side_quest_unlocked = message.data.radiru_side_quest_unlocked;
                  variables.cooldownData = message.data.cooldownData;
               }
            }
         );
         context.setPacketHandled(true);
      }
   }

   public static class SavedDataSyncMessage {
      private final int type;
      private SavedData data;

      public SavedDataSyncMessage(FriendlyByteBuf buffer) {
         this.type = buffer.readInt();
         CompoundTag nbt = buffer.readNbt();
         if (nbt != null) {
            this.data = this.type == 0 ? new SololevelingModVariables.MapVariables() : new SololevelingModVariables.WorldVariables();
            if (this.data instanceof SololevelingModVariables.MapVariables mapVariables) {
               mapVariables.read(nbt);
            } else if (this.data instanceof SololevelingModVariables.WorldVariables worldVariables) {
               worldVariables.read(nbt);
            }
         }
      }

      public SavedDataSyncMessage(int type, SavedData data) {
         this.type = type;
         this.data = data;
      }

      public static void buffer(SololevelingModVariables.SavedDataSyncMessage message, FriendlyByteBuf buffer) {
         buffer.writeInt(message.type);
         if (message.data != null) {
            buffer.writeNbt(message.data.save(new CompoundTag()));
         }
      }

      public static void handler(SololevelingModVariables.SavedDataSyncMessage message, Supplier<Context> contextSupplier) {
         Context context = contextSupplier.get();
         context.enqueueWork(() -> {
            if (!context.getDirection().getReceptionSide().isServer() && message.data != null) {
               if (message.type == 0) {
                  SololevelingModVariables.MapVariables.clientSide = (SololevelingModVariables.MapVariables)message.data;
               } else {
                  SololevelingModVariables.WorldVariables.clientSide = (SololevelingModVariables.WorldVariables)message.data;
               }
            }
         });
         context.setPacketHandled(true);
      }
   }

   public static class WorldVariables extends SavedData {
      public static final String DATA_NAME = "sololeveling_worldvars";
      static SololevelingModVariables.WorldVariables clientSide = new SololevelingModVariables.WorldVariables();

      public static SololevelingModVariables.WorldVariables load(CompoundTag tag) {
         SololevelingModVariables.WorldVariables data = new SololevelingModVariables.WorldVariables();
         data.read(tag);
         return data;
      }

      public void read(CompoundTag nbt) {
      }

      @Override
      public CompoundTag save(CompoundTag nbt) {
         return nbt;
      }

      public void syncData(LevelAccessor world) {
         this.setDirty();
         if (world instanceof Level level && !level.isClientSide()) {
            SololevelingMod.PACKET_HANDLER.send(PacketDistributor.DIMENSION.with(level::dimension), new SololevelingModVariables.SavedDataSyncMessage(1, this));
         }
      }

      public static SololevelingModVariables.WorldVariables get(LevelAccessor world) {
         return world instanceof ServerLevel level
            ? level.getDataStorage().computeIfAbsent(e -> load(e), SololevelingModVariables.WorldVariables::new, "sololeveling_worldvars")
            : clientSide;
      }
   }
}
