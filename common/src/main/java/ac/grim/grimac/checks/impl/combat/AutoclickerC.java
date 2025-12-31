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
import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(name = "AutoClicker (C)", setback = 10, description = "Tick Variation(Kauri Inspiration)")
public class AutoclickerC extends Check implements PacketCheck {

    private int lastClickTick = 0;
    private int localTick = 0;
    private float buffer = 0;
    private boolean isDigging = false;
    private final Deque<Integer> tickDeltas = new ArrayDeque<>();

    public AutoclickerC(@NotNull GrimPlayer player) {
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

        if (isUpdate(event.getPacketType())) {
            localTick++;
        }

        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            WrapperPlayClientAnimation wrapper = new WrapperPlayClientAnimation(event);
            if (isDigging || wrapper.getHand() != InteractionHand.MAIN_HAND) return;

            int currentTick = localTick;
            int delta = currentTick - lastClickTick;

            if (lastClickTick != 0 && delta < 100) {
                tickDeltas.add(delta);
                if (tickDeltas.size() > 30) tickDeltas.removeFirst();

                if (tickDeltas.size() > 8) {
                    int max = -1000, min = 1000;
                    double average = 0;
                    for (int d : tickDeltas) {
                        max = Math.max(d, max);
                        min = Math.min(d, min);
                        average += d;
                    }
                    average /= tickDeltas.size();
                    int range = max - min;
                    if (average < 3.0 && range <= 1) {
                        if (++buffer > 12) this.flagAndAlert("range=" + range);
                    } else {
                        buffer = Math.max(0, buffer - 0.5f);
                    }
                }
            }
            lastClickTick = currentTick;
        }
    }
}
