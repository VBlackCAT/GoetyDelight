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

float ring(float d, float r, float w) {
    return 1.0 - smoothstep(w * 0.45, w, abs(d - r));
}

float arc(float a, float cells, float seed) {
    float id = floor((a + 3.1415926) / 6.2831853 * cells + seed);
    return step(0.30, hash(id * 27.31 + seed * 9.17));
}

vec3 spectral(float p) {
    return 0.55 + 0.45 * cos(p + vec3(0.0, 2.0943952, 4.1887903));
}

float crossBar(vec2 p, vec2 center, vec2 halfSize) {
    vec2 q = abs(p - center);
    float box = (1.0 - smoothstep(halfSize.x * 0.82, halfSize.x, q.x)) * (1.0 - smoothstep(halfSize.y * 0.70, halfSize.y, q.y));
    return box;
}

void main() {
    vec2 uv = texCoord;
    vec2 p = uv * 2.0 - 1.0;
    float d = length(p);
    float a = atan(p.y, p.x);
    vec3 base = vertexColor.rgb;
    float alpha = vertexColor.a;
    vec3 color = base;
    float mask = 0.0;

    if (EffectMode == 0) {
        float outer = ring(d, 0.76 + 0.025 * sin(a * 5.0 + time * 2.4), 0.052) * arc(a + time * 0.8, 34.0, 3.0);
        float inner = ring(d, 0.52, 0.030) * arc(a - time * 1.1, 20.0, 7.0);
        float flare = pow(max(0.0, 1.0 - d), 3.2) * (0.45 + 0.55 * sin(a * 11.0 - time * 5.0));
        float rays = pow(abs(sin(a * 18.0 + time * 1.6)), 18.0) * smoothstep(0.40, 0.82, d) * (1.0 - smoothstep(0.84, 1.0, d));
        mask = outer * 1.25 + inner * 0.72 + rays * 0.55 + flare * 0.25;
        color = mix(vec3(1.0, 0.35, 0.04), vec3(0.45, 0.03, 0.0), d) + base * 0.42;
    } else if (EffectMode == 1) {
        vec2 q = p;
        q.x *= 0.48;
        float slit = (1.0 - smoothstep(0.02, 0.16, abs(q.x))) * (1.0 - smoothstep(0.18, 0.90, abs(q.y)));
        float iris = ring(length(vec2(p.x * 0.92, p.y * 1.28)), 0.47, 0.050);
        float collapse = 1.0 - smoothstep(0.10, 0.95, d + 0.08 * sin(a * 4.0 + time * 2.1));
        float veins = pow(abs(sin(a * 7.0 + d * 17.0 - time * 4.0)), 10.0) * smoothstep(0.32, 0.88, d);
        mask = slit * 1.55 + iris * 0.88 + collapse * 0.36 + veins * 0.35;
        color = mix(vec3(0.04, 0.0, 0.08), vec3(0.82, 0.0, 1.0), iris + veins) + vec3(0.75, 0.0, 0.08) * slit;
    } else if (EffectMode == 2) {
        float poly = ring(d + 0.018 * sin(a * 6.0 - time), 0.62, 0.034);
        float outer = ring(d, 0.78, 0.030) * arc(a + time * 0.55, 24.0, 11.0);
        float tick = pow(abs(sin(a * 12.0)), 22.0) * smoothstep(0.54, 0.74, d) * (1.0 - smoothstep(0.76, 0.90, d));
        float cross = crossBar(p, vec2(0.0), vec2(0.035, 0.54)) + crossBar(p, vec2(0.0), vec2(0.48, 0.035));
        mask = poly * 0.82 + outer * 1.0 + tick * 0.75 + cross * 0.55;
        color = vec3(1.0, 0.84, 0.48) + spectral(time * 1.5 + a) * 0.22;
    } else if (EffectMode == 3) {
        float crown = ring(d + 0.055 * sin(a * 5.0 + time * 1.8), 0.68, 0.042) * arc(a, 16.0, 19.0);
        float stones = pow(abs(sin(a * 9.0 + time * 0.7)), 24.0) * ring(d, 0.72, 0.12);
        float nebula = (1.0 - smoothstep(0.15, 0.84, d)) * (0.38 + 0.62 * sin(p.x * 9.0 + p.y * 13.0 + time * 2.2));
        float star = pow(max(0.0, 1.0 - d), 4.2);
        mask = crown * 1.08 + stones * 0.75 + nebula * 0.35 + star * 0.62;
        color = spectral(time * 2.0 + d * 4.0 + a * 0.4) + vec3(0.08, 0.06, 0.36);
    } else if (EffectMode == 4) {
        vec2 moonP = p + vec2(0.10 * sin(time * 0.9), 0.02);
        float moon = 1.0 - smoothstep(0.66, 0.69, length(moonP));
        float bite = 1.0 - smoothstep(0.50, 0.54, length(moonP + vec2(0.28, -0.03)));
        float crescent = moon * (1.0 - bite * 0.75);
        float rim = ring(length(moonP), 0.66, 0.055);
        float cloud = 0.58 + 0.42 * sin(p.x * 8.0 + p.y * 5.5 + time * 2.0);
        mask = crescent * (0.55 + cloud * 0.35) + rim * 0.75;
        color = mix(vec3(0.28, 0.0, 0.015), vec3(1.0, 0.05, 0.06), crescent + rim);
    } else if (EffectMode == 5) {
        float link = ring(abs(p.y) + 0.05 * sin(p.x * 18.0 + time * 5.0), 0.22, 0.045);
        float broken = step(0.22, hash(floor(uv.x * 28.0 + time * 4.0)));
        float core = 1.0 - smoothstep(0.0, 0.55, abs(p.y));
        mask = (link * 1.15 + core * 0.20) * broken * smoothstep(0.02, 0.15, uv.x) * (1.0 - smoothstep(0.86, 1.0, uv.x));
        color = vec3(0.58, 0.20, 1.0) + spectral(time * 2.1 + uv.x * 3.0) * 0.22;
    } else if (EffectMode == 6) {
        vec2 r = vec2(p.x * 0.82 + p.y * 0.20, p.y * 1.10 - p.x * 0.08);
        float vertical = crossBar(r, vec2(0.0, -0.12), vec2(0.070, 0.78));
        float horizontal = crossBar(r, vec2(0.0, 0.24), vec2(0.46, 0.065));
        float crack = pow(abs(sin(r.y * 18.0 + time * 6.0)), 16.0) * (vertical + horizontal);
        float aura = ring(d, 0.58, 0.11) * 0.34;
        mask = vertical * 1.05 + horizontal * 0.92 + crack * 0.48 + aura;
        color = mix(vec3(0.24, 0.0, 0.36), vec3(0.92, 0.10, 1.0), vertical + horizontal) + vec3(0.45, 0.0, 0.10) * crack;
    } else if (EffectMode == 7) {
        float glow = pow(max(0.0, 1.0 - smoothstep(0.04, 1.0, d)), 1.7);
        float halo = ring(d, 0.72, 0.24) * 0.45;
        mask = (glow * 0.34 + halo) * (0.72 + 0.28 * sin(time * 4.0 + d * 5.0));
        color = base + spectral(time + d * 2.5) * 0.20;
    } else {
        vec2 q = p;
        q.x *= 1.40;
        float shard = 1.0 - smoothstep(0.06, 0.82, length(q));
        float hot = 1.0 - smoothstep(0.00, 0.18, length(q));
        mask = (shard + hot * 0.55) * (0.70 + 0.30 * sin(time * 8.0 + uv.x * 7.0));
        color = base + spectral(time * 1.8 + uv.x * 5.0) * hot;
    }

    mask = clamp(mask, 0.0, 1.65);
    fragColor = vec4(color * mask * intensity, alpha * mask) * ColorModulator;
}
