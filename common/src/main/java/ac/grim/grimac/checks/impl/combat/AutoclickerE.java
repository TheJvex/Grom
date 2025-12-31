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

import java.util.*;

@CheckData(name = "AutoClicker (E)", setback = 0, description = "Low Entropy (Rhys Inspiration)")
public class AutoclickerE extends Check implements PacketCheck {
    private final List<Integer> intervals = new ArrayList<>();
    private int ticksSinceLastClick, buffer;
    private boolean isDigging;
    private long lastRightClickTime;

    public AutoclickerE(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging d = new WrapperPlayClientPlayerDigging(event);
            if (d.getAction() == DiggingAction.START_DIGGING) isDigging = true;
            else if (d.getAction() == DiggingAction.CANCELLED_DIGGING || d.getAction() == DiggingAction.FINISHED_DIGGING) isDigging = false;
        }


        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
                || event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            this.lastRightClickTime = System.currentTimeMillis();
        }

        if (isUpdate(event.getPacketType())) ticksSinceLastClick++;

        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            WrapperPlayClientAnimation w = new WrapperPlayClientAnimation(event);


            if (w.getHand() != InteractionHand.MAIN_HAND || isDigging || (System.currentTimeMillis() - lastRightClickTime < 50)) {
                return;
            }

            if (ticksSinceLastClick < 10) {
                intervals.add(ticksSinceLastClick);
                if (intervals.size() >= 60) {
                    int unique = new HashSet<>(intervals).size();
                    if (unique <= 4) {
                        if (++buffer > 2) flagAndAlert("unique=" + unique);
                    }
                    else buffer = Math.max(0, buffer / 2);
                    intervals.clear();
                }
            }
            ticksSinceLastClick = 0;
        }
    }
}
