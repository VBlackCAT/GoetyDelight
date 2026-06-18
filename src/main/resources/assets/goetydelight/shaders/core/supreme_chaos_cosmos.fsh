#version 150

uniform vec4 ColorModulator;
uniform float intensity;
uniform int EffectMode;

in vec4 vertexColor;
in vec2 texCoord;
in float time;

out vec4 fragColor;

// ---- utility ----

float hash(float n) { return fract(sin(n) * 43758.5453); }

float hash2(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

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
        p *= 2.13;
        a *= 0.47;
    }
    return v;
}

float ring(float radius, float target, float width) {
    return 1.0 - smoothstep(width * 0.42, width, abs(radius - target));
}

float star5(vec2 p, float size) {
    float a = atan(p.y, p.x);
    float r = length(p);
    float s = 0.5 + 0.5 * cos(a * 5.0);
    return smoothstep(size, size * 0.3, r * (0.6 + 0.4 * s));
}

vec3 hsv2rgb(vec3 c) {
    vec3 p = abs(fract(vec3(c.x) + vec3(0.0, 2.0/3.0, 1.0/3.0)) * 6.0 - 3.0);
    return c.z * mix(vec3(1.0), clamp(p - 1.0, 0.0, 1.0), c.y);
}

vec3 rainbow(float phase) {
    return hsv2rgb(vec3(fract(phase * 0.1), 0.82, 1.0));
}

