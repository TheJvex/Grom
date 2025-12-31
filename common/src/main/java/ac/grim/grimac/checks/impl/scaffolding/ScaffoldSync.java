package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "Scaffold (Sync)", setback = 0, description = "Scaffold Packet Detection(Kauri Inspiration)")
public class ScaffoldSync extends BlockPlaceCheck {

    private long lastFlyingTime;
    private float buffer;

    public ScaffoldSync(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (!player.packetStateData.lastPacketWasTeleport) {
                lastFlyingTime = event.getTimestamp();
            }
        }

        super.onPacketReceive(event);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (player.gamemode == GameMode.CREATIVE
                || place.material == StateTypes.SCAFFOLDING) return;

        long delta = System.currentTimeMillis() - lastFlyingTime;

        if (delta <= 25) {
            if (++buffer > 10) {
                flagAndAlert("delta=" + delta + "ms");
            }
        } else {
            // Decaimento
            buffer = Math.max(0, buffer - 0.25f);
        }
    }
}
