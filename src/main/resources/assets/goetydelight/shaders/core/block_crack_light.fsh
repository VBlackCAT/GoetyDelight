#version 150

uniform vec4 ColorModulator;
uniform float intensity;
uniform int EffectMode;

in vec4 vertexColor;
in vec2 texCoord;
in float time;

out vec4 fragColor;

vec3 spectral(float phase) {
    return 0.56 + 0.44 * cos(phase + vec3(0.0, 2.0943952, 4.1887903));
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(41.31, 289.97))) * 43758.5453);
}

void main() {
    vec2 uv = texCoord;
    vec3 base = vertexColor.rgb;
    float alpha = vertexColor.a;
    float mask;
    vec3 color;

    if (EffectMode == 0) {
        float center = abs(uv.y - 0.5) * 2.0;
        float core = 1.0 - smoothstep(0.0, 0.28, center);
        float bloom = 1.0 - smoothstep(0.10, 1.0, center);
        float broken = step(0.18, hash(floor(vec2(uv.x * 34.0 + time * 1.7, uv.y * 5.0))));
        float pulse = 0.72 + 0.28 * sin(time * 6.3 + uv.x * 19.0 + hash(vec2(uv.x)) * 3.0);
        float endFade = smoothstep(0.0, 0.08, uv.x) * (1.0 - smoothstep(0.88, 1.0, uv.x));
        mask = (core * 1.4 + bloom * 0.45) * mix(0.38, 1.0, broken) * pulse * endFade;
        color = mix(base, spectral(time * 2.2 + uv.x * 4.5), 0.48) + vec3(0.35, 0.55, 0.95) * core;
    } else {
        vec2 p = uv * 2.0 - 1.0;
        float d = length(p);
        float core = 1.0 - smoothstep(0.02, 0.82, d);
        float ring = (1.0 - smoothstep(0.55, 1.0, d)) * smoothstep(0.15, 0.0, d) * 0.65;
        float sparkle = 0.62 + 0.38 * sin(time * 10.0 + uv.x * 21.0 + uv.y * 13.0);
        mask = (core + ring) * sparkle;
        color = mix(base, spectral(time * 2.8 + uv.x * 7.0 + uv.y * 4.0), 0.62) + vec3(0.30, 0.62, 1.0) * core;
    }

    fragColor = vec4(color * mask * intensity, alpha * mask) * ColorModulator;
}
