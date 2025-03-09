package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ParticleEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class POIHighlighter extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> groupRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("group-radius")
        .description("The maximum distance between two POIs to group them.")
        .defaultValue(4.0)
        .min(0.0)
        .sliderMax(10.0)
        .build()
    );

    private final Setting<Double> poiLifetime = sgGeneral.add(new DoubleSetting.Builder()
        .name("poi-lifetime")
        .description("The time in seconds a POI is visible.")
        .defaultValue(5.0)
        .min(0)
        .sliderMax(10.0)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the POI boxes.")
        .defaultValue(new SettingColor(0, 255, 0, 50))
        .build()
    );

    public POIHighlighter() {
        super(Categories.Helium, "poi-highlighter", "Highlights points of interest.");
    }

    private final List<POI> poiPoses = new ArrayList<>();

    @EventHandler
    private void onParticle(ParticleEvent e) {
        if (e.particle == ParticleTypes.COMPOSTER || e.particle == ParticleTypes.HAPPY_VILLAGER || e.particle == ParticleTypes.EGG_CRACK) {
            poiPoses.add(new POI(new Vec3d(e.x, e.y, e.z), System.currentTimeMillis()));
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent e) {
        long currentTime = System.currentTimeMillis();
        List<List<POI>> clusters = new ArrayList<>();

        // Remove expired POIs
        Iterator<POI> iterator = poiPoses.iterator();
        while (iterator.hasNext()) {
            if (currentTime - iterator.next().timestamp > (poiLifetime.get() * 1000)) {
                iterator.remove();
            }
        }

        // Group POIs into clusters
        for (POI poi : poiPoses) {
            boolean added = false;
            for (List<POI> cluster : clusters) {
                for (POI clusteredPoi : cluster) {
                    if (poi.pos.distanceTo(clusteredPoi.pos) <= groupRadius.get()) {
                        cluster.add(poi);
                        added = true;
                        break;
                    }
                }
                if (added) break;
            }
            if (!added) {
                List<POI> newCluster = new ArrayList<>();
                newCluster.add(poi);
                clusters.add(newCluster);
            }
        }

        // Render a box around each cluster
        for (List<POI> cluster : clusters) {
            if (cluster.isEmpty()) continue;

            // Calculate bounding box
            // Initialize bounding box variables correctly
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE; // FIXED

            for (POI poi : cluster) {
                minX = Math.min(minX, poi.pos.x);
                minY = Math.min(minY, poi.pos.y);
                minZ = Math.min(minZ, poi.pos.z);
                maxX = Math.max(maxX, poi.pos.x);
                maxY = Math.max(maxY, poi.pos.y);
                maxZ = Math.max(maxZ, poi.pos.z);
            }

            // Draw the bounding box around the cluster
            e.renderer.box(
                minX, minY, minZ,
                maxX, maxY, maxZ,
                color.get(), color.get(),
                ShapeMode.Sides, 0
            );
        }
    }

    public static class POI {
        public Vec3d pos;
        public long timestamp;

        public POI(Vec3d pos, long timestamp) {
            this.pos = pos;
            this.timestamp = timestamp;
        }
    }
}
