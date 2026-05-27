#version 150

uniform vec4 ColorModulator;
uniform float intensity;
uniform int EffectMode;

in vec4 vertexColor;
in vec2 texCoord;
in float time;

out vec4 fragColor;

vec3 spectral(float phase) {
    return 0.55 + 0.45 * cos(phase + vec3(0.0, 2.0943952, 4.1887903));
}

void main() {
    vec2 p = texCoord * 2.0 - 1.0;
    float d = length(p);
    vec3 rainbow = spectral(time * 2.4 + texCoord.x * 5.7 + texCoord.y * 3.1);
    vec3 color = mix(vertexColor.rgb, rainbow, 0.45);
    float alpha = vertexColor.a;
    float mask = 1.0;

    if (EffectMode == 0) {
        float rim = smoothstep(0.45, 1.0, d) * (1.0 - smoothstep(1.0, 1.18, d));
        float pulse = 0.72 + 0.28 * sin(time * 4.4 + d * 6.0);
        mask = rim * pulse;
        color += rainbow * rim * 0.8;
    } else if (EffectMode == 1) {
        float edge = 1.0 - smoothstep(0.0, 0.55, abs(texCoord.y - 0.5) * 2.0);
        float scrape = 0.65 + 0.35 * sin(time * 9.0 + texCoord.x * 36.0);
        mask = edge * scrape;
        color += vec3(0.35, 0.8, 1.0) * edge;
    } else if (EffectMode == 2) {
        float soft = 1.0 - smoothstep(0.05, 1.0, d);
        float speck = 0.72 + 0.28 * sin(time * 13.0 + texCoord.x * 31.0 + texCoord.y * 17.0);
        mask = soft * soft * speck;
        color = mix(color, rainbow + vec3(0.18, 0.32, 0.45), 0.5);
    } else if (EffectMode == 3) {
        float ring = 1.0 - smoothstep(0.035, 0.16, abs(d - 0.72));
        float fade = 1.0 - smoothstep(0.84, 1.05, d);
        mask = ring * fade;
        color = rainbow + vec3(0.20, 0.42, 0.60);
    } else if (EffectMode == 4) {
        float wave = sin((1.0 - d) * 26.0 + time * 10.0) * 0.5 + 0.5;
        float heat = (1.0 - smoothstep(0.1, 1.0, d)) * (0.45 + wave * 0.55);
        mask = heat;
        color = mix(vec3(0.15, 0.75, 1.0), vec3(1.0, 0.24, 0.82), wave);
    } else if (EffectMode == 5) {
        float side = 1.0 - smoothstep(0.62, 1.0, abs(p.x));
        float heightFade = smoothstep(0.0, 0.16, texCoord.y) * (1.0 - smoothstep(0.76, 1.0, texCoord.y));
        float bands = 0.70 + 0.30 * sin(texCoord.y * 42.0 - time * 5.0);
        mask = side * heightFade * bands;
        color = mix(color, rainbow + vec3(0.12, 0.28, 0.50), 0.65);
    } else {
        float ring = 1.0 - smoothstep(0.025, 0.085, abs(d - 0.78));
        float scan = smoothstep(0.0, 0.08, texCoord.y) * (1.0 - smoothstep(0.92, 1.0, texCoord.y));
        mask = max(ring, scan * 0.35);
        color = rainbow + vec3(0.35, 0.2, 0.55);
    }

    fragColor = vec4(color * mask * intensity, alpha * mask) * ColorModulator;
}
