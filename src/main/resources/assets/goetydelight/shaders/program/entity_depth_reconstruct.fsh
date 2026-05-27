#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 InvProjMat;
uniform vec3 CameraLeft;
uniform vec3 CameraUp;
uniform vec3 CameraLook;
uniform vec2 InSize;
uniform float Time;
uniform int EffectCount;
uniform vec3 EffectCenter0;
uniform vec3 EffectCenter1;
uniform vec3 EffectCenter2;
uniform vec3 EffectCenter3;
uniform vec3 EffectCenter4;
uniform vec3 EffectCenter5;
uniform vec3 EffectCenter6;
uniform vec3 EffectCenter7;
uniform vec4 EffectData0;
uniform vec4 EffectData1;
uniform vec4 EffectData2;
uniform vec4 EffectData3;
uniform vec4 EffectData4;
uniform vec4 EffectData5;
uniform vec4 EffectData6;
uniform vec4 EffectData7;
uniform vec3 EffectColor0;
uniform vec3 EffectColor1;
uniform vec3 EffectColor2;
uniform vec3 EffectColor3;
uniform vec3 EffectColor4;
uniform vec3 EffectColor5;
uniform vec3 EffectColor6;
uniform vec3 EffectColor7;

in vec2 texCoord;

out vec4 fragColor;

vec3 spectral(float phase) {
    return 0.55 + 0.45 * cos(phase + vec3(0.0, 2.0943952, 4.1887903));
}

vec3 effectCenter(int index) {
    if (index == 0) return EffectCenter0;
    if (index == 1) return EffectCenter1;
    if (index == 2) return EffectCenter2;
    if (index == 3) return EffectCenter3;
    if (index == 4) return EffectCenter4;
    if (index == 5) return EffectCenter5;
    if (index == 6) return EffectCenter6;
    return EffectCenter7;
}

vec4 effectData(int index) {
    if (index == 0) return EffectData0;
    if (index == 1) return EffectData1;
    if (index == 2) return EffectData2;
    if (index == 3) return EffectData3;
    if (index == 4) return EffectData4;
    if (index == 5) return EffectData5;
    if (index == 6) return EffectData6;
    return EffectData7;
}

vec3 effectColor(int index) {
    if (index == 0) return EffectColor0;
    if (index == 1) return EffectColor1;
    if (index == 2) return EffectColor2;
    if (index == 3) return EffectColor3;
    if (index == 4) return EffectColor4;
    if (index == 5) return EffectColor5;
    if (index == 6) return EffectColor6;
    return EffectColor7;
}

vec3 reconstructViewPosition(vec2 uv, float depth) {
    vec4 clip = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 view = InvProjMat * clip;
    float invW = abs(view.w) > 0.000001 ? 1.0 / view.w : 1.0;
    return view.xyz * invW;
}

vec3 viewToCameraRelativeWorld(vec3 viewPos) {
    return CameraLeft * viewPos.x + CameraUp * viewPos.y - CameraLook * viewPos.z;
}

float depthAt(vec2 uv) {
    return texture(DepthSampler, clamp(uv, vec2(0.0), vec2(1.0))).r;
}

float depthEdge(vec2 uv, float centerDepth) {
    vec2 texel = 1.0 / InSize;
    float l = depthAt(uv - vec2(texel.x, 0.0));
    float r = depthAt(uv + vec2(texel.x, 0.0));
    float u = depthAt(uv - vec2(0.0, texel.y));
    float d = depthAt(uv + vec2(0.0, texel.y));
    float raw = abs(centerDepth - l) + abs(centerDepth - r) + abs(centerDepth - u) + abs(centerDepth - d);
    return smoothstep(0.00035, 0.0065, raw);
}

vec3 applyShockwave(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint) {
    float radius = max(data.y, 0.001);
    float progress = clamp(data.z, 0.0, 1.0);
    float intensity = max(data.w, 0.0);
    float distanceToCenter = length(scenePos - center);
    float bandWidth = max(radius * 0.055, 0.16);
    float ring = 1.0 - smoothstep(0.0, bandWidth, abs(distanceToCenter - radius));
    float innerWake = exp(-abs(distanceToCenter - radius * 0.72) * 3.2) * 0.22;
    float ripple = 0.5 + 0.5 * sin(distanceToCenter * 14.0 - Time * 18.0);
    vec3 rainbow = spectral(Time * 3.6 + distanceToCenter * 1.3 + uv.x * 5.0);
    float energy = (ring * (0.65 + ripple * 0.35) + innerWake) * intensity * (1.0 - progress * 0.18);
    return color + (rainbow + tint) * energy * 0.55;
}

vec3 applyHeatwave(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint) {
    float radius = max(data.y, 0.001);
    float intensity = max(data.w, 0.0);
    vec3 delta = scenePos - center;
    float dist = length(delta);
    float field = 1.0 - smoothstep(radius * 0.18, radius, dist);
    float behindCenter = smoothstep(-0.35, 0.9, center.z - scenePos.z);
    float wave = sin(delta.y * 7.0 + dist * 5.0 - Time * 8.0) * cos(delta.x * 4.0 + Time * 3.1);
    vec2 direction = normalize(delta.xy + vec2(0.001, -0.001));
    vec2 refractUv = uv + direction * wave * field * behindCenter * intensity * 0.012;
    vec3 refracted = texture(DiffuseSampler, clamp(refractUv, vec2(0.0), vec2(1.0))).rgb;
    vec3 rainbow = spectral(Time * 2.8 + dist * 2.1);
    float shimmer = field * behindCenter * (0.35 + 0.65 * abs(wave)) * intensity;
    return mix(color, refracted, shimmer * 0.45) + (rainbow + tint) * shimmer * 0.18;
}

