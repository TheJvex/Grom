package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;

@CheckData(name = "Scaffold (Ratio)", setback = 0, description = "SUS Block Placement(Kauri Inspiration)")
public class ScaffoldRatio extends BlockPlaceCheck {

    public ScaffoldRatio(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (player.gamemode == GameMode.CREATIVE
                || place.material == StateTypes.SCAFFOLDING
                || player.inVehicle()) return;


        Vector3i pos = place.position;
        Vector3d blockLoc = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);


        int modX = place.getFace().getModX();
        int modY = place.getFace().getModY();
        int modZ = place.getFace().getModZ();
        Vector3d againstLoc = new Vector3d(
                pos.getX() - modX + 0.5,
                pos.getY() - modY + 0.5,
                pos.getZ() - modZ + 0.5
        );

        Vector3d playerLoc = new Vector3d(player.x, player.y, player.z);

        double yDiff = blockLoc.getY() - playerLoc.getY();
        double distancePlayerBlock = playerLoc.distance(blockLoc);
        double distancePlayerAgainst = playerLoc.distance(againstLoc) + 0.4;


        if (distancePlayerBlock >= 1.3
                && distancePlayerBlock > distancePlayerAgainst
                && yDiff <= 0.5) {


            if (!player.packetStateData.lastPacketWasTeleport) {
                flagAndAlert(String.format("d=%.2f ad=%.2f y=%.1f", distancePlayerBlock, distancePlayerAgainst, yDiff));
            }
        }
    }
}
