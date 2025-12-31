package ac.grim.grimac.checks.impl.combat.killaura;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "KillAura (PostSnap)", setback = 0, description = "Rotation Pos-Hit (LightAC Inspiration)")
public class AuraPostSnap extends Check implements RotationCheck, PacketCheck {
    private long lastStaticHitTime;
    private double buffer;
    private boolean lastPacketWasAttack;

    public AuraPostSnap(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity w = new WrapperPlayClientInteractEntity(event);
            if (w.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) lastPacketWasAttack = true;
        }
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        long now = System.currentTimeMillis();
        if (lastPacketWasAttack) {
            if (rotationUpdate.getDeltaXRotABS() < 0.1 && rotationUpdate.getDeltaYRotABS() < 0.1) lastStaticHitTime = now;
            lastPacketWasAttack = false;
            return;
        }
        long timeSinceHit = now - lastStaticHitTime;
        if (timeSinceHit < 200 && lastStaticHitTime != 0) {
            if (rotationUpdate.getDeltaXRotABS() > 35.0) {
                if (++buffer > 5) {
                    flagAndAlert("snap=" + rotationUpdate.getDeltaXRotABS());
                    buffer = 3;
                }
                lastStaticHitTime = 0;
            }
        } else {
            buffer = Math.max(0, buffer - 0.01);
        }
    }
}
