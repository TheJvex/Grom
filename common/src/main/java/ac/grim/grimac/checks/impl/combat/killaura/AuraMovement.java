package ac.grim.grimac.checks.impl.combat.killaura;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "Aura (Movement)", setback = 0, description = "Detects linear stability (FlopAC Inspiration)")
public class AuraMovement extends Check implements PacketCheck {
    private double lastYawDiff, lastDeltaXZ, buffer, lastAttackTime;

    public AuraMovement(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isUpdate(event.getPacketType())) {
            if (System.currentTimeMillis() - lastAttackTime > 50) buffer = Math.max(0, buffer - 0.5);
        }

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

            lastAttackTime = System.currentTimeMillis();
            double currentYawDiff = player.yaw - player.lastYaw;
            double yawAccel = Math.abs(currentYawDiff - this.lastYawDiff);
            double currentDeltaXZ = Math.hypot(player.x - player.lastX, player.z - player.lastZ);
            double speedAccel = Math.abs(currentDeltaXZ - lastDeltaXZ);

            if (speedAccel > 0 && speedAccel < 0.008 && yawAccel > 7.0) {
                if (++buffer > 6.0) {
                    flagAndAlert(String.format("accel=%.5f", speedAccel));
                    buffer = 4.0;
                }
            } else {
                buffer = Math.max(0, buffer - 0.25);
            }
            this.lastDeltaXZ = currentDeltaXZ;
            this.lastYawDiff = currentYawDiff;
        }
    }
}
