
package dev.trick.zonecontrolplus.config;

public class PotionConfig {
    private final int duration;
    private final int amplifier;

    public PotionConfig(int duration, int amplifier) {
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public boolean isPermanent() {
        return this.duration == -1;
    }

    public String toString() {
        return "PotionConfig{duration=" + (String)(this.duration == -1 ? "permanent" : this.duration + "ticks") + ", amplifier=" + this.amplifier + "}";
    }
}

