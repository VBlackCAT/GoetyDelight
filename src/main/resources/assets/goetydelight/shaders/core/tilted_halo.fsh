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

float ring(float radius, float target, float width) {
    return 1.0 - smoothstep(width * 0.45, width, abs(radius - target));
}

float arcGate(float angle, float cells, float offset) {
    float id = floor((angle + 3.1415926) / 6.2831853 * cells + offset);
    return step(0.26, hash(id * 19.73 + offset * 7.11));
}

vec3 haloColor(float phase) {
    vec3 gold = vec3(1.00, 0.73, 0.28);
    vec3 cyan = vec3(0.30, 0.92, 1.00);
    vec3 rose = vec3(1.00, 0.28, 0.82);
    return mix(mix(gold, cyan, 0.34 + 0.22 * sin(phase)), rose, 0.14 + 0.10 * cos(phase * 1.7));
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
        float spinA = angle + time * 1.25;
        float spinB = angle - time * 0.82;
        float outer = ring(radius, 0.76, 0.035) * arcGate(spinA, 28.0, 1.0);
        float inner = ring(radius, 0.49, 0.026) * (0.55 + 0.45 * arcGate(spinB, 18.0, 3.0));
        float crescent = ring(radius + 0.045 * sin(angle * 3.0 - time * 1.9), 0.64, 0.030)
                * smoothstep(-0.82, 0.35, sin(angle + time * 0.42));
        float spokes = pow(abs(sin(angle * 6.0 - time * 2.6)), 34.0)
                * smoothstep(0.18, 0.72, radius)
                * (1.0 - smoothstep(0.73, 0.92, radius));
        float star = pow(max(0.0, 1.0 - radius), 2.7)
                * (0.18 + 0.18 * sin(angle * 5.0 + time * 3.1));
        float coreVoid = smoothstep(0.19, 0.38, radius);
        mask = (outer * 1.15 + inner * 0.75 + crescent * 0.90 + spokes * 0.58 + star) * coreVoid;
        color = haloColor(time * 1.6 + angle * 0.7) + base * 0.35;
    } else if (EffectMode == 1) {
        float glow = pow(max(0.0, 1.0 - smoothstep(0.16, 1.0, radius)), 1.65);
        float aura = ring(radius, 0.72, 0.26) * 0.42;
        float pulse = 0.72 + 0.28 * sin(time * 4.2 + radius * 7.0);
        mask = (glow * 0.38 + aura) * pulse;
        color = haloColor(time * 1.2 + radius * 2.4) + vec3(0.28, 0.16, 0.02);
    } else {
        vec2 q = p;
        q.x *= 1.35;
        float d = length(q);
        float shard = 1.0 - smoothstep(0.08, 0.92, d);
        float hot = 1.0 - smoothstep(0.00, 0.22, d);
        float line = (1.0 - smoothstep(0.0, 0.18, abs(p.y))) * (1.0 - smoothstep(0.15, 1.0, abs(p.x)));
        float pulse = 0.68 + 0.32 * sin(time * 9.0 + uv.x * 8.0);
        mask = (shard + hot * 0.75 + line * 0.36) * pulse;
        color = mix(base, haloColor(time * 2.0 + uv.x * 5.0), 0.68) + vec3(0.32, 0.42, 0.70) * hot;
    }

    mask = clamp(mask, 0.0, 1.45);
    fragColor = vec4(color * mask * intensity, alpha * mask) * ColorModulator;
}
