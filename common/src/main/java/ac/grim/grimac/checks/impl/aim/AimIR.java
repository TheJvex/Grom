package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "AimAssist (D)", setback = 0, description = "Identical Rotation (Artemis Inspiration)")
public class AimIR extends Check implements RotationCheck, PacketCheck {
    private float lastYawDelta;
    private double streak, lastAttack;

    public AimIR(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity p = new WrapperPlayClientInteractEntity(event);
            if (p.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) lastAttack = System.currentTimeMillis();
        }
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (System.currentTimeMillis() - lastAttack > 2000 || player.packetStateData.lastPacketWasTeleport) {
            streak = 0; return;
        }
        float yawDelta = rotationUpdate.getDeltaXRotABS();
        float pitchDelta = rotationUpdate.getDeltaYRotABS();

        if (yawDelta > 0.0f && pitchDelta > 0.0f) {
            int roundedYaw = Math.round(yawDelta);
            if (roundedYaw == Math.round(lastYawDelta) && yawDelta > 1.5f && Math.abs(yawDelta - lastYawDelta) > 0.001f && pitchDelta > 0.5f) {
                if (++streak > 6) flagAndAlert("streak=" + (int)streak);
            } else {
                streak = Math.max(0, streak - 0.25);
            }
        }
        this.lastYawDelta = yawDelta;
    }
}
