package ac.grim.grimac.checks.impl.combat.killaura;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.math.KauriMath;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "KillAura (Calc)", setback = 0, description = "Aura Calc(Kauri Inspiration)")
public class KACalc extends Check implements RotationCheck, PacketCheck {

    private KAZero kaZero;

    private final List<Double> YAW_OFFSET = new ArrayList<>();
    private final List<Double> PITCH_OFFSET = new ArrayList<>();

    private int targetId = -1;
    private long lastAttackTime = 0;

    public KACalc(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                this.targetId = packet.getEntityId();
                this.lastAttackTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (kaZero == null) kaZero = player.checkManager.getCheck(KAZero.class);
        if (kaZero == null) return;

        if (System.currentTimeMillis() - lastAttackTime > 1000 || targetId == -1) return;

        PacketEntity target = player.compensatedEntities.entityMap.get(targetId);
        if (target == null) return;

        SimpleCollisionBox box = target.getPossibleCollisionBoxes();
        if (box == null) return;

        double tX = (box.minX + box.maxX) / 2.0;
        double tZ = (box.minZ + box.maxZ) / 2.0;

        double entityHeight = box.maxY - box.minY;
        double tY = box.minY + (entityHeight * 0.85);

        double pX = player.x;
        double pY = player.y + player.getEyeHeight();
        double pZ = player.z;

        float[] ideal = KauriMath.getRotation(pX, pY, pZ, tX, tY, tZ);

        float yawOffset = KauriMath.getAngleDistance(player.yaw, ideal[0]);
        float pitchOffset = KauriMath.getAngleDistance(player.pitch, ideal[1]);

        YAW_OFFSET.add((double) yawOffset);
        PITCH_OFFSET.add((double) pitchOffset);

        if (YAW_OFFSET.size() > 10) YAW_OFFSET.remove(0);
        if (PITCH_OFFSET.size() > 10) PITCH_OFFSET.remove(0);

        if (YAW_OFFSET.size() >= 5) {
            double[] offsetArr = {yawOffset, pitchOffset};

            kaZero.runCheck(offsetArr);
        }
    }
}
