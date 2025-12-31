package ac.grim.grimac.utils.math;

import java.util.List;

public class KauriMath {

    public static float[] getRotation(double x, double y, double z, double tx, double ty, double tz) {
        double diffX = tx - x;
        double diffZ = tz - z;
        double diffY = ty - y;

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0D / Math.PI));

        return new float[]{yaw, pitch};
    }

    public static float getAngleDistance(float angle1, float angle2) {
        float dist = Math.abs(angle1 - angle2) % 360.0F;
        if (dist > 180.0F) {
            dist = 360.0F - dist;
        }
        return dist;
    }

    public static double stdev(List<Double> numbers) {
        if (numbers == null || numbers.isEmpty()) return 0.0;
        return GrimMath.calculateSD(numbers);
    }


    public static double getGridDouble(List<Double> samples) {
        if (samples.size() < 2) return 0;

        double result = samples.get(0);
        for (int i = 1; i < samples.size(); i++) {
            result = GrimMath.gcd(result, samples.get(i));
        }
        return result;
    }
}
