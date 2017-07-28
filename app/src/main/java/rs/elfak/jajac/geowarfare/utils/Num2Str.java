package rs.elfak.jajac.geowarfare.utils;

public class Num2Str {
    private static final char[] magnitudes = {'K', 'M', 'G', 'T', 'P', 'E'};

    // Converts a positive integer to a 4-char string
    public static String convert(long number) {
        if (number < 10000)
            return String.valueOf(number);

        for (int i = 0; ; i++) {
            number /= 1000;
            if (number < 1000)
                return String.valueOf(number) + magnitudes[i];
        }
    }
}
