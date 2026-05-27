#version 150

uniform vec4 ColorModulator;
uniform float intensity;
uniform int EffectMode;

in vec4 vertexColor;
in vec2 texCoord;
in float time;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(41.17, 173.31))) * 43758.5453);
}

float pulse(float offset) {
    float fast = 0.72 + 0.28 * sin(time * 17.0 + offset);
    float snap = smoothstep(0.72, 1.0, sin(time * 3.9 + offset) * 0.5 + 0.5);
    return fast + snap * 0.42;
}

void main() {
    vec2 uv = texCoord;
    vec2 p = uv * 2.0 - 1.0;
    vec3 base = vertexColor.rgb;
    float alpha = vertexColor.a;
    vec3 color;
    float mask;

    if (EffectMode == 0) {
        p.x *= 1.28;
        float d = length(p);
        float core = 1.0 - smoothstep(0.02, 0.35, d);
        float ember = 1.0 - smoothstep(0.12, 0.82, d);
        float slit = (1.0 - smoothstep(0.015, 0.155, abs(p.y))) * (1.0 - smoothstep(0.15, 0.95, abs(p.x)));
        float rim = smoothstep(0.52, 0.25, d) * smoothstep(0.08, 0.38, d);
        float flicker = pulse(d * 2.7);
        mask = (core * 1.45 + slit * 0.82 + ember * 0.50 + rim * 0.30) * flicker;
        color = mix(vec3(0.45, 0.0, 0.0), base + vec3(0.95, 0.14, 0.02), core + slit * 0.45);
    } else if (EffectMode == 1) {
        float x = abs(uv.x - 0.5) * 2.0;
        float y = abs(uv.y - 0.5) * 2.0;
        float line = (1.0 - smoothstep(0.00, 0.13, y)) * pow(max(0.0, 1.0 - x), 0.38);
        float bloom = (1.0 - smoothstep(0.05, 0.72, y)) * pow(max(0.0, 1.0 - x), 1.20);
        float centerHot = (1.0 - smoothstep(0.00, 0.18, x)) * (1.0 - smoothstep(0.00, 0.20, y));
        float broken = 0.76 + 0.24 * hash(vec2(floor(uv.x * 42.0 + time * 8.0), floor(uv.y * 5.0)));
        mask = (line * 1.35 + bloom * 0.60 + centerHot * 1.05) * pulse(uv.x * 6.0) * broken;
        color = base + vec3(1.25, 0.05, 0.00) * centerHot + vec3(0.40, 0.02, 0.00) * bloom;
    } else if (EffectMode == 2) {
        p.x *= 0.82;
        float d = length(p);
        float halo = pow(max(0.0, 1.0 - smoothstep(0.0, 1.0, d)), 1.55);
        float smoke = 0.70 + 0.30 * sin(atan(p.y, p.x) * 9.0 + time * 5.0);
        float rays = (1.0 - smoothstep(0.0, 0.24, abs(p.y))) * pow(max(0.0, 1.0 - abs(p.x)), 0.72);
        mask = (halo * smoke + rays * 0.28) * pulse(d);
        color = base * 0.72 + vec3(0.70, 0.02, 0.00) * halo;
    } else if (EffectMode == 3) {
        p.x *= 1.75;
        float d = length(p);
        float star = 1.0 - smoothstep(0.02, 0.85, d);
        float cut = smoothstep(0.92, 0.12, abs(p.y)) * smoothstep(1.00, 0.06, abs(p.x));
        mask = (star + cut * 0.7) * pulse(uv.x * 11.0);
        color = base + vec3(1.00, 0.22, 0.05) * star;
    } else {
        float y = abs(uv.y - 0.5) * 2.0;
        float core = 1.0 - smoothstep(0.00, 0.22, y);
        float glow = 1.0 - smoothstep(0.08, 1.0, y);
        float taper = smoothstep(0.00, 0.08, uv.x) * (1.0 - smoothstep(0.82, 1.0, uv.x));
        float hotTip = 1.0 - smoothstep(0.0, 0.18, uv.x);
        float noise = 0.82 + 0.18 * hash(vec2(floor(uv.x * 28.0 + time * 9.0), floor(uv.y * 4.0)));
        mask = (core * 1.25 + glow * 0.65 + hotTip * 0.35) * taper * pulse(uv.x * 5.0) * noise;
        color = base + vec3(1.10, 0.08, 0.00) * core + vec3(0.30, 0.00, 0.00) * glow;
    }

    mask = clamp(mask, 0.0, 1.65);
    fragColor = vec4(color * mask * intensity, alpha * mask) * ColorModulator;
}
