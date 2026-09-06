package dev.eness.sololevelingfinal.core.client;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import dev.eness.sololevelingfinal.core.entity.AntaresEntity;
import dev.eness.sololevelingfinal.core.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Opt-in development harness. Runs only in an explicitly named disposable copy. */
public final class AntaresVisualReview {
    private static boolean setup,farCamera;
    private static volatile boolean starting;
    private static int stage,settle=40,exitDelay;
    private static volatile UUID bossId;
    private static AntaresEntity serverBoss;
    private static IronGolem target;
    private static final Set<String> CAPTURED=new HashSet<>();
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("sl3.antares.visualReview"))return;
        var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();
        if(server==null||mc.level==null||mc.player==null)return;
        if(!server.getWorldPath(LevelResource.ROOT).toAbsolutePath().toString().contains("Antares-Visual-Review"))return;
        mc.options.hideGui=true;
        if(!setup){setup=true;starting=true;server.execute(()->{
            var level=server.overworld();for(var e:level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new AABB(-15,0,0,50,30,45)))if(!(e instanceof net.minecraft.world.entity.player.Player))e.discard();
            for(int x=-15;x<=40;x++)for(int z=0;z<=45;z++){level.setBlockAndUpdate(new BlockPos(x,0,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=1;y<=8;y++)level.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}
            level.setDayTime(6000);level.setWeatherParameters(0,6000,false,false);
            serverBoss=ModEntities.ANTARES.get().create(level);serverBoss.moveTo(15,1,12,0,0);serverBoss.setNoAi(true);serverBoss.setInvulnerable(false);level.addFreshEntity(serverBoss);bossId=serverBoss.getUUID();
            target=EntityType.IRON_GOLEM.create(level);target.moveTo(15,1,14.8,0,0);target.setNoAi(true);target.setInvulnerable(true);level.addFreshEntity(target);
            ServerPlayer p=server.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.SPECTATOR);camera(p,false);starting=false;
        });return;}
        if(starting||bossId==null)return;
        if(settle>0){settle--;return;}
        AntaresEntity clientBoss=null;for(var e:mc.level.entitiesForRendering())if(e instanceof AntaresEntity a&&a.getUUID().equals(bossId)){clientBoss=a;break;}
        if(clientBoss==null)return;
        if(stage==0){capture(mc,"00_idle");start(server,AntaresEntity.COMBO);stage=1;return;}
        int age=clientBoss.getActionTick();
        if(stage==1){if(age>=10&&age<=12)capture(mc,"01_combo_right");if(age>=20&&age<=22)capture(mc,"02_combo_left");if(age>=27&&age<=29)capture(mc,"03_combo_palm");
            if(clientBoss.getCombatAction()==0&&CAPTURED.contains("03_combo_palm")){start(server,AntaresEntity.SWEEP);stage=2;}}
        else if(stage==2){if(age>=16&&age<=18)capture(mc,"04_sweep_release");if(age>=23&&age<=25)capture(mc,"05_sweep_travel");
            if(clientBoss.getCombatAction()==0&&CAPTURED.contains("05_sweep_travel")){start(server,AntaresEntity.GUARD);stage=3;}}
        else if(stage==3){if(age>=22&&age<=24)capture(mc,"06_guard");if(age>=30&&!CAPTURED.contains("counter-trigger")){CAPTURED.add("counter-trigger");server.execute(()->{serverBoss.invulnerableTime=0;serverBoss.hurt(serverBoss.damageSources().mobAttack(target),10);});}
            if(clientBoss.getCounterTick()>=4&&clientBoss.getCounterTick()<=6)capture(mc,"07_guard_counter");
            if(clientBoss.getCombatAction()==0&&age==0&&CAPTURED.contains("06_guard")){start(server,AntaresEntity.RAY);stage=4;}}
        else if(stage==4){if(age>=33&&age<=35)capture(mc,"08_ray_charge");
            if(age>=48&&!farCamera){farCamera=true;server.execute(()->camera(server.getPlayerList().getPlayers().get(0),true));}
            if(age>=70&&age<=72)capture(mc,"09_ray_fire");if(age>=84&&age<=86)capture(mc,"10_ray_burst");
            if(clientBoss.getCombatAction()==0&&CAPTURED.contains("10_ray_burst")){stage=5;exitDelay=40;}}
        else if(stage==5&&--exitDelay<=0){SoloLeveling3.LOGGER.info("ANTARES_VISUAL_REVIEW_COMPLETE {}",CAPTURED);mc.stop();}
    }
    private static void start(net.minecraft.server.MinecraftServer server,int action){starting=true;server.execute(()->{
        target.teleportTo(15,1,action==AntaresEntity.COMBO||action==AntaresEntity.GUARD?14.8:23);
        serverBoss.previewAbility(action,target);starting=false;
    });settle=3;}
    private static void camera(ServerPlayer p,boolean far){p.teleportTo(p.serverLevel(),far?-10:7,far?5:4,far?24:19,far?-90:-132,far?5:9);}
    private static void capture(Minecraft mc,String name){if(!CAPTURED.add(name))return;
        File folder=new File("../Antares-Animated-Combat/client-review");folder.mkdirs();
        Screenshot.grab(folder,name+".png",mc.getMainRenderTarget(),message->SoloLeveling3.LOGGER.info("Antares screenshot {}: {}",name,message.getString()));}
    private AntaresVisualReview(){}
}
