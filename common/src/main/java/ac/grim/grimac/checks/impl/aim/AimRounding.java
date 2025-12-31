package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimAssist (C)", setback = 0, description = "GCD Rounding (Kauri Inspiration)")
public class AimRounding extends Check implements RotationCheck {
    private double buffer;

    public AimRounding(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        float deltaPitch = rotationUpdate.getDeltaYRotABS();
        if (deltaPitch < 0.5f) return;
        boolean rounded = (deltaPitch % 0.1f == 0) || (deltaPitch % 0.05f == 0) || (deltaPitch % 1.0f == 0);
        if (rounded) {
            if (++buffer > 10) flagAndAlert("dp=" + deltaPitch);
        } else buffer = Math.max(0, buffer - 0.25);
    }
}
