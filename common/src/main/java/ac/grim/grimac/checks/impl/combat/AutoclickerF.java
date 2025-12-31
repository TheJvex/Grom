package ac.grim.grimac.checks.impl.combat;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import java.util.ArrayList;
import java.util.List;

@CheckData(name = "AutoClicker (F)", setback = 0, description = "Statistical Identity (Artemis Inspiration)")
public class AutoclickerF extends Check implements PacketCheck {
    private final List<Double> samples = new ArrayList<>();
    private double lastAverage, lastStdDev;
    private int ticks;
    private double buffer;

    public AutoclickerF(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isUpdate(event.getPacketType())) ticks++;
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (ticks < 10) {
                samples.add((double) ticks);
                if (samples.size() >= 10) {
                    double avg = samples.stream().mapToDouble(d -> d).average().orElse(0);
                    double std = Math.sqrt(samples.stream().mapToDouble(d -> Math.pow(d - avg, 2)).sum() / 10);
                    if (avg == lastAverage && std == lastStdDev && std > 0) {
                        if (++buffer > 2) flagAndAlert("Identical Stats");
                    } else buffer = Math.max(0, buffer - 0.5);
                    lastAverage = avg; lastStdDev = std;
                    samples.clear();
                }
            }
            ticks = 0;
        }
    }
}
