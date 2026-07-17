package net.v_black_cat.goetydelight.buff;


public class BuffType {
    private final int defaultDuration;
    private final int defaultAmplifier;
    private final boolean stackable;

    public BuffType(int defaultDuration, int defaultAmplifier, boolean stackable) {
        this.defaultDuration = defaultDuration;
        this.defaultAmplifier = defaultAmplifier;
        this.stackable = stackable;
    }

    public int defaultDuration() { return defaultDuration; }
    public int defaultAmplifier() { return defaultAmplifier; }
    public boolean stackable() { return stackable; }
}
