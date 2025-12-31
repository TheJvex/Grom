package ac.grim.grimac.checks.impl.combat;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "AutoClicker (B)", setback = 10, description = "Packet Sync(Kauri Inspiration)")
public class AutoclickerB extends Check implements PacketCheck {

    private long lastFlyingTime = 0;
    private long lastRightClickTime = 0;
    private double buffer = 0;
    private boolean isDigging = false;

    public AutoclickerB(@NotNull GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging dig = new WrapperPlayClientPlayerDigging(event);
            if (dig.getAction() == DiggingAction.START_DIGGING) {
                isDigging = true;
            } else if (dig.getAction() == DiggingAction.CANCELLED_DIGGING ||
                    dig.getAction() == DiggingAction.FINISHED_DIGGING) {
                isDigging = false;
            }
        }


        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
                || event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            this.lastRightClickTime = System.currentTimeMillis();
        }


        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (!player.getSetbackTeleportUtil().shouldBlockMovement()) {
                this.lastFlyingTime = event.getTimestamp();
            }
        }


        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            WrapperPlayClientAnimation wrapper = new WrapperPlayClientAnimation(event);
            if (wrapper.getHand() != InteractionHand.MAIN_HAND) return;


            if (System.currentTimeMillis() - lastRightClickTime < 50) return;


            if (isDigging) {
                buffer = Math.max(0, buffer - 0.5);
                return;
            }

            long delta = event.getTimestamp() - lastFlyingTime;

            if (delta < 10) {
                if (++buffer > 4) {
                    flagAndAlert("delta=" + delta + "ms");
                }
            } else {
                buffer = Math.max(0, buffer - 0.25);
            }
        }
    }
}
