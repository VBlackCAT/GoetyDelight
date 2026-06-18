#version 150

uniform vec4 ColorModulator;
uniform float intensity;
uniform int EffectMode;

in vec4 vertexColor;
in vec2 texCoord;
in float time;

out vec4 fragColor;

float hash(float n) {
    return fract(sin(n) * 43758.5453);
}

float hash2(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash2(i);
    float b = hash2(i + vec2(1.0, 0.0));
    float c = hash2(i + vec2(0.0, 1.0));
    float d = hash2(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p *= 2.1;
        a *= 0.48;
    }
    return v;
}

float ring(float radius, float target, float width) {
    return 1.0 - smoothstep(width * 0.45, width, abs(radius - target));
}

vec3 riftPalette(float t, float angle) {
    vec3 violet  = vec3(0.55, 0.10, 1.00);
    vec3 cyan    = vec3(0.00, 0.88, 1.00);
    vec3 magenta = vec3(1.00, 0.08, 0.72);
    float wave = sin(t * 2.4 + angle * 1.3) * 0.5 + 0.5;
    vec3 base = mix(violet, cyan, wave);
    return mix(base, magenta, smoothstep(0.6, 1.0, sin(t * 1.1 + angle * 2.7) * 0.5 + 0.5) * 0.42);
}

void main() {
    vec2 uv = texCoord;
    vec2 p = uv * 2.0 - 1.0;
    float radius = length(p);
    float angle = atan(p.y, p.x);
    vec3 base = vertexColor.rgb;
    float alpha = vertexColor.a;
    vec3 color;
    float mask;

    if (EffectMode == 0) {
        // Crystal shard: faceted crystal with inner fracture glow
        float facetAngle = angle + sin(radius * 8.0 - time * 2.2) * 0.35;
        float facets = abs(sin(facetAngle * 5.0)) * smoothstep(0.05, 0.30, radius);
        float fracture = fbm(p * 6.0 + time * 0.8) * smoothstep(0.10, 0.55, radius);
        float edge = smoothstep(0.82, 0.60, radius) * (1.0 - smoothstep(0.55, 0.35, radius));
        float edgeGlow = pow(edge, 1.6) * (0.72 + 0.28 * sin(time * 5.0 + angle * 3.0));
        float core = pow(max(0.0, 1.0 - radius * 1.15), 2.2) * 0.55;
        float crack = pow(abs(sin(angle * 7.0 + radius * 12.0 - time * 3.0)), 18.0)
                    * smoothstep(0.20, 0.65, radius)
                    * (1.0 - smoothstep(0.68, 0.88, radius));
        mask = (facets * 0.40 + fracture * 0.55 + edgeGlow * 1.20 + core + crack * 0.85)
             * smoothstep(0.95, 0.78, radius);
        color = riftPalette(time * 0.6 + radius * 2.0, angle) + base * 0.30;
        color += vec3(0.22, 0.04, 0.38) * crack;

    } else if (EffectMode == 1) {
        // Void core: swirling dark center with pulsing event horizon
        float swirl = fbm(p * 3.0 + vec2(cos(time * 0.7), sin(time * 0.7)) * 2.0);
        float voidHole = smoothstep(0.42, 0.05, radius);
        float horizon = ring(radius, 0.38, 0.06) * (0.65 + 0.35 * sin(time * 3.8 + angle * 4.0));
        float tendril = pow(abs(sin(angle * 6.0 + swirl * 8.0 - time * 2.5)), 6.0)
                      * smoothstep(0.50, 0.15, radius);
        float pulse = 0.60 + 0.40 * sin(time * 2.1);
        mask = (voidHole * 0.85 + horizon * 1.10 + tendril * 0.55 + swirl * 0.22) * pulse
             * smoothstep(0.80, 0.55, radius);
        color = mix(vec3(0.02, 0.00, 0.06), riftPalette(time * 0.4, angle), 0.38 + voidHole * 0.40);
        color += vec3(0.40, 0.10, 0.80) * horizon;

    } else if (EffectMode == 2) {
        // Arc lightning: bright electric arcs connecting shard positions
        float d = length(p);
        float line1 = 1.0 - smoothstep(0.00, 0.04, abs(sin(angle * 3.0 + time * 4.0) * p.x + cos(angle * 2.0 - time * 3.0) * p.y));
        float line2 = 1.0 - smoothstep(0.00, 0.03, abs(p.y - sin(p.x * 8.0 + time * 6.0) * 0.22));
        float sparks = step(0.92, hash2(floor(uv * 12.0) + floor(time * 8.0)));
        float hotCore = pow(max(0.0, 1.0 - d * 1.8), 3.0);
        float pulse = 0.55 + 0.45 * sin(time * 12.0 + uv.x * 10.0);
        mask = (line1 * 1.20 + line2 * 0.90 + sparks * 0.65 + hotCore * 0.50) * pulse;
        color = mix(vec3(0.62, 0.30, 1.00), vec3(0.20, 0.90, 1.00), line2 * 0.65);
        color += vec3(0.80, 0.60, 1.00) * hotCore;

    } else {
        // Mode 3: Outer aura glow pass
        float glow = pow(max(0.0, 1.0 - smoothstep(0.08, 0.95, radius)), 1.45);
        float ring1 = ring(radius, 0.68, 0.18) * 0.38;
        float drift = fbm(p * 2.5 + time * 0.5) * smoothstep(0.90, 0.40, radius);
        float pulse = 0.68 + 0.32 * sin(time * 2.8 + radius * 5.0);
        mask = (glow * 0.32 + ring1 + drift * 0.28) * pulse;
        color = riftPalette(time * 0.8 + radius * 1.5, angle) + vec3(0.12, 0.02, 0.22);
    }

    mask = clamp(mask, 0.0, 1.50);
    fragColor = vec4(color * mask * intensity, alpha * mask) * ColorModulator;
}
