package ac.grim.grimac.checks.impl.combat;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "AutoClicker (D)", setback = 15, description = "Auto-Block Sync(Kauri Inspiration)")
public class AutoclickerD extends Check implements PacketCheck {

    private long lastArmTime = 0;
    private double currentCps = 0;
    private boolean swordBlocked = false;
    private int armTicks = 0;
    private double verbose = 0;
    private boolean isDigging = false;

    public AutoclickerD(@NotNull GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.getClientVersion().isNewerThan(ClientVersion.V_1_8)) return;

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging dig = new WrapperPlayClientPlayerDigging(event);
            if (dig.getAction() == DiggingAction.START_DIGGING) isDigging = true;
            else if (dig.getAction() == DiggingAction.CANCELLED_DIGGING ||
                    dig.getAction() == DiggingAction.FINISHED_DIGGING) isDigging = false;
        }

        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (isDigging) return;
            long now = event.getTimestamp();
            if (lastArmTime != 0) currentCps = 1000.0 / (now - lastArmTime);
            lastArmTime = now;
            armTicks++;
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement placement = new WrapperPlayClientPlayerBlockPlacement(event);

            if (placement.getItemStack().isPresent()) {
                if (placement.getItemStack().get().getType().getName().getKey().contains("sword")) {
                    swordBlocked = true;
                }
            }
        }

        if (isUpdate(event.getPacketType())) {
            if (swordBlocked && armTicks > 0) {
                if (armTicks == 1 && currentCps > 3.0) {
                    if (currentCps > 7.0) verbose++;
                    if (verbose > 15) this.flagAndAlert("cps=" + (int)currentCps);
                } else {
                    verbose = Math.max(0, verbose - 20);
                }
            }
            swordBlocked = false;
            armTicks = 0;
        }
    }
}
