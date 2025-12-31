package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.processor.AimProcessor;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "Aim (Snap)", setback = 0, description = "AimSnap(Kauri Inspiration)")
public class AimSnap extends Check implements RotationCheck {

    private float buffer;
    private float lastDeltaYaw;

    public AimSnap(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (rotationUpdate.getDeltaXRot() == 0 && rotationUpdate.getDeltaYRot() == 0) return;

        float toYaw = player.yaw;
        float deltaYaw = Math.abs(rotationUpdate.getDeltaXRot());

        AimProcessor processor = player.checkManager.getRotationCheck(AimProcessor.class);
        if (processor == null) return;



        if (deltaYaw > 320
                && lastDeltaYaw > 0
                && lastDeltaYaw < 30
                && toYaw < 360 && toYaw > -360
                && !player.packetStateData.lastPacketWasTeleport
                && processor.sensitivityX < 0.65) {


            if (++buffer > 1) {
                flagAndAlert(String.format("yaw=%.3f", deltaYaw));
            }
        } else if (buffer > 0) {
            buffer -= 0.005f;
        }

        lastDeltaYaw = deltaYaw;
    }
}
