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

@CheckData(name = "AimConstant (A)", setback = 0, description = "Constant Speed (Rhys Inspiration)")
public class AimConstant extends Check implements RotationCheck, PacketCheck {
    private float lastDeltaYaw;
    private double buffer;
    private long lastAttackTime;

    public AimConstant(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity p = new WrapperPlayClientInteractEntity(event);
            if (p.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) lastAttackTime = System.currentTimeMillis();
        }
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (System.currentTimeMillis() - lastAttackTime > 200) return;
        float deltaYaw = rotationUpdate.getDeltaXRotABS();
        if (deltaYaw > 1.0f && deltaYaw == lastDeltaYaw) {
            if (++buffer > 5) flagAndAlert("speed=" + deltaYaw);
        } else buffer = Math.max(0, buffer - 0.5);
        lastDeltaYaw = deltaYaw;
    }
}
