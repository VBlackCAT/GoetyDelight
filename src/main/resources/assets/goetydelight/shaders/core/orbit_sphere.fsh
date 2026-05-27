#version 150

uniform vec4 ColorModulator;
uniform float intensity;
uniform int GlowMode;

in vec4 vertexColor;
in vec3 localNormal;
in float time;

out vec4 fragColor;

void main() {
    vec3 n = normalize(localNormal);
    vec3 lightDir = normalize(vec3(-0.35, 0.72, 0.60));
    vec3 viewDir = vec3(0.0, 0.0, 1.0);

    vec3 flow = 0.5 + 0.5 * cos(time * 3.0 + n.xyx * 4.0 + vec3(0.0, 2.1, 4.2));
    vec3 baseColor = mix(vertexColor.rgb, flow, 0.45);

    float diffuse = max(dot(n, lightDir), 0.0);
    float backLight = max(dot(n, -lightDir), 0.0);
    float specular = pow(max(dot(reflect(-lightDir, n), viewDir), 0.0), 24.0);
    float rim = pow(1.0 - max(dot(n, viewDir), 0.0), 2.4);

    if (GlowMode == 1) {
        vec3 glowColor = mix(vec3(0.20, 0.75, 1.00), vec3(1.00, 0.25, 0.85), 0.5 + 0.5 * sin(time * 2.0));
        float glow = pow(1.0 - abs(dot(n, viewDir)), 1.45);
        float pulse = 0.72 + 0.28 * sin(time * 4.0);
        fragColor = vec4(glowColor * glow * pulse * intensity, glow * 0.48) * ColorModulator;
    } else {
        vec3 litColor = baseColor * (0.30 + diffuse * 0.78 + backLight * 0.18);
        litColor += vec3(1.0, 0.92, 0.75) * specular * 0.42;
        litColor += baseColor * rim * 0.26;
        fragColor = vec4(litColor, vertexColor.a) * ColorModulator;
    }
}