vec3 chaosColor(float phase, float angle) {
    vec3 r1 = rainbow(phase);
    vec3 gold = vec3(1.0, 0.82, 0.28);
    vec3 hotPink = vec3(1.0, 0.18, 0.72);
    float m = sin(phase * 0.7 + angle) * 0.5 + 0.5;
    return mix(mix(r1, gold, 0.30), hotPink, m * 0.25);
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
        // ---- Triple holy halo rings ----
        float spin1 = angle + time * 1.80;
        float spin2 = angle - time * 1.25;
        float spin3 = angle + time * 0.72;

        // outer ring - serrated edge
        float r1 = ring(radius, 0.82, 0.040);
        float gate1 = step(0.22, hash(floor((spin1 + 3.14) / 6.28 * 32.0)));
        float outer = r1 * gate1;

        // mid ring - pulsing arcs
        float r2 = ring(radius, 0.58, 0.032);
        float gate2 = 0.5 + 0.5 * sin(spin2 * 12.0);
        float mid = r2 * gate2;

        // inner ring - solid with shimmer
        float r3 = ring(radius, 0.36, 0.028);
        float shimmer = 0.70 + 0.30 * sin(spin3 * 8.0 + radius * 20.0);
        float inner = r3 * shimmer;

        // spokes radiating from center
        float spokes = pow(abs(sin(angle * 8.0 - time * 2.4)), 28.0)
                     * smoothstep(0.15, 0.55, radius)
                     * (1.0 - smoothstep(0.80, 0.95, radius));

        // bright center star
        float center = star5(p * 1.2, 0.28) * (0.65 + 0.35 * sin(time * 3.5));

        // void ring cut for depth
        float voidCut = smoothstep(0.22, 0.30, radius);

        mask = (outer * 1.15 + mid * 0.90 + inner * 0.75 + spokes * 0.50 + center * 0.80) * voidCut;
        color = chaosColor(time * 2.2 + angle * 0.8, angle) + base * 0.28;

    } else if (EffectMode == 1) {
        // ---- Star orbit trail ----
        vec2 q = p;
        float d = length(q);

        // multiple orbiting star dots
        float stars = 0.0;
        for (int i = 0; i < 8; i++) {
            float orbitAngle = time * (0.6 + float(i) * 0.11) + float(i) * 0.785;
            float orbitR = 0.42 + 0.22 * sin(time * 0.3 + float(i) * 1.7);
            vec2 starPos = vec2(cos(orbitAngle), sin(orbitAngle)) * orbitR;
            float sz = 0.045 + 0.020 * sin(time * 2.1 + float(i));
            stars += star5(q - starPos, sz);
        }

        // trailing sparkle dust
        float dust = 0.0;
        for (int i = 0; i < 12; i++) {
            float da = time * (0.9 + float(i) * 0.07) + float(i) * 0.523;
            float dr = 0.30 + 0.38 * fract(sin(float(i) * 7.31));
            vec2 dp = vec2(cos(da), sin(da)) * dr;
            float sparkle = smoothstep(0.06, 0.00, length(q - dp));
            sparkle *= 0.5 + 0.5 * sin(time * 8.0 + float(i) * 2.3);
            dust += sparkle;
        }

        // faint orbital ring glow
        float orbitGlow = ring(radius, 0.55, 0.20) * 0.18;

        mask = (stars * 1.40 + dust * 0.70 + orbitGlow) * smoothstep(0.95, 0.75, radius);
        color = rainbow(time * 1.5 + d * 3.0) + vec3(0.20, 0.15, 0.05) * stars;

    } else if (EffectMode == 2) {
        // ---- Sacred light burst / divine rays ----
        float rays = 0.0;
        for (int i = 0; i < 6; i++) {
            float rayAngle = float(i) * 3.1415926 / 6.0 + time * 0.22;
            float cosA = cos(rayAngle);
            float sinA = sin(rayAngle);
            vec2 rp = vec2(p.x * cosA + p.y * sinA, -p.x * sinA + p.y * cosA);
            float beam = smoothstep(0.035, 0.00, abs(rp.y)) * smoothstep(1.0, 0.15, abs(rp.x));
            beam *= 0.60 + 0.40 * sin(time * 3.0 + float(i) * 1.05);
            rays += beam;
        }

        // pulsing core
        float core = pow(max(0.0, 1.0 - radius * 2.5), 3.0);

        // radial burst wave
        float burst = ring(radius, fract(time * 0.18) * 0.9, 0.05) * (1.0 - fract(time * 0.18));

        mask = (rays * 1.0 + core * 1.20 + burst * 0.55) * smoothstep(1.0, 0.60, radius);
        color = mix(vec3(1.0, 0.95, 0.70), rainbow(time * 0.8), 0.35);
        color += vec3(0.50, 0.30, 0.10) * core;

    } else if (EffectMode == 3) {
        // ---- Chaos outer aura / distortion halo ----
        float glow = pow(max(0.0, 1.0 - smoothstep(0.05, 0.98, radius)), 1.30);

        // turbulent noise field
        vec2 warp = vec2(
            fbm(p * 3.0 + time * 0.6),
            fbm(p * 3.0 + vec2(5.2, 1.3) + time * 0.6)
        );
        float turbulence = fbm(p * 2.0 + warp * 3.0 + time * 0.4);

        float ring1 = ring(radius, 0.72, 0.22) * 0.32;
        float ring2 = ring(radius, 0.48, 0.16) * 0.20;

        float pulse = 0.65 + 0.35 * sin(time * 2.4 + radius * 6.0);
        float flicker = 0.85 + 0.15 * sin(time * 7.3 + angle * 3.0);

        mask = (glow * 0.35 + turbulence * 0.32 + ring1 + ring2) * pulse * flicker;
        color = chaosColor(time * 1.8 + radius * 2.0, angle);
        color += vec3(0.15, 0.05, 0.25) * turbulence;

    } else {
        // ---- Mode 4: Sparkle particle shower ----
        float particles = 0.0;
        for (int i = 0; i < 16; i++) {
            float seed = float(i) * 17.31;
            float pa = hash(seed) * 6.2831853 + time * (0.3 + hash(seed + 1.0) * 0.8);
            float pr = hash(seed + 2.0) * 0.85;
            float pSize = 0.015 + hash(seed + 3.0) * 0.025;
            vec2 pp = vec2(cos(pa), sin(pa)) * pr;
            float bright = smoothstep(pSize, 0.0, length(p - pp));
            bright *= step(0.3, hash(seed + floor(time * 4.0)));
            particles += bright;
        }

        // falling streaks
        float streaks = 0.0;
        for (int i = 0; i < 5; i++) {
            float sx = (hash(float(i) * 31.7) - 0.5) * 1.6;
            float sy = fract(hash(float(i) * 19.3) - time * (0.4 + hash(float(i) * 7.1) * 0.5));
            sy = sy * 2.0 - 1.0;
            vec2 sp = vec2(sx, sy);
            float streak = smoothstep(0.025, 0.0, abs(p.x - sp.x))
                         * smoothstep(0.12, 0.0, abs(p.y - sp.y))
                         * (1.0 - smoothstep(0.0, 0.06, abs(p.y - sp.y)));
            streaks += streak;
        }

        mask = (particles * 0.85 + streaks * 0.60) * smoothstep(0.98, 0.80, radius);
        color = rainbow(time * 3.0 + p.x * 2.0 + p.y * 3.0);
    }

    mask = clamp(mask, 0.0, 1.60);
    fragColor = vec4(color * mask * intensity, alpha * mask) * ColorModulator;
}
