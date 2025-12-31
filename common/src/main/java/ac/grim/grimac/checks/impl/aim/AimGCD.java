package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.processor.AimProcessor;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(name = "Aim (GCD)", setback = 0, description = "AimGCD(Kauri Inspiration)")
public class AimGCD extends Check implements RotationCheck {

    private float buffer;
    private long lastGridTime;


    private final Deque<Double> recentDivisors = new ArrayDeque<>();
    private static final int MAX_SAMPLES = 45;

    public AimGCD(GrimPlayer playerData) {
        super(playerData);
        this.lastGridTime = System.currentTimeMillis();
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {

        if (rotationUpdate.getDeltaXRot() == 0 && rotationUpdate.getDeltaYRot() == 0) return;

        AimProcessor processor = player.checkManager.getRotationCheck(AimProcessor.class);
        if (processor == null) return;



        double currentDivisor = processor.divisorY;


        if (currentDivisor < 0.009) {
            lastGridTime = System.currentTimeMillis();
        }


        recentDivisors.add(currentDivisor);
        if (recentDivisors.size() > MAX_SAMPLES) {
            recentDivisors.removeFirst();
        }


        if (recentDivisors.size() < 40) return;

        int suspiciousCount = 0;
        double min = 100.0, max = -100.0;

        for (Double val : recentDivisors) {

            if (val < 0.007 && val > 1e-5) {
                suspiciousCount++;
                if (val < min) min = val;
                if (val > max) max = val;
            }
        }


        double variation = max - min;



        boolean lastGridPassed = (System.currentTimeMillis() - lastGridTime) > 150;
        final float deltaPitch = Math.abs(rotationUpdate.getDeltaYRot());
        final float deltaYaw = Math.abs(rotationUpdate.getDeltaXRot());


        boolean isSignificantMovement = deltaPitch > 0.15 || deltaYaw > 0.15;


        if (suspiciousCount > 40
                && lastGridPassed
                && variation < 0.005
                && isSignificantMovement) {


            if (deltaPitch > 0.15 && deltaPitch < 20) {
                buffer++;



                if (buffer > 8) {
                    flagAndAlert("gcd=" + String.format("%.5f", currentDivisor));
                }
            }
        } else {

            buffer = Math.max(0, buffer - 0.75f);
        }
    }
}
