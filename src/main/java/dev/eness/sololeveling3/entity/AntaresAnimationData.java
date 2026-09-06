package dev.eness.sololeveling3.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

/** Offline-baked hand/armor transforms from the same keys as the GeckoLib clips. */
public final class AntaresAnimationData {
    private static final Map<String, Map<String, Vec3[]>> TRACKS = new HashMap<>();
    private static final Map<String, double[]> TIMES = new HashMap<>();
    static {
        try (var stream = AntaresAnimationData.class.getResourceAsStream("/assets/sololeveling3/combat/antares_sockets.json")) {
            if (stream == null) throw new IllegalStateException("Missing Antares socket tracks");
            var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (var entry : json.entrySet()) {
                JsonArray rows = entry.getValue().getAsJsonArray();
                Map<String, Vec3[]> tracks = new HashMap<>(); double[] times = new double[rows.size()];
                for (int i=0;i<rows.size();i++) {
                    JsonObject row=rows.get(i).getAsJsonObject(); times[i]=row.get("time").getAsDouble();
                    for (var field:row.entrySet()) {
                        if (field.getKey().equals("time")) continue;
                        var v=field.getValue().getAsJsonArray();
                        tracks.computeIfAbsent(field.getKey(), k->new Vec3[rows.size()])[i]=
                                new Vec3(v.get(0).getAsDouble(),v.get(1).getAsDouble(),v.get(2).getAsDouble());
                    }
                }
                TRACKS.put(entry.getKey(),tracks);TIMES.put(entry.getKey(),times);
            }
        } catch (java.io.IOException e) { throw new ExceptionInInitializerError(e); }
    }
    public static Vec3 socket(String clip,String bone,double seconds) {
        var tracks=TRACKS.get(clip);if(tracks==null) throw new IllegalArgumentException(clip);
        var points=tracks.get(bone);var times=TIMES.get(clip);
        if(points==null)throw new IllegalArgumentException("Missing Antares socket: "+bone);
        if(seconds<=0)return points[0];
        int i=Arrays.binarySearch(times,seconds);if(i>=0)return points[i];i=-i-1;
        if(i>=times.length)return points[points.length-1];
        return points[i-1].lerp(points[i],Mth.clamp((seconds-times[i-1])/(times[i]-times[i-1]),0,1));
    }
    private AntaresAnimationData() {}
}
