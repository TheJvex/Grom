package ac.grim.grimac.checks.impl.combat.killaura;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.player.GrimPlayer;

@CheckData(name = "KillAura (Zero)", setback = 0, description = "(Offset 0.0)(Kauri Inspiration)")
public class KAZero extends Check {

    private int buffer;

    public KAZero(GrimPlayer player) {
        super(player);
    }

    /**
     * @param offset Array de doubles onde:
     *               offset[0] = Yaw Offset (Diferença horizontal)
     *               offset[1] = Pitch Offset (Diferença vertical)
     */
    public void runCheck(double[] offset) {
        // se o offset for "Zero" é um belissimo hack kkkkk

        if (Math.abs(offset[0]) < 0.001) {
            if (++buffer > 2) {
                flagAndAlert(String.format("offset=%.5f", offset[0]));
            }
        } else {
            buffer = 0;
        }
    }
}
