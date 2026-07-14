package net.v_black_cat.goetydelight.util;

public class TickConverterUtil {
    public static int minToTick(float min){
        return (int)(min*60*20);
    }
    public static int sToTick(float s){
        return (int)(s*20);
    }
}
