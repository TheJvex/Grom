package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import com.github.retrooper.packetevents.protocol.world.BlockFace;

@CheckData(name = "Scaffold (Direction)", setback = 0, description = "Impossible Placement (Truthful Inspiration)")
public class ScaffoldDirection extends BlockPlaceCheck {
    private double buffer;

    public ScaffoldDirection(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (!player.isSprinting || player.inVehicle()) {
            buffer = Math.max(0, buffer - 0.25);
            return;
        }
        BlockFace face = place.getFace();
        if (face == null || face == BlockFace.UP || face == BlockFace.DOWN) return;

        double vx = player.x - player.lastX;
        double vz = player.z - player.lastZ;
        double dot = (vx * face.getModX()) + (vz * face.getModZ());

        if (dot > 0.15) {
            if (++buffer > 5.0) flagAndAlert(String.format("dot=%.3f", dot));
        } else {
            buffer = Math.max(0, buffer - 0.25);
        }
    }
}
