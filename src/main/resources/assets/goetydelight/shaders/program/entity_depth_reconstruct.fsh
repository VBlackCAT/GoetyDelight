#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 ViewProjMat;
uniform mat4 InvViewProjMat;
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

float ring(float value, float target, float width) {
    return 1.0 - smoothstep(width * 0.45, width, abs(value - target));
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
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

vec3 reconstructWorldPosition(vec2 uv, float depth) {
    vec4 clip = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 world = InvViewProjMat * clip;
    float invW = abs(world.w) > 0.000001 ? 1.0 / world.w : 1.0;
    return world.xyz * invW;
}

vec2 cameraRelativeWorldToUv(vec3 relWorld) {
    vec4 clip = ViewProjMat * vec4(relWorld, 1.0);
    if (clip.w <= 0.000001) {
        return vec2(-1000.0);
    }

    return clip.xy / clip.w * 0.5 + 0.5;
}

vec2 projectedWorldOffset(vec3 origin, vec3 worldOffset) {
    vec2 originUv = cameraRelativeWorldToUv(origin);
    vec2 targetUv = cameraRelativeWorldToUv(origin + worldOffset);
    vec2 offset = targetUv - originUv;
    float len = length(offset);
    if (len > 0.050) {
        offset *= 0.050 / len;
    }

    return offset;
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

vec3 applyDepthRefractionPressure(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float height = max(data.z, 0.5);
    float intensity = max(data.w, 0.0);
    vec3 delta = scenePos - center;
    float dist = length(delta);
    vec3 horizontalDelta = vec3(delta.x, 0.0, delta.z);
    float horizontalLength = length(horizontalDelta);
    vec3 radialWorld = horizontalLength > 0.0001 ? horizontalDelta / horizontalLength : vec3(1.0, 0.0, 0.0);
    vec3 tangentWorld = vec3(-radialWorld.z, 0.0, radialWorld.x);

    float body = 1.0 - smoothstep(radius * 0.18, radius * 1.05, dist);
    float vertical = 1.0 - smoothstep(height * 0.55, height * 1.35, abs(delta.y));
    float shell = ring(length(delta.xz), radius * 0.64, radius * 0.20) * vertical;
    float innerFog = 1.0 - smoothstep(radius * 0.18, radius * 0.95, length(delta.xz));
    float contact = edge * (1.0 - smoothstep(radius * 0.30, radius * 1.32, dist));
    float stableNoise = valueNoise(delta.xz * 1.85 + vec2(Time * 0.42, -Time * 0.31));
    float fineNoise = valueNoise(delta.xz * 4.20 + delta.yy * 0.35 + vec2(-Time * 0.85, Time * 0.58));
    float wave = sin(dist * 10.5 - Time * 8.2 + stableNoise * 2.4)
            + cos(delta.y * 5.8 + length(delta.xz) * 3.2 + Time * 3.4);
    float swirl = sin(horizontalLength * 12.0 + delta.y * 2.2 - Time * 6.0 + stableNoise * 6.28318);
    float verticalWave = sin(delta.y * 4.0 + horizontalLength * 1.7 + Time * 2.3);
    float pressure = (body * 0.42 + shell * 0.72 + contact * 0.86 + innerFog * vertical * 0.28) * intensity;
    vec3 worldOffset = radialWorld * (wave * radius * 0.040)
            + tangentWorld * (swirl * radius * 0.022)
            + vec3(0.0, 1.0, 0.0) * (verticalWave * height * 0.014)
            + radialWorld * (cos(horizontalLength * 3.6 - Time * 2.0) * radius * 0.010);
    vec2 distortion = projectedWorldOffset(scenePos, worldOffset) * pressure;
    vec2 heatDrift = projectedWorldOffset(scenePos, vec3(0.0, verticalWave * height * 0.020, 0.0)) * pressure * 0.55;
    vec2 refractUv = uv + distortion + heatDrift;
    vec3 refracted = texture(DiffuseSampler, clamp(refractUv, vec2(0.0), vec2(1.0))).rgb;

    float fog = (body * 0.30 + innerFog * vertical * 0.38 + shell * 0.26 + contact * 0.42)
            * (0.58 + stableNoise * 0.30 + fineNoise * 0.22) * intensity;
    fog *= 1.0 - smoothstep(radius * 1.20, radius * 1.75, dist);

    vec3 rim = spectral(Time * 2.1 + dist * 1.8 + edge * 3.0) + tint * 0.35;
    vec3 fogColor = mix(vec3(0.10, 0.16, 0.22), rim, 0.48);
    vec3 distorted = mix(color, refracted, clamp(pressure * 0.46, 0.0, 0.72));
    distorted = mix(distorted, fogColor, clamp(fog * 0.34, 0.0, 0.55));
    return distorted + rim * (pressure * 0.16 + fog * 0.12);
}

void main() {
    vec2 uv = texCoord;
    vec4 base = texture(DiffuseSampler, uv);
    float depth = depthAt(uv);

    if (depth >= 0.999999) {
        fragColor = base;
        return;
    }

    vec3 scenePos = reconstructWorldPosition(uv, depth);
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
        } else if (mode == 5) {
            color = applyVolumetricLightColumn(color, scenePos, center, data, tint, edge);
        } else {
            color = applyDepthRefractionPressure(color, scenePos, uv, center, data, tint, edge);
        }
    }

    fragColor = vec4(color, base.a);
}
