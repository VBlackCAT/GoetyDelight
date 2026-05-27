package net.v_black_cat.goetydelight.visual;

public class EntityVisualEffectType {
    private final int defaultDuration;
    private final boolean persistent;
    private final boolean renderWhenInvisible;
    private final boolean renderInFirstPerson;
    private final double renderDistance;

    public EntityVisualEffectType(Properties properties) {
        this.defaultDuration = properties.defaultDuration;
        this.persistent = properties.persistent;
        this.renderWhenInvisible = properties.renderWhenInvisible;
        this.renderInFirstPerson = properties.renderInFirstPerson;
        this.renderDistance = properties.renderDistance;
    }

    public int defaultDuration() {
        return defaultDuration;
    }

    public boolean persistent() {
        return persistent;
    }

    public boolean renderWhenInvisible() {
        return renderWhenInvisible;
    }

    public boolean renderInFirstPerson() {
        return renderInFirstPerson;
    }

    public double renderDistance() {
        return renderDistance;
    }

    public boolean hasRenderDistanceLimit() {
        return renderDistance > 0.0D;
    }

    public boolean shouldRenderInvisibleEntity() {
        return renderWhenInvisible;
    }

    public static Properties properties() {
        return new Properties();
    }

    public static class Properties {
        private int defaultDuration = EntityVisualEffects.INFINITE;
        private boolean persistent;
        private boolean renderWhenInvisible;
        private boolean renderInFirstPerson;
        private double renderDistance = 64.0D;

        public Properties defaultDuration(int defaultDuration) {
            this.defaultDuration = defaultDuration;
            return this;
        }

        public Properties infiniteDuration() {
            return defaultDuration(EntityVisualEffects.INFINITE);
        }

        public Properties persistent() {
            this.persistent = true;
            return this;
        }

        public Properties renderWhenInvisible() {
            this.renderWhenInvisible = true;
            return this;
        }

        public Properties renderInFirstPerson() {
            this.renderInFirstPerson = true;
            return this;
        }

        public Properties renderDistance(double renderDistance) {
            this.renderDistance = renderDistance;
            return this;
        }
    }
}