vec3 applyOutlineScan(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float progress = fract(data.z + Time * 0.18);
    float intensity = max(data.w, 0.0);
    vec3 delta = scenePos - center;
    float dist = length(delta);
    float field = 1.0 - smoothstep(radius * 0.55, radius * 1.65, dist);
    float scanHeight = mix(-radius, radius, progress);
    float scan = 1.0 - smoothstep(0.0, max(radius * 0.12, 0.18), abs(delta.y - scanHeight));
    vec3 rainbow = spectral(Time * 4.0 + delta.y * 2.5 + edge * 2.0);
    float glow = field * intensity * max(edge * 0.85, scan * 0.55);
    return color + (rainbow + tint) * glow * 0.55;
}

vec3 applyDepthOccludedHalo(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float progress = clamp(data.z, 0.0, 1.0);
    float intensity = max(data.w, 0.0);
    vec3 delta = scenePos - center;
    float dist = length(delta);
    float shell = 1.0 - smoothstep(radius * 0.82, radius * 2.65, dist);
    float coreCut = smoothstep(radius * 0.25, radius * 0.92, dist);
    float occludedRim = edge * smoothstep(radius * 0.55, radius * 2.6, dist);
    float pulse = 0.7 + 0.3 * sin(Time * 5.0 + dist * 3.2 + progress * 6.28318);
    vec3 rainbow = spectral(Time * 2.2 + dist * 1.4 + uv.y * 4.0);
    float glow = (shell * coreCut * 0.32 + occludedRim * 0.92) * pulse * intensity;
    return color + (rainbow + tint) * glow * 0.45;
}

vec3 applyContactEdgeGlow(vec3 color, vec3 scenePos, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float height = max(data.z, 0.25);
    float intensity = max(data.w, 0.0);
    vec3 delta = scenePos - center;
    float horizontal = length(delta.xz);
    float radial = 1.0 - smoothstep(radius * 0.62, radius * 1.38, horizontal);
    float lowBand = 1.0 - smoothstep(0.0, max(0.28, height * 0.22), abs(delta.y));
    float scrape = 0.65 + 0.35 * sin(Time * 9.0 + horizontal * 18.0);
    float glow = radial * lowBand * max(edge, 0.22) * scrape * intensity;
    vec3 rainbow = spectral(Time * 3.4 + horizontal * 4.5);
    return color + (rainbow + tint + vec3(0.12, 0.32, 0.55)) * glow * 0.42;
}

vec3 applyVolumetricLightColumn(vec3 color, vec3 scenePos, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float height = max(data.z, 0.5);
    float intensity = max(data.w, 0.0);
    vec3 delta = scenePos - center;
    float radial = 1.0 - smoothstep(radius * 0.25, radius * 1.8, length(delta.xz));
    float vertical = smoothstep(0.0, height * 0.12, delta.y) * (1.0 - smoothstep(height * 0.72, height, delta.y));
    float bands = 0.58 + 0.42 * sin(delta.y * 8.0 - Time * 5.0 + length(delta.xz) * 3.5);
    float contact = edge * (1.0 - smoothstep(height * 0.05, height * 0.35, abs(delta.y)));
    float glow = (radial * vertical * (0.45 + 0.55 * bands) + contact * 0.55) * intensity;
    vec3 rainbow = spectral(Time * 2.0 + delta.y * 0.9 + length(delta.xz) * 2.0);
    return color + (rainbow + tint + vec3(0.08, 0.18, 0.34)) * glow * 0.30;
}

void main() {
    vec2 uv = texCoord;
    vec4 base = texture(DiffuseSampler, uv);
    float depth = depthAt(uv);

    if (depth >= 0.999999) {
        fragColor = base;
        return;
    }

    vec3 viewPos = reconstructViewPosition(uv, depth);
    vec3 scenePos = viewToCameraRelativeWorld(viewPos);
    vec3 color = base.rgb;
    float edge = depthEdge(uv, depth);

    for (int i = 0; i < 8; i++) {
        if (i >= EffectCount) {
            break;
        }

        vec4 data = effectData(i);
        int mode = int(data.x + 0.5);
        vec3 center = effectCenter(i);
        vec3 tint = effectColor(i);

        if (mode == 0) {
            color = applyShockwave(color, scenePos, uv, center, data, tint);
        } else if (mode == 1) {
            color = applyHeatwave(color, scenePos, uv, center, data, tint);
        } else if (mode == 2) {
            color = applyOutlineScan(color, scenePos, uv, center, data, tint, edge);
        } else if (mode == 3) {
            color = applyDepthOccludedHalo(color, scenePos, uv, center, data, tint, edge);
        } else if (mode == 4) {
            color = applyContactEdgeGlow(color, scenePos, center, data, tint, edge);
        } else {
            color = applyVolumetricLightColumn(color, scenePos, center, data, tint, edge);
        }
    }

    fragColor = vec4(color, base.a);
}
