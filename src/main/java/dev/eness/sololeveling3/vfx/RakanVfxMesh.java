package dev.eness.sololeveling3.vfx;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Shared numeric VFX geometry, also exercised by the headless GameTests. */
public final class RakanVfxMesh {
    public enum Style { CLAW_RIGHT, CLAW_LEFT, BLOCK, COUNTER, SLAM, ROAR }
    private static final Vec3 UP = new Vec3(0,1,0);
    public interface Sink {
        void quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, float r, float g, float blue, float alpha);
    }
    public static int duration(Style style) {
        return switch(style) { case ROAR -> 28; case SLAM -> 16; case BLOCK -> 9; default -> 8; };
    }
    public static void emit(Style style, double elapsed, Vec3 center, Vec3 forward, Sink out) {
        int lifetime=duration(style);
        Vec3 right=new Vec3(forward.z,0,-forward.x);
        double t = Mth.clamp(elapsed / lifetime, 0, 1);
        float fade = (float)((1 - t) * (1 - t));
        switch (style) {
            case CLAW_RIGHT, CLAW_LEFT, COUNTER -> {
                double sign = style == Style.CLAW_LEFT ? -1 : 1;
                Vec3 u = right.scale(sign);
                Vec3 v = forward.scale(.75).add(UP.scale(.66 * sign));
                double sweep = Math.min(1, elapsed / 2.5);
                double end = -.65 + 3.45 * sweep;
                double start = Math.max(-.65, end - 2.7);
                // Three separate curved cuts, each with a dark edge and gold core.
                // Their spacing is wider than both edges, so they never form a band.
                for (int claw = 0; claw < 3; claw++) {
                    double radius = (style == Style.COUNTER ? .65 : .9) + claw * .32 + t * .25;
                    ribbon(out, center, u, v, radius, .072, start, end, .018F, .045F, .12F, fade * .9F, true, 32);
                    ribbon(out, center, u, v, radius, .027, start, end, 1F, .78F, .16F, fade, true, 32);
                }
            }
            case BLOCK -> {
                double radius = .43 + .5 * Math.sqrt(t);
                ribbon(out, center, right, UP, radius, .035 * (1-t), 0, Math.PI*2, .7F, .76F, .84F, fade, false);
                ribbon(out, center, right, UP, radius*.83, .018, 0, Math.PI*2, .95F, .97F, 1F, fade, false);
                for (int i=0; i<12; i++) {
                    double a=i*Math.PI/6;
                    Vec3 axis=right.scale(Math.cos(a)).add(UP.scale(Math.sin(a)));
                    Vec3 tangent=right.scale(-Math.sin(a)).add(UP.scale(Math.cos(a)));
                    out.quad( center.add(axis.scale(radius*.8)), center.add(axis.scale(radius*1.5)).add(tangent.scale(.012)),
                            center.add(axis.scale(radius*1.5)).add(tangent.scale(-.012)), center.add(axis.scale(radius*.8)),
                            .8F,.85F,.9F,fade*.7F);
                }
            }
            case SLAM -> {
                double radius=.25+3.75*(1-Math.pow(1-t,2));
                ribbon(out, center, right, forward, radius, .07*(1-t)+.018, 0, Math.PI*2, .48F,.5F,.54F,fade*.85F,false);
                ribbon(out, center.add(0,.04,0), right, forward, radius*.92, .032, 0, Math.PI*2, 1F,.70F,.08F,fade*.6F,false);
                ribbon(out, center.add(0,.08,0), right, forward, radius*.7, .10, 0, Math.PI*2, .025F,.045F,.09F,fade*.8F,false);
                // Eight tumbling fragments, rendered locally; no terrain entities.
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4 + .17;
                    double distance = .26 + t * (1.45 + (i % 3) * .22);
                    double halfSize = .09 + (i % 3) * .025;
                    Vec3 fragment = center.add(Math.cos(angle) * distance,
                            halfSize + Math.sin(t * Math.PI) * (.65 + (i % 3) * .16), Math.sin(angle) * distance);
                    rubble(out, fragment, halfSize, angle + t * 5, t * 7 + i, (float)(1 - t));
                }
            }
            case ROAR -> {
                // Exactly three expanding pressure fronts, each with its own onset.
                for (int i=0; i<3; i++) {
                    double life=(elapsed-i*3)/19.;
                    if (life<0 || life>=1) continue;
                    double radius=.5+7.5*life;
                    float a=(float)(.44*(1-life));
                    Vec3 v=UP.scale(.94).add(forward.scale(.34));
                    ribbon(out,center.add(forward.scale(i*.12)),right,v,radius,.012+.018*(1-life),0,Math.PI*2,
                            .6F,.65F,.75F,a,false);
                }
            }
        }
    }

    private static void ribbon(Sink out, Vec3 center, Vec3 u, Vec3 v, double radius, double width,
                        double start, double end, float r, float g, float b, float alpha, boolean taper) {
        ribbon(out, center, u, v, radius, width, start, end, r, g, b, alpha, taper, 48);
    }

    private static void ribbon(Sink out, Vec3 center, Vec3 u, Vec3 v, double radius, double width,
                        double start, double end, float r, float g, float b, float alpha, boolean taper, int steps) {
        for (int i=0; i<steps; i++) {
            double p=i/(double)steps, q=(i+1)/(double)steps;
            double a=Mth.lerp(p,start,end), c=Mth.lerp(q,start,end);
            double wa=width*(taper ? Math.pow(Math.sin(p*Math.PI),.65) : 1);
            double wb=width*(taper ? Math.pow(Math.sin(q*Math.PI),.65) : 1);
            Vec3 axisA=u.scale(Math.cos(a)).add(v.scale(Math.sin(a)));
            Vec3 axisB=u.scale(Math.cos(c)).add(v.scale(Math.sin(c)));
            out.quad(center.add(axisA.scale(radius-wa)),center.add(axisA.scale(radius+wa)),
                    center.add(axisB.scale(radius+wb)),center.add(axisB.scale(radius-wb)),r,g,b,alpha);
        }
    }

    private static void rubble(Sink out, Vec3 center, double size, double yaw, double roll, float alpha) {
        Vec3[] corners = new Vec3[8];
        for (int i = 0; i < 8; i++) {
            Vec3 local = new Vec3((i & 1) == 0 ? -size : size,
                    (i & 2) == 0 ? -size * .7 : size * .7, (i & 4) == 0 ? -size : size);
            corners[i] = center.add(local.zRot((float)roll).yRot((float)yaw));
        }
        int[][] faces = {{0,1,3,2},{4,6,7,5},{0,4,5,1},{2,3,7,6},{0,2,6,4},{1,5,7,3}};
        for (int i = 0; i < faces.length; i++) {
            int[] face = faces[i];
            float shade = .30F + (i % 3) * .075F;
            out.quad(corners[face[0]], corners[face[1]], corners[face[2]], corners[face[3]],
                    shade, shade * .96F, shade * .9F, alpha);
        }
    }


}
