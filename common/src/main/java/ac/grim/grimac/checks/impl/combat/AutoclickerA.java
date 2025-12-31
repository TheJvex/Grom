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
import org.jetbrains.annotations.NotNull;

@CheckData(name = "AutoClicker (A)", setback = 10, description = "Cps Limiter(Kauri Inspiration)")
public class AutoclickerA extends Check implements PacketCheck {

    private int flyingTicks = 0;
    private int cps = 0;
    private boolean isDigging = false;

    public AutoclickerA(@NotNull GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging dig = new WrapperPlayClientPlayerDigging(event);
            if (dig.getAction() == DiggingAction.START_DIGGING) isDigging = true;
            else if (dig.getAction() == DiggingAction.CANCELLED_DIGGING ||
                    dig.getAction() == DiggingAction.FINISHED_DIGGING) isDigging = false;
        }

        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            WrapperPlayClientAnimation wrapper = new WrapperPlayClientAnimation(event);
            if (wrapper.getHand() == InteractionHand.MAIN_HAND && !isDigging) {
                cps++;
            }
        }

        if (isUpdate(event.getPacketType())) {
            flyingTicks++;
            if (flyingTicks >= 20) {
                if (cps > 25) this.flagAndAlert("cps=" + cps);
                cps = 0;
                flyingTicks = 0;
            }
        }
    }
}
