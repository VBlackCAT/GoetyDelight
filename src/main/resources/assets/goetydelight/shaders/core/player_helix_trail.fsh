#version 150

uniform vec4 ColorModulator;
uniform float intensity;
uniform int EffectMode;

in vec4 vertexColor;
in vec2 texCoord;
in float time;

out vec4 fragColor;

void main() {
    vec3 color = vertexColor.rgb;
    float alpha = vertexColor.a;

    if (EffectMode == 0) {
        float edge = abs(texCoord.y - 0.5) * 2.0;
        float core = 1.0 - smoothstep(0.18, 1.0, edge);
        float shimmer = 0.70 + 0.30 * sin(time * 8.0 + texCoord.x * 22.0);
        vec3 hot = color + vec3(0.45, 0.35, 0.80) * core;
        fragColor = vec4(hot * core * shimmer * intensity, alpha * core) * ColorModulator;
    } else {
        vec2 p = texCoord * 2.0 - 1.0;
        float d = length(p);
        float core = 1.0 - smoothstep(0.0, 0.92, d);
        float ring = smoothstep(0.78, 0.25, d) * 0.34;
        float twinkle = 0.72 + 0.28 * sin(time * 11.0 + texCoord.x * 17.0 + texCoord.y * 13.0);
        vec3 hot = color * (core + ring) + vec3(0.55, 0.80, 1.00) * core * 0.45;
        fragColor = vec4(hot * twinkle * intensity, alpha * core) * ColorModulator;
    }
}
