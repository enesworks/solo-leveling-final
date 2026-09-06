package dev.eness.sololeveling3.vfx;

import dev.eness.sololeveling3.entity.AntaresEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Bounded, world-oriented ribbons; shared between runtime and mesh review tests. */
public final class AntaresVfxMesh {
    @FunctionalInterface public interface Sink {void quad(Vec3 a,Vec3 b,Vec3 c,Vec3 d,float r,float g,float blue,float alpha);}
    @FunctionalInterface public interface Socket {Vec3 at(String bone,double tick);}
    private static final Vec3 UP=new Vec3(0,1,0);
    public static void emit(int action,double tick,double counter,Vec3 forward,Vec3 aim,double range,Socket socket,Sink out){
        Vec3 side=new Vec3(forward.z,0,-forward.x);
        if(action==AntaresEntity.COMBO){
            // Anatomical sides: right descending claw, left ascending reverse.
            fingerClaws(tick,7.5,10.125,13.125,"right",socket,forward,out);
            fingerClaws(tick,17.625,20.25,22.125,"left",socket,forward,out);
            if(tick>=AntaresEntity.COMBO_PALM_HIT&&tick<AntaresEntity.COMBO_DURATION)
                palmImpact((tick-AntaresEntity.COMBO_PALM_HIT)/.375,socket.at("right_palm",tick),forward,aim,range,side,out);
        }
        if(action==AntaresEntity.SWEEP){
            claws(tick,13.8,18.6,"right_hand",socket,side,out);
            double age=tick-AntaresEntity.SWEEP_RELEASE;
            if(age>=0&&tick<=AntaresEntity.SWEEP_DURATION){double travel=Math.min(AntaresEntity.SWEEP_RANGE,age*(AntaresEntity.SWEEP_RANGE/AntaresEntity.SWEEP_TRAVEL_TICKS));float fade=(float)Math.min(1,(AntaresEntity.SWEEP_DURATION-tick)/3);
                for(int layer=0;layer<3;layer++){
                    final int l=layer;final double d=travel;
                    ribbon(u->{double x=(u-.5)*5.2;return side.scale(x).add(forward.scale(.8+d-.12*x*x)).add(0,1.8+Math.sin(u*Math.PI)*.04,0);},
                        UP,layer==0?.15:layer==1?.08:.025,32,layer==0?.055F:1,layer==2?.68F:.018F,layer==2?.73F:.06F,fade*(layer==0?.9F:.85F),out);
                }
            }
        }
        if(action==AntaresEntity.GUARD){
            float alpha=(float)(.28+.12*Math.sin(tick*.3));
            for(String bone:new String[]{"right_pauldron","left_pauldron","right_forearm","left_forearm"})
                ring(socket.at(bone,tick),forward,.23,.025,tick*.015,alpha,24,out);
            Vec3 center=socket.at("right_hand",tick).add(socket.at("left_hand",tick)).scale(.5);
            if(tick<18)ring(center,forward,.2+tick*.025,.025,0,(float)(.65*(1-tick/22)),32,out);
        }
        if(counter>0){Vec3 center=socket.at("right_hand",tick);shock(center,forward,counter-1,13,1.7,out);}
        if(action==AntaresEntity.RAY){
            Vec3 center=socket.at("right_hand",Math.min(tick,83)).add(socket.at("left_hand",Math.min(tick,83))).scale(.5);
            if(tick>=10&&tick<65){double charge=Math.min(1,(tick-10)/35),r=.12+.25*charge;
                for(int i=0;i<3;i++)ring(center,i==0?forward:i==1?UP:side,r*(1+i*.25),.018+i*.003,tick*.075+i*.7,.8F,32,out);
                for(int i=0;i<6;i++){double a=i*Math.PI/3+tick*.07;Vec3 tip=center.add(side.scale(Math.cos(a)*(.5+.3*charge))).add(0,Math.sin(a)*(.5+.3*charge),0);
                    line(tip,tip.lerp(center,.65),UP,.018,1,.04F,.12F,.65F,out);}
                glowOrb(center,r*.65,forward,side,out);
            }
            if(tick>=65&&tick<=83&&range>0){
                Vec3 end=center.add(aim.scale(range));Vec3 axis=aim.cross(UP).normalize();if(axis.lengthSqr()<.1)axis=side;
                Vec3 second=aim.cross(axis).normalize();
                for(int i=0;i<6;i++){double a=i*Math.PI/6;Vec3 width=axis.scale(Math.cos(a)).add(second.scale(Math.sin(a)));
                    line(center,end,width,.30,.035F,.003F,.012F,.65F,out);
                    line(center,end,width,.16,1,.015F,.05F,.85F,out);
                    line(center,end,width,.045,1,.8F,.83F,1,out);
                }
                ring(center,aim,.42,.06,tick*.1,.9F,32,out);
                final Vec3 ax=axis,sy=second;
                for(int i=0;i<2;i++){final double phase=tick*.4+i*Math.PI;
                    ribbon(u->center.add(aim.scale(range*u)).add(ax.scale(Math.cos(u*24+phase)*.23)).add(sy.scale(Math.sin(u*24+phase)*.23)),ax,.02,40,1,.06F,.15F,.65F,out);}
                glowOrb(end,.22,forward,side,out);
            }
            if(tick>=83&&tick<=90){Vec3 end=center.add(aim.scale(range));shock(end,aim,tick-83,7,2.5,out);}
        }
    }
    private static void fingerClaws(double tick,double start,double hit,double end,String side,Socket socket,Vec3 forward,Sink out){
        if(tick<=start||tick>=end)return;
        double tip=tick,tail=Math.max(start,tip-2.8125);
        float fade=(float)Math.min(Math.min(1,(tick-start)/.45),(end-tick)/1.3125);
        for(int finger=1;finger<=3;finger++){
            String name=side+"_claw_"+finger;
            for(int layer=0;layer<3;layer++){
                double width=layer==0?.041:layer==1?.021:.0055;
                for(int i=0;i<18;i++){
                    double u=i/18.,v=(i+1)/18.;
                    Vec3 a=socket.at(name,tail+(tip-tail)*u),b=socket.at(name,tail+(tip-tail)*v);
                    Vec3 normal=b.subtract(a).cross(forward).normalize();if(normal.lengthSqr()<.1)normal=UP;
                    Vec3 wa=normal.scale(width*(.15+.85*Math.sin(Math.PI*u))),wb=normal.scale(width*(.15+.85*Math.sin(Math.PI*v)));
                    out.quad(a.subtract(wa),b.subtract(wb),b.add(wb),a.add(wa),1,layer==2?.78F:.015F,layer==2?.84F:.07F,fade*(layer==0?.26F:.94F));
                }
            }
            // Each bright tip sits on its own actual finger, including spreading.
            Vec3 head=socket.at(name,tip);
            ring(head,forward,.032,.009,0,fade,12,out);
        }
    }
    private static void palmImpact(double age,Vec3 palm,Vec3 forward,Vec3 aim,double range,Vec3 side,Sink out){
        double u=Mth.clamp(age/8,0,1);float fade=(float)(1-u);
        Vec3 direction=aim.lengthSqr()>.01?aim.normalize():forward;
        Vec3 center=palm.add(direction.scale(.015));
        Vec3 widthAxis=direction.cross(UP).normalize();if(widthAxis.lengthSqr()<.1)widthAxis=side;
        Vec3 heightAxis=direction.cross(widthAxis).normalize();
        ring(center,direction,.30+.65*u,.025,age*.03,fade,40,out);
        ring(center,direction,.24+.45*u,.010,-age*.04,fade*.7F,32,out);
        if(age<5){
            // The impulse reaches the locked target immediately at the damage
            // tick and tracks its position while the palm remains extended.
            Vec3 tip=center.add(direction.scale(Math.max(0,range-.015)));
            float flash=(float)(1-age/5);
            for(Vec3 width:new Vec3[]{heightAxis,widthAxis}){
                line(center,tip,width,.09,1,.01F,.055F,flash*.30F,out);
                line(center,tip,width,.038,1,.035F,.09F,flash*.95F,out);
                line(center,tip,width,.010,1,.82F,.86F,flash,out);
            }
            ring(tip,direction,.09+.16*age/5,.012,0,flash,24,out);
        }
        for(int i=0;i<10;i++){
            double a=i*Math.PI/5+.15;Vec3 radial=widthAxis.scale(Math.cos(a)).add(heightAxis.scale(Math.sin(a)));
            Vec3 from=center.add(radial.scale(.34+.6*u));
            line(from,from.add(radial.scale(.09+.20*u)),radial.cross(direction),.007,1,.11F,.19F,fade,out);
        }
        // Secondary foot-level accent; the dominant impact stays on the palm.
        shock(forward.scale(1.4).add(0,.06,0),UP,age,8,1.5,
            (a,b,c,d,r,g,blue,alpha)->out.quad(a,b,c,d,r,g,blue,alpha*.38F));
    }
    private static void claws(double tick,double start,double end,String hand,Socket socket,Vec3 side,Sink out){
        if(tick<start||tick>end)return;double tip=Math.min(tick,end-2),tail=Math.max(start,tip-4.5);float fade=(float)Math.min(1,(end-tick)/2);
        for(int finger=-1;finger<=1;finger++){
            Vec3 offset=UP.scale(finger*.12).add(side.scale(finger*.035));
            for(int layer=0;layer<3;layer++)ribbon(u->socket.at(hand,tail+(tip-tail)*u).add(offset),UP,
                layer==0?.075:layer==1?.044:.012,12,layer==0?.04F:1,layer==2?.64F:.015F,layer==2?.72F:.06F,fade*.85F,out);
        }
    }
    private static void glowOrb(Vec3 at,double radius,Vec3 f,Vec3 s,Sink out){
        for(Vec3 normal:new Vec3[]{f,s,UP}){ring(at,normal,radius,.09,0,.8F,24,out);ring(at,normal,radius*.55,.07,0,1,24,out);}
    }
    private static void shock(Vec3 at,Vec3 normal,double age,double duration,double max,Sink out){
        double u=Mth.clamp(age/duration,0,1),r=.15+max*(1-Math.pow(1-u,2));
        ring(at,normal,r,.08*(1-u)+.01,0,(float)(1-u),40,out);
        ring(at,normal,r*.74,.025,0,(float)((1-u)*.65),32,out);
    }
    private static void ring(Vec3 center,Vec3 normal,double radius,double width,double phase,float alpha,int count,Sink out){
        Vec3 a=normal.cross(UP).normalize();if(a.lengthSqr()<.1)a=new Vec3(1,0,0);Vec3 b=normal.cross(a).normalize();
        for(int i=0;i<count;i++){double x=i*Math.PI*2/count+phase,y=(i+1)*Math.PI*2/count+phase;
            Vec3 n1=a.scale(Math.cos(x)).add(b.scale(Math.sin(x))),n2=a.scale(Math.cos(y)).add(b.scale(Math.sin(y)));
            out.quad(center.add(n1.scale(radius-width)),center.add(n2.scale(radius-width)),center.add(n2.scale(radius+width)),center.add(n1.scale(radius+width)),1,.025F,.08F,Mth.clamp(alpha,0,1));}
    }
    @FunctionalInterface private interface Curve{Vec3 at(double u);}
    private static void ribbon(Curve curve,Vec3 width,double thickness,int count,float r,float g,float b,float alpha,Sink out){
        for(int i=0;i<count;i++){double u=i/(double)count,v=(i+1.)/count;Vec3 a=curve.at(u),z=curve.at(v);
            Vec3 w1=width.scale(thickness*Math.sin(Math.PI*u)),w2=width.scale(thickness*Math.sin(Math.PI*v));
            out.quad(a.subtract(w1),z.subtract(w2),z.add(w2),a.add(w1),r,g,b,alpha);}
    }
    private static void line(Vec3 a,Vec3 b,Vec3 normal,double width,float r,float g,float blue,float alpha,Sink out){Vec3 w=normal.scale(width);out.quad(a.subtract(w),b.subtract(w),b.add(w),a.add(w),r,g,blue,alpha);}
    private AntaresVfxMesh(){}
}
