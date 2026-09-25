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

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += amplitude * valueNoise(p);
        p *= 2.17;
        amplitude *= 0.52;
    }
    return value;
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

vec3 viewRay(vec2 uv) {
    vec4 clip = vec4(uv * 2.0 - 1.0, 1.0, 1.0);
    vec4 world = InvViewProjMat * clip;
    float invW = abs(world.w) > 0.000001 ? 1.0 / world.w : 1.0;
    return normalize(world.xyz * invW);
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


vec3 applyMalevolentShrineDomain(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint) {
    float radius = max(data.y, 0.001);
    float height = max(data.z, 1.0);
    float intensity = max(data.w, 0.0);

    vec3 ro = vec3(0.0);
    vec3 rd = normalize(scenePos);
    float maxDist = length(scenePos);
    float boundRadius = length(vec3(radius, height, radius));

    vec3 toCenter = center - ro;
    float centerProj = dot(rd, toCenter);
    float tNear = max(centerProj - boundRadius, 0.0);
    float tFar = min(centerProj + boundRadius, maxDist);
    if (tNear >= tFar) {
        return color;
    }

    float span = max(tFar - tNear, 0.001);
    float stepSize = span / 34.0;
    vec3 fogColor = mix(vec3(0.010, 0.0, 0.005), vec3(0.30, 0.010, 0.020), 0.45);
    float transmittance = 1.0;
    vec3 fog = vec3(0.0);

    for (int i = 0; i < 34; i++) {
        float t = tNear + (float(i) + 0.5) * stepSize;
        vec3 p = ro + rd * t;
        vec3 delta = p - center;
        float horizontal = length(delta.xz);

        float inside = 1.0 - smoothstep(radius * 0.68, radius, horizontal);
        float vertical = smoothstep(-0.10, height * 0.18, delta.y)
                * (1.0 - smoothstep(height * 0.60, height * 1.20, delta.y));
        float body = inside * vertical;
        if (body <= 0.001) {
            continue;
        }

        // 近处稀薄，远处逐渐厚重，边缘浓到足以吞掉天空。
        float nearFade = smoothstep(0.35, radius * 0.52, t);
        float farBoost = smoothstep(radius * 0.42, radius * 0.94, horizontal);
        float distanceFactor = nearFade * (0.70 + farBoost * 0.75);

        vec2 flow = vec2(Time * 0.06, -Time * 0.038);
        vec2 q = delta.xz * 0.46 + flow;
        float n = fbm(q);
        float n2 = fbm(q * 2.3 - flow * 1.4 + delta.y * 0.18);
        float warp = fbm(q + vec2(n * 1.7 - n2 * 1.2, n2 * 1.5 + n * 0.8));
        float angle = atan(delta.z, delta.x) * 5.0 + warp * 6.5 + Time * 0.18;
        float strand = pow(1.0 - abs(sin(angle + n * 2.6)), 11.0);
        float verticalWisp = pow(1.0 - abs(sin(delta.y * 1.7 + fbm(q) * 3.6 - Time * 0.2)), 5.0);
        float density = body * (0.05 + 1.9 * strand * (0.22 + 0.78 * n) * (0.40 + 0.60 * verticalWisp));
        density *= distanceFactor;

        float wall = (1.0 - smoothstep(radius * 0.82, radius, horizontal)) * vertical;
        density += wall * (0.12 + 0.55 * n2) * (0.5 + 0.5 * strand) * nearFade;

        density *= intensity * 0.72;
        float sampleOpacity = 1.0 - exp(-density * stepSize * 2.2);

        vec3 sheen = spectral(Time * 0.75 + warp * 4.2);
        vec3 sampleFog = mix(fogColor, sheen, clamp(strand * 0.45 + wall * 0.12, 0.0, 0.55));
        sampleFog = mix(sampleFog, vec3(0.018, 0.0, 0.009), 0.16);

        fog += sampleFog * sampleOpacity * transmittance;
        transmittance *= 1.0 - sampleOpacity;

        if (transmittance < 0.02) {
            break;
        }
    }

    float fogAmount = 1.0 - transmittance;
    vec3 integratedFog = fog / max(fogAmount, 0.001);
    vec3 result = mix(color, integratedFog, clamp(fogAmount, 0.0, 0.94));
    result *= 1.0 - fogAmount * intensity * 0.10;
    return result;
}

vec3 applyMalevolentShrineSlash(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint) {
    float radius = max(data.y, 0.001);
    float height = max(data.z, 1.0);
    float intensity = max(data.w, 0.0);
    vec3 p = scenePos - center;
    float horizontal = length(p.xz);
    float inside = 1.0 - smoothstep(radius * 0.80, radius, horizontal);
    float vertical = smoothstep(-0.15, height * 0.10, p.y) * (1.0 - smoothstep(height * 0.72, height * 1.22, p.y));
    float field = inside * vertical;

    float cuts = 0.0;
    float glow = 0.0;
    for (int i = 0; i < 8; i++) {
        float angle = float(i) * 0.785398 + Time * 0.075;
        vec3 dir = normalize(vec3(cos(angle), sin(angle * 1.73 + Time * 0.31) * 0.42, sin(angle)));
        float along = dot(p, dir);
        float perpendicular = length(p - dir * along);
        float travel = abs(fract(along * 0.42 - Time * 0.95) - 0.5);
        float line = 1.0 - smoothstep(0.012, 0.10, perpendicular);
        float fade = 1.0 - smoothstep(0.0, radius * 0.72, abs(along));
        cuts += line * travel * fade;
        glow += line * fade * 0.12;
    }

    float ring1 = ring(length(p.xz), radius * (0.18 + 0.16 * fract(Time * 0.41)), radius * 0.035);
    float ring2 = ring(length(p.xz), radius * (0.55 + 0.22 * fract(Time * 0.27 + 0.5)), radius * 0.025);
    float ripple = (ring1 * 0.5 + ring2 * 0.7) * field;

    vec3 rift = mix(vec3(0.0), vec3(0.55, 0.006, 0.016), clamp(cuts * intensity * 1.7, 0.0, 0.82));
    vec3 fractured = mix(color, rift, clamp(cuts * intensity * 0.85, 0.0, 0.68));
    fractured += tint * (glow + ripple) * intensity * 1.1;
    return fractured;
}

float fireBayerDither(vec2 uv) {
    return fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 applyMalevolentShrineFire(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float progress = clamp(data.z, 0.0, 1.0);
    float intensity = max(data.w, 0.0);

    vec3 ro = vec3(0.0);
    vec3 rd = normalize(viewRay(uv));

    float expand = mix(0.18, 1.0, smoothstep(0.0, 0.35, progress));
    float decay = 1.0 - smoothstep(0.62, 1.0, progress);
    float height = radius * mix(1.35, 2.5, expand);

    float maxDistance = length(scenePos);

    // 标准 ray-sphere 求交，避免未命中包围球时仍然执行体积积分。
    vec3 boundCenter = center + vec3(0.0, radius, 0.0);
    float boundRadius = radius * 3.0;
    vec3 oc = ro - boundCenter;
    float b = dot(oc, rd);
    float c = dot(oc, oc) - boundRadius * boundRadius;
    float h = b * b - c;

    if (h < 0.0) {
        return color;
    }

    h = sqrt(h);
    float tNear = max(-b - h, 0.0);
    float tFar = min(-b + h, maxDistance);

    if (tNear >= tFar) {
        return color;
    }

    const int STEPS = 64;
    float span = max(tFar - tNear, 0.001);
    float stepSize = span / float(STEPS);
    float dither = fireBayerDither(uv);

    float transmittance = 1.0;
    vec3 accumulated = vec3(0.0);

    for (int i = 0; i < STEPS; i++) {
        float t = tNear + (float(i) + dither) * stepSize;
        vec3 p = ro + rd * t;
        vec3 q = p - center;

        float hNorm = clamp((q.y + radius * 0.4) / max(height, 0.001), 0.0, 1.0);
        vec3 qNorm = q / max(radius, 0.001);

        vec2 uvBase = vec2(length(qNorm.xz) * 1.8, qNorm.y * 1.5 - Time * 3.5);
        float nBase = fbm(uvBase);

        vec2 uvTear = qNorm.xz * 1.5;
        uvTear.x += nBase * 1.5 + Time * 0.8;
        uvTear.y -= nBase * 1.2 - Time * 1.1;
        float nTear = fbm(uvTear + qNorm.y * 1.2);

        float noise = mix(nBase, nTear, 0.6);

        float profile = (1.0 - hNorm * hNorm) * smoothstep(-0.25, 0.35, hNorm);
        float baseRadius = radius * expand * profile;
        float distXZ = length(q.xz);
        float baseMask = 1.0 - smoothstep(0.0, max(baseRadius, 0.001), distXZ);

        // 噪声不得在半径之外泄漏出无限密度的黑烟。
        float shape = baseMask + (noise - 0.5) * 1.4;
        float core = smoothstep(0.0, 0.4, shape);
        core *= smoothstep(radius * expand * 1.5, radius * expand * 1.0, distXZ);

        float vertMask = smoothstep(-radius * 0.5, -radius * 0.1, q.y)
                * (1.0 - smoothstep(height * 0.75, height * 1.1, q.y));

        // 相机进入体积内部时近场淡出，避免被黑烟完整遮住。
        float camFade = smoothstep(0.0, max(radius * 0.2, 0.5), t);
        float density = core * vertMask * intensity * decay * camFade;

        if (density <= 0.01) {
            continue;
        }

        float temp = clamp(baseMask * 0.9 + 0.1, 0.0, 1.0);
        temp *= pow(1.0 - hNorm, 1.4);
        temp *= mix(0.5, 1.2, noise);
        temp = clamp(temp, 0.0, 1.0);

        vec3 cSmoke = vec3(0.03, 0.025, 0.025);
        vec3 cDarkRed = vec3(0.65, 0.06, 0.01);
        vec3 cOrange = vec3(1.0, 0.35, 0.02);
        vec3 cYellow = vec3(1.0, 0.85, 0.15);
        vec3 cWhite = vec3(1.0, 0.98, 0.90);

        vec3 col = cSmoke;
        col = mix(col, cDarkRed, smoothstep(0.05, 0.25, temp));
        col = mix(col, cOrange, smoothstep(0.25, 0.50, temp));
        col = mix(col, cYellow, smoothstep(0.50, 0.75, temp));
        col = mix(col, cWhite, smoothstep(0.75, 1.00, temp));

        float absorption = density * stepSize * mix(14.0, 4.0, temp);
        float alpha = 1.0 - exp(-absorption);

        float emission = pow(max(temp - 0.15, 0.0), 2.2) * 16.0;
        vec3 fireColor = col * (1.0 + emission);
        fireColor += tint * emission * 0.15;

        accumulated += fireColor * alpha * transmittance;
        transmittance *= 1.0 - alpha;

        if (transmittance < 0.015) {
            break;
        }
    }

    vec3 surface = scenePos - center;
    float surfaceDist = length(surface);

    vec2 heatUV = vec2(
        surfaceDist * 3.0 / max(radius, 0.001) - Time * 2.5,
        atan(surface.z, surface.x) * 2.0 + Time
    );
    float heatNoise = fbm(heatUV) * 2.0 - 1.0;

    float heat = decay * intensity
            * smoothstep(radius * 0.2, radius * 1.5, surfaceDist)
            * (1.0 - smoothstep(radius * 0.8, radius * 2.5, surfaceDist));

    vec3 radialDir = surfaceDist > 0.001 ? surface / surfaceDist : vec3(0.0, 1.0, 0.0);
    vec3 tangentDir = vec3(-radialDir.z, 0.0, radialDir.x);

    vec3 worldDistort =
            (radialDir * heatNoise
            + tangentDir * (fbm(heatUV + 4.2) * 2.0 - 1.0))
            * radius * 0.06;

    vec2 refractOffset = projectedWorldOffset(scenePos, worldDistort) * heat;
    vec3 refractedBg = texture(
            DiffuseSampler,
            clamp(uv + refractOffset, vec2(0.0), vec2(1.0))
    ).rgb;

    vec3 baseBg = mix(color, refractedBg, clamp(heat * 2.0, 0.0, 1.0));

    float fireAmount = 1.0 - transmittance;
    vec3 integrated = accumulated / max(fireAmount, 0.001);

    return mix(baseBg, integrated, clamp(fireAmount, 0.0, 1.0));
}
vec3 applyMalevolentShrineFireLegacy(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float progress = clamp(data.z, 0.0, 1.0);
    float intensity = max(data.w, 0.0);
    vec3 ro = vec3(0.0);
    vec3 rd = normalize(scenePos);
    float expand = mix(0.18, 1.0, smoothstep(0.0, 0.35, progress));
    float decay = 1.0 - smoothstep(0.62, 1.0, progress);
    float height = radius * mix(1.35, 2.15, expand);
    float maxDistance = length(scenePos);
    float boundRadius = radius * 1.35;
    float centerProj = dot(rd, center - ro);
    float tNear = max(centerProj - boundRadius, 0.0);
    float tFar = min(centerProj + boundRadius, maxDistance);
    if (tNear >= tFar) return color;

    const int STEPS = 64;
    float span = max(tFar - tNear, 0.001);
    float stepSize = span / float(STEPS);
    float transmittance = 1.0;
    vec3 accumulated = vec3(0.0);
    vec3 flow3 = vec3(Time * 0.18, Time * 0.42, -Time * 0.13);

    for (int i = 0; i < STEPS; i++) {
        float t = tNear + (float(i) + 0.5) * stepSize;
        vec3 p = ro + rd * t;
        vec3 q = p - center;
        float y01 = clamp((q.y + radius * 0.45) / max(height, 0.001), 0.0, 1.0);
        float width = mix(1.25, 0.25, pow(y01, 1.35));
        vec2 horizontal = q.xz / max(radius * width, 0.001);
        float radial = length(horizontal);
        float body = 1.0 - smoothstep(0.35, 1.05, radial);
        float vertical = smoothstep(-radius * 0.35, radius * 0.05, q.y)
                * (1.0 - smoothstep(height * 0.65, height, q.y));
        vec2 baseXZ = q.xz * 0.42 + flow3.xz;
        float nXZ1 = fbm(baseXZ);
        float nXZ2 = fbm(baseXZ * 2.3 - flow3.zx);
        float nY = fbm(vec2(q.y * 0.55, Time * 0.16));
        float noise3D = mix(nXZ1, nXZ2, 0.45);
        noise3D = mix(noise3D, nY, 0.35);
        vec2 warpUV = baseXZ + vec2(noise3D * 1.8, nY * 1.5);
        float warped = fbm(warpUV);
        float angle = atan(q.z, q.x) + warped * 5.5 + Time * 0.45;
        float swirl = sin(angle * 3.0 + q.y * 1.7);
        swirl = 0.5 + 0.5 * swirl;
        float tear = smoothstep(0.25, 0.85, warped);
        float density = body * vertical * (0.25 + noise3D * 0.85 + swirl * 0.55);
        density *= mix(0.55, 1.35, tear);
        density *= intensity * decay;
        float omnidirectional = 1.0 - smoothstep(radius * 0.25, radius * 1.25, length(q));
        density = max(density, omnidirectional * 0.12);
        float coreMask = 1.0 - smoothstep(0.0, 0.42, radial);
        float edgeMask = smoothstep(0.55, 1.0, radial);
        density *= mix(0.65, 1.25, coreMask);
        density *= mix(1.0, 0.45, edgeMask);
        float opticalDepth = density * stepSize * 3.4;
        float alpha = 1.0 - exp(-opticalDepth);
        float temperature = clamp(coreMask * 1.15 + density * 0.35 + (1.0 - radial) * 0.35, 0.0, 1.0);
        vec3 whiteHot = vec3(1.0);
        vec3 hotYellow = vec3(1.0, 0.96, 0.84);
        vec3 yellow = vec3(1.0, 0.78, 0.32);
        vec3 orange = vec3(1.0, 0.38, 0.055);
        vec3 red = vec3(0.72, 0.055, 0.01);
        vec3 smoke = vec3(0.018, 0.004, 0.003);
        vec3 fireColor = mix(smoke, red, smoothstep(0.05, 0.25, temperature));
        fireColor = mix(fireColor, orange, smoothstep(0.25, 0.50, temperature));
        fireColor = mix(fireColor, yellow, smoothstep(0.50, 0.72, temperature));
        fireColor = mix(fireColor, hotYellow, smoothstep(0.72, 0.90, temperature));
        fireColor = mix(fireColor, whiteHot, smoothstep(0.90, 1.0, temperature));
        float emission = pow(temperature, 2.4) * density * 5.5;
        fireColor *= 1.0 + emission;
        fireColor = mix(fireColor, fireColor + tint * 0.35, 0.18);
        accumulated += fireColor * alpha * transmittance;
        transmittance *= 1.0 - alpha;
        if (transmittance < 0.015) break;
    }

    float fireAmount = 1.0 - transmittance;
    vec3 integrated = accumulated / max(fireAmount, 0.001);
    vec3 result = mix(color, integrated, clamp(fireAmount, 0.0, 0.96));
    vec3 surface = scenePos - center;
    float surfaceDist = length(surface);
    vec3 radial = surfaceDist > 0.001 ? surface / surfaceDist : vec3(1.0, 0.0, 0.0);
    vec3 tangent = vec3(-radial.z, 0.0, radial.x);
    float heat = decay * intensity
            * smoothstep(radius * 0.35, radius * 1.25, surfaceDist)
            * (1.0 - smoothstep(radius * 0.8, radius * 1.8, surfaceDist));
    float heatWave = sin(surfaceDist * 9.0 - Time * 12.0) * 0.5 + 0.5;
    vec3 worldOffset = radial * (heatWave * radius * 0.045)
            + tangent * (cos(surfaceDist * 13.0 + Time * 8.0) * radius * 0.028)
            + vec3(0.0, 1.0, 0.0) * (sin(Time * 5.0 + surfaceDist * 4.0) * radius * 0.018);
    vec2 refractOffset = projectedWorldOffset(scenePos, worldOffset) * heat;
    vec3 refracted = texture(DiffuseSampler, clamp(uv + refractOffset, vec2(0.0), vec2(1.0))).rgb;
    result = mix(refracted, result, clamp(1.0 - heat * 0.35, 0.0, 1.0));
    return result;
}
vec3 applyMalevolentShrineBlackDomain(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float progress = clamp(data.z, 0.0, 1.0);
    float intensity = max(data.w, 0.0);

    vec3 ro = vec3(0.0);
    vec3 rd = normalize(viewRay(uv));

    // 严格限制在领域球体内部，绝不向球外泄漏密度。
    vec3 oc = ro - center;
    float b = dot(oc, rd);
    float c = dot(oc, oc) - radius * radius;
    float h = b * b - c;

    if (h < 0.0) {
        return color;
    }

    h = sqrt(h);
    float tNear = max(-b - h, 0.0);
    float tFar = min(-b + h, length(scenePos));

    if (tNear >= tFar) {
        return color;
    }

    const int STEPS = 56;
    float span = max(tFar - tNear, 0.001);
    float stepSize = span / float(STEPS);
    float dither = fireBayerDither(uv);

    float trans = 1.0;
    vec3 accum = vec3(0.0);
    float decay = 1.0 - smoothstep(0.8, 1.0, progress);

    for (int i = 0; i < STEPS; i++) {
        float t = tNear + (float(i) + dither) * stepSize;
        vec3 p = ro + rd * t;
        vec3 q = p - center;

        float distNorm = length(q) / radius;
        float boundaryFade = smoothstep(1.0, 0.75, distNorm);
        if (boundaryFade <= 0.001) {
            continue;
        }

        vec2 uvBase = q.xz * (5.0 / radius) - Time * 2.5;
        float n1 = fbm(uvBase);

        vec2 uvTear = q.xz * (10.0 / radius) + vec2(n1 * 2.0) + Time * 1.5;
        float n2 = fbm(uvTear + q.y * (3.0 / radius));
        float noise = mix(n1, n2, 0.65);

        float hNorm = clamp((q.y + radius * 0.4) / (radius * 1.2), 0.0, 1.0);
        float heightFade = smoothstep(1.0, 0.1, hNorm);
        float shape = heightFade * 0.85 + (noise - 0.45) * 1.5;

        // 玩家位于领域内部时，贴近相机的浓烟仍然保持一点能见度。
        float camFade = smoothstep(0.0, max(radius * 0.15, 0.5), t);
        float density = smoothstep(0.0, 0.6, shape) * boundaryFade * intensity * decay * camFade;

        if (density <= 0.01) {
            continue;
        }

        float temp = clamp(heightFade * 0.4 + noise * 0.8, 0.0, 1.0);

        vec3 cSmoke = vec3(0.01, 0.005, 0.012);
        vec3 cDarkPurple = vec3(0.20, 0.02, 0.30);
        vec3 cCrimson = vec3(0.70, 0.05, 0.08);
        vec3 cAshWhite = vec3(0.85, 0.80, 0.95);

        vec3 col = cSmoke;
        col = mix(col, cDarkPurple, smoothstep(0.10, 0.35, temp));
        col = mix(col, cCrimson, smoothstep(0.35, 0.65, temp));
        col = mix(col, cAshWhite, smoothstep(0.65, 0.95, temp));

        float absorption = density * stepSize * mix(12.0, 3.0, temp);
        float alpha = 1.0 - exp(-absorption);
        float emission = pow(max(temp - 0.4, 0.0), 2.5) * 12.0;

        vec3 fireColor = col * (1.0 + emission);
        fireColor += tint * emission * 0.15;

        accum += fireColor * alpha * trans;
        trans *= 1.0 - alpha;

        if (trans < 0.015) {
            break;
        }
    }

    float fireAmount = 1.0 - trans;
    vec3 integrated = accum / max(fireAmount, 0.001);
    return mix(color, integrated, clamp(fireAmount, 0.0, 1.0));
}
vec3 applyMalevolentShrineBlackMist(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float progress = clamp(data.z, 0.0, 1.0);
    float intensity = max(data.w, 0.0);

    vec3 ro = vec3(0.0);
    vec3 rd = normalize(viewRay(uv));

    float expand = mix(0.30, 1.0, smoothstep(0.0, 0.35, progress));
    float decay = 1.0 - smoothstep(0.72, 1.0, progress);
    float height = radius * mix(1.35, 2.15, expand);

    // 保留旧版黑烟的稀疏体积感，但用真实球体求交锁死边界。
    float boundRadius = radius * 1.10;
    vec3 oc = ro - center;
    float b = dot(oc, rd);
    float c = dot(oc, oc) - boundRadius * boundRadius;
    float h = b * b - c;

    if (h < 0.0) {
        return color;
    }

    h = sqrt(h);
    float tNear = max(-b - h, 0.0);
    float tFar = min(-b + h, length(scenePos));

    if (tNear >= tFar) {
        return color;
    }

    const int STEPS = 52;
    float span = max(tFar - tNear, 0.001);
    float stepSize = span / float(STEPS);
    float dither = fireBayerDither(uv);

    float transmittance = 1.0;
    vec3 accumulated = vec3(0.0);

    for (int i = 0; i < STEPS; i++) {
        float t = tNear + (float(i) + dither) * stepSize;
        vec3 p = ro + rd * t;
        vec3 q = p - center;

        float distNorm = length(q) / radius;
        float sphereMask = 1.0 - smoothstep(0.84, 1.0, distNorm);
        if (sphereMask <= 0.001) {
            continue;
        }

        float hNorm = clamp((q.y + radius * 0.40) / max(height, 0.001), 0.0, 1.0);
        vec3 qNorm = q / max(radius, 0.001);

        vec2 uvBase = vec2(length(qNorm.xz) * 1.55, qNorm.y * 1.15 - Time * 2.8);
        float nBase = fbm(uvBase);

        vec2 uvTear = qNorm.xz * 1.35;
        uvTear.x += nBase * 1.65 + Time * 0.65;
        uvTear.y -= nBase * 1.25 - Time * 0.9;
        float nTear = fbm(uvTear + qNorm.y * 1.1 + Time * 0.22);
        float noise = mix(nBase, nTear, 0.58);

        float vertical = smoothstep(-radius * 0.48, -radius * 0.10, q.y)
                * (1.0 - smoothstep(height * 0.72, height * 1.05, q.y));

        // 去掉旧火柱的 profile/baseMask/core，只保留受球体约束的稀疏烟丝。
        float wisp = smoothstep(0.44, 0.78, noise);
        float ribbon = smoothstep(0.50, 0.86, fbm(uvBase * 2.35 + vec2(Time * 0.4, -Time * 0.25)));
        float sparse = wisp * (0.42 + 0.58 * ribbon);
        float camFade = smoothstep(0.0, max(radius * 0.16, 0.5), t);

        float density = sparse * vertical * sphereMask * intensity * decay * camFade;

        if (density <= 0.008) {
            continue;
        }

        float temp = clamp(noise * 0.38 + (1.0 - hNorm) * 0.12, 0.0, 1.0);

        vec3 cSmoke = vec3(0.008, 0.004, 0.006);
        vec3 cAsh = vec3(0.12, 0.10, 0.12);
        vec3 cDarkRed = vec3(0.48, 0.025, 0.012);
        vec3 cEmber = vec3(0.90, 0.18, 0.025);

        vec3 col = cSmoke;
        col = mix(col, cAsh, smoothstep(0.12, 0.38, temp));
        col = mix(col, cDarkRed, smoothstep(0.38, 0.66, temp));
        col = mix(col, cEmber, smoothstep(0.72, 0.95, temp));

        float absorption = density * stepSize * mix(10.0, 4.0, temp);
        float alpha = 1.0 - exp(-absorption);
        float emission = pow(max(temp - 0.68, 0.0), 2.0) * 5.0;

        vec3 smokeColor = col * (1.0 + emission);
        smokeColor += tint * emission * 0.10;

        accumulated += smokeColor * alpha * transmittance;
        transmittance *= 1.0 - alpha;

        if (transmittance < 0.015) {
            break;
        }
    }

    float smokeAmount = 1.0 - transmittance;
    vec3 integrated = accumulated / max(smokeAmount, 0.001);
    return mix(color, integrated, clamp(smokeAmount, 0.0, 1.0));
}
float warpNoise(vec3 p, float a) {
    return abs(dot(sin(Time + 0.1 * p.z + 0.3 * p / a), vec3(a + a)));
}

float bayerDither(vec2 uv) {
    return fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 applyMalevolentShrineVoid(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float progress = clamp(data.z, 0.0, 1.0);
    float intensity = max(data.w, 0.0);

    vec3 ro = vec3(0.0);
    vec3 rd = normalize(viewRay(uv));
    vec3 oc = ro - center;

    float b = dot(oc, rd);
    float c = dot(oc, oc) - radius * radius;
    float h = b * b - c;
    if (h < 0.0) {
        return color;
    }

    h = sqrt(h);
    float tNear = max(-b - h, 0.0);
    float tFar = min(-b + h, length(scenePos));
    if (tNear >= tFar) {
        return color;
    }

    float span = tFar - tNear;
    const int STEPS = 48;
    float stepSize = span / float(STEPS);
    float dither = bayerDither(uv);

    float trans = 1.0;
    vec3 accum = vec3(0.0);

    for (int i = 0; i < STEPS; i++) {
        float t = tNear + (float(i) + dither) * stepSize;
        vec3 p = ro + rd * t - center;

        float r = length(p) / radius;
        float sphereMask = smoothstep(1.0, 0.72, r);
        if (sphereMask <= 0.001) {
            continue;
        }

        vec2 flowUV = p.xz * 0.35 + vec2(Time * 0.02, Time * -0.015);
        float flow = fbm(flowUV);
        vec2 warpUV = p.yx * 0.5 + flow * 0.6 + Time * 0.03;
        float cloud = fbm(warpUV);

        float riftX = smoothstep(0.15, 0.0, abs(fbm(p.zy * 0.75 + flow) - 0.5));
        float riftY = smoothstep(0.15, 0.0, abs(fbm(p.xy * 0.75 - flow) - 0.5));
        float rift = riftX * riftY * 3.5;

        float core = smoothstep(1.0, 0.0, r);
        float density = sphereMask * (cloud * 0.85 + rift + 0.15) * (0.35 + core * 0.75);

        vec3 baseColor = mix(vec3(0.02, 0.0, 0.06), vec3(0.35, 0.05, 0.5), cloud);
        vec3 riftColor = vec3(0.85, 0.15, 1.0) * rift;
        vec3 cloudColor = baseColor + riftColor + tint * 0.25;

        float alpha = 1.0 - exp(-density * intensity * 0.25 * stepSize);

        accum += cloudColor * alpha * trans;
        trans *= 1.0 - alpha;

        if (trans < 0.02) {
            break;
        }
    }

    float amount = 1.0 - trans;
    vec3 volume = accum / max(amount, 0.001);

    return mix(color, volume, clamp(amount, 0.0, 0.9));
}

float starHash(vec3 p) {
    p = fract(p * 0.3183099 + vec3(0.1));
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

vec3 randomDirection(vec3 p) {
    float a = starHash(p) * 6.28318;
    float z = starHash(p + 13.7) * 2.0 - 1.0;
    float r = sqrt(max(0.0, 1.0 - z * z));
    return vec3(r * cos(a), z, r * sin(a));
}

vec3 applyMalevolentShrineStarfield(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float progress = clamp(data.z, 0.0, 1.0);
    float intensity = max(data.w, 0.0);
    float fade = 1.0;

    vec3 ro = vec3(0.0);
    vec3 rd = normalize(viewRay(uv));
    vec3 oc = ro - center;

    float b = dot(oc, rd);
    float c = dot(oc, oc) - radius * radius;
    float h = b * b - c;
    if (h < 0.0) {
        return color;
    }

    h = sqrt(h);
    float tNear = max(-b - h, 0.0);
    float tFar = min(-b + h, length(scenePos));
    if (tNear >= tFar) {
        return color;
    }

    float span = max(tFar - tNear, 0.001);
    const int STEPS = 40;
    float stepSize = span / float(STEPS);
    float dither = bayerDither(uv);

    float transmittance = 1.0;
    vec3 accumulated = vec3(0.0);
    float cellSize = radius * 0.08;

    for (int i = 0; i < STEPS; i++) {
        float t = tNear + (float(i) + dither) * stepSize;
        vec3 p = ro + rd * t;
        vec3 local = p - center;

        float normalized = length(local) / radius;
        float sphereMask = smoothstep(1.0, 0.72, normalized);
        if (sphereMask <= 0.001) {
            continue;
        }

        vec3 cell = floor(local / cellSize);
        float rnd = starHash(cell);
        if (rnd <= 0.90) {
            continue;
        }

        vec3 starPos = (cell + 0.5) * cellSize + randomDirection(cell) * (cellSize * 0.35);
        vec3 diff = local - starPos;
        float starDist = length(diff);
        float starRadius = cellSize * mix(0.05, 0.15, rnd);

        float core = exp(-starDist * starDist / (starRadius * starRadius));
        float glow = exp(-starDist / (starRadius * 2.5));
        vec3 absDiff = abs(diff);
        float crossMask = exp(-max(absDiff.x, max(absDiff.y, absDiff.z)) * 12.0 / starRadius);
        crossMask *= exp(-starDist * 1.5 / starRadius);

        float phase = Time * mix(2.0, 4.0, rnd) + starHash(cell + 1.0) * 6.28;
        float twinkle = smoothstep(-0.5, 1.0, sin(phase));

        vec3 starColor = mix(vec3(0.4, 0.75, 1.0), vec3(1.0, 0.9, 0.6), fract(rnd * 13.0));
        if (fract(rnd * 17.0) > 0.85) {
            starColor = vec3(1.0, 0.4, 0.3);
        }
        starColor += tint * 0.2;

        float density = (core * 2.5 + glow * 0.6 + crossMask * 1.5) * sphereMask * (0.4 + 0.6 * twinkle);
        float alpha = 1.0 - exp(-density * intensity * stepSize * 3.5);

        accumulated += starColor * alpha * transmittance;
        transmittance *= 1.0 - alpha;

        if (transmittance < 0.02) {
            break;
        }
    }

    return color + accumulated * fade;
}

vec3 applyMalevolentShrineTargetGlow(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float radius = max(data.y, 0.001);
    float height = max(data.z, 0.5);
    float intensity = max(data.w, 0.0);
    vec3 delta = scenePos - center;
    float dist = length(delta);
    float body = 1.0 - smoothstep(radius * 0.45, radius * 1.05, dist);
    float vertical = 1.0 - smoothstep(height * 0.42, height * 0.82, abs(delta.y));
    float pulse = 0.72 + 0.28 * sin(Time * 4.2 + length(delta.xz) * 3.1);
    float glow = (body * vertical * 0.46 + edge * 0.54) * intensity * pulse;
    vec3 blood = mix(vec3(0.06, 0.0, 0.01), vec3(0.95, 0.025, 0.035), edge);
    return color * (1.0 - glow * 0.28) + blood * glow;
}
float catHash3(vec3 p) {
    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453);
}

float catNoise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float n000 = catHash3(i + vec3(0.0, 0.0, 0.0));
    float n100 = catHash3(i + vec3(1.0, 0.0, 0.0));
    float n010 = catHash3(i + vec3(0.0, 1.0, 0.0));
    float n110 = catHash3(i + vec3(1.0, 1.0, 0.0));
    float n001 = catHash3(i + vec3(0.0, 0.0, 1.0));
    float n101 = catHash3(i + vec3(1.0, 0.0, 1.0));
    float n011 = catHash3(i + vec3(0.0, 1.0, 1.0));
    float n111 = catHash3(i + vec3(1.0, 1.0, 1.0));
    float nx00 = mix(n000, n100, f.x);
    float nx10 = mix(n010, n110, f.x);
    float nx01 = mix(n001, n101, f.x);
    float nx11 = mix(n011, n111, f.x);
    return mix(mix(nx00, nx10, f.y), mix(nx01, nx11, f.y), f.z);
}

float catFbm3(vec3 p) {
    float value = 0.0;
    float amplitude = 0.55;
    for (int i = 0; i < 3; i++) {
        value += amplitude * catNoise3(p);
        p = p * 2.07 + vec3(11.3, 7.1, 5.7);
        amplitude *= 0.48;
    }
    return value;
}

float catSmin(float a, float b, float k) {
    float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
    return mix(b, a, h) - k * h * (1.0 - h);
}

float catSdEllipsoid(vec3 p, vec3 center, vec3 radius) {
    vec3 q = (p - center) / radius;
    return (length(q) - 1.0) * min(min(radius.x, radius.y), radius.z);
}


float catSdSegment3(vec3 p, vec3 a, vec3 b) {
    vec3 pa = p - a;
    vec3 ba = b - a;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba * h);
}

float catSdQuadraticBezier3(vec3 p, vec3 a, vec3 b, vec3 c) {
    vec3 previous = a;
    float best = 1000000.0;
    for (int i = 1; i <= 10; i++) {
        float t = float(i) / 10.0;
        vec3 point = mix(mix(a, b, t), mix(b, c, t), t);
        best = min(best, catSdSegment3(p, previous, point));
        previous = point;
    }
    return best;
}

float catSdTriangle2D(vec2 p, vec2 a, vec2 b, vec2 c) {
    vec2 e0 = b - a;
    vec2 e1 = c - b;
    vec2 e2 = a - c;
    vec2 v0 = p - a;
    vec2 v1 = p - b;
    vec2 v2 = p - c;

    vec2 pq0 = v0 - e0 * clamp(dot(v0, e0) / dot(e0, e0), 0.0, 1.0);
    vec2 pq1 = v1 - e1 * clamp(dot(v1, e1) / dot(e1, e1), 0.0, 1.0);
    vec2 pq2 = v2 - e2 * clamp(dot(v2, e2) / dot(e2, e2), 0.0, 1.0);
    float s = sign(e0.x * e2.y - e0.y * e2.x);
    vec2 d = min(
        min(vec2(dot(pq0, pq0), s * (v0.x * e0.y - v0.y * e0.x)),
            vec2(dot(pq1, pq1), s * (v1.x * e1.y - v1.y * e1.x))),
        vec2(dot(pq2, pq2), s * (v2.x * e2.y - v2.y * e2.x))
    );
    return -sqrt(d.x) * sign(d.y);
}

float catSdRoundedEar(vec3 p, vec2 a, vec2 b, vec2 c, float cornerRadius, float baseDepth) {
    float frontShape = catSdTriangle2D(p.xy, a, b, c) - cornerRadius;
    float height01 = clamp((p.y - 0.10) / 0.82, 0.0, 1.0);
    float halfDepth = mix(baseDepth, baseDepth * 0.62, height01);
    vec2 extruded = vec2(frontShape, abs(p.z) - halfDepth);
    return min(max(extruded.x, extruded.y), 0.0) + length(max(extruded, 0.0));
}

float catHeadField(vec3 p) {
    vec3 q = p - vec3(0.0, -0.06, 0.0);

    float head = catSdEllipsoid(q, vec3(0.0, 0.0, 0.0), vec3(0.68, 0.58, 0.56));
    float leftEar = catSdRoundedEar(q, vec2(-0.14, 0.29), vec2(-0.74, 0.18), vec2(-0.80, 0.94), 0.090, 0.205);
    float rightEar = catSdRoundedEar(q, vec2(0.14, 0.29), vec2(0.74, 0.18), vec2(0.80, 0.94), 0.090, 0.205);
    float leftCheek = catSdEllipsoid(q, vec3(-0.33, -0.20, 0.23), vec3(0.32, 0.22, 0.34));
    float rightCheek = catSdEllipsoid(q, vec3(0.33, -0.20, 0.23), vec3(0.32, 0.22, 0.34));
    float muzzle = catSdEllipsoid(q, vec3(0.0, -0.31, 0.34), vec3(0.24, 0.13, 0.14));
    float chin = catSdEllipsoid(q, vec3(0.0, -0.47, 0.24), vec3(0.28, 0.16, 0.31));

    float d = catSmin(head, leftEar, 0.072);
    d = catSmin(d, rightEar, 0.072);
    d = catSmin(d, leftCheek, 0.085);
    d = catSmin(d, rightCheek, 0.085);
    d = catSmin(d, muzzle, 0.075);
    d = catSmin(d, chin, 0.085);
    return d;
}

float catEyeMask(vec3 p) {
    // Eyes sit deeper in the head volume; the mouth stays on the front surface.
    float front = smoothstep(0.22, 0.40, p.z);
    float left = 1.0 - smoothstep(0.060, 0.102, length(p - vec3(-0.25, 0.015, 0.405)));
    float right = 1.0 - smoothstep(0.060, 0.102, length(p - vec3(0.25, 0.015, 0.405)));
    return (left + right) * front;
}

float catNoseMask(vec3 p) {
    vec3 n = p - vec3(0.0, -0.145, 0.50);
    float irregularity = catNoise3(n * 19.0 + vec3(Time * 0.11, -Time * 0.07, Time * 0.09));
    float noseRadius = 0.034 + irregularity * 0.012;
    float d = length(n) - noseRadius;
    return 1.0 - smoothstep(0.008, 0.042, d);
}

float catMouthMask(vec3 p) {
    // One smooth U-shaped half; mirroring it across x produces a rounded W.
    vec3 q = vec3(abs(p.x), p.y, p.z);
    vec3 centerTop = vec3(0.00, -0.20, 0.490);
    vec3 valleyControl = vec3(0.145, -0.360, 0.455);
    vec3 outerTop = vec3(0.29, -0.20, 0.425);

    float d = catSdQuadraticBezier3(q, centerTop, valleyControl, outerTop);
    return 1.0 - smoothstep(0.018, 0.052, d);
}

vec3 applyBlackCatHeadFog(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float maxDistance) {
    float radius = max(data.y, 0.35);
    float yaw = data.z;
    float intensity = max(data.w, 0.0);
    vec3 eyeColor = vec3(0.52, 0.94, 1.0);
    vec3 noseColor = vec3(1.0, 0.42, 0.66);
    vec3 mouthColor = vec3(1.0, 0.75, 0.88);

    vec3 forward = vec3(-sin(yaw), 0.0, cos(yaw));
    vec3 right = normalize(cross(vec3(0.0, 1.0, 0.0), forward));
    vec3 up = normalize(cross(forward, right));

    vec3 ro = vec3(0.0);
    vec3 rd = normalize(viewRay(uv));
    float boundRadius = radius * 1.58;
    vec3 oc = ro - center;
    float b = dot(oc, rd);
    float c = dot(oc, oc) - boundRadius * boundRadius;
    float h = b * b - c;
    if (h < 0.0) {
        return color;
    }

    h = sqrt(h);
    float tNear = max(-b - h, 0.0);
    float tFar = min(-b + h, maxDistance);
    if (tNear >= tFar) {
        return color;
    }

    const int CAT_STEPS = 44;
    float span = max(tFar - tNear, 0.001);
    float stepSize = span / float(CAT_STEPS);
    float dither = fireBayerDither(uv);
    float transmittance = 1.0;
    vec3 accumulated = vec3(0.0);

    for (int i = 0; i < CAT_STEPS; i++) {
        float t = tNear + (float(i) + dither) * stepSize;
        vec3 p = ro + rd * t;
        vec3 worldDelta = p - center;
        vec3 local = vec3(
            dot(worldDelta, right) / radius,
            dot(worldDelta, up) / max(radius * 1.05, 0.001),
            dot(worldDelta, forward) / radius
        );

        float shape = catHeadField(local);
        float density = 1.0 - smoothstep(-0.13, 0.10, shape);
        float boxFade = 1.0 - smoothstep(0.82, 1.08, max(max(abs(local.x), abs(local.y)), abs(local.z)));
        density *= boxFade;
        if (density <= 0.001) {
            continue;
        }

        float flow = clamp(catFbm3(local * 2.35 + vec3(Time * 0.09, -Time * 0.19, Time * 0.12)), 0.0, 1.0);
        float detail = clamp(catFbm3(local * 5.10 + vec3(-Time * 0.13, Time * 0.28, Time * 0.17)), 0.0, 1.0);
        density *= mix(0.42, 1.05, flow) * (0.82 + 0.22 * detail);

        float opticalDepth = density * stepSize * 1.55 / max(radius, 0.001);
        float sampleAlpha = 1.0 - exp(-opticalDepth);
        float smokeTone = flow * 0.75 + detail * 0.25;
        vec3 sampleColor = mix(tint, tint * 2.8 + mouthColor * 0.025, smokeTone * 0.42);

        float eye = catEyeMask(local) * (0.78 + 0.22 * sin(Time * 4.5));
        float nose = catNoseMask(local);
        float mouth = catMouthMask(local);
        float feature = clamp(eye + nose * 0.85 + mouth * 0.72, 0.0, 1.55);
        vec3 featureColor = eyeColor * (eye * 1.65)
                + noseColor * (nose * 1.12)
                + mouthColor * (mouth * 0.96);
        sampleAlpha = max(sampleAlpha, feature * 0.035);

        accumulated += transmittance * (sampleColor * sampleAlpha + featureColor * (0.045 + sampleAlpha * 0.82));
        transmittance *= 1.0 - clamp(sampleAlpha, 0.0, 0.84);

        if (transmittance < 0.03) {
            break;
        }
    }

    float fogAmount = 1.0 - transmittance;
    vec3 integrated = accumulated / max(fogAmount, 0.001);
    return mix(color, integrated, clamp(fogAmount * intensity, 0.0, 0.95));
}
void main() {
    vec2 uv = texCoord;
    vec4 base = texture(DiffuseSampler, uv);
    float depth = depthAt(uv);
    bool sky = depth >= 0.999999;

    vec3 scenePos;
    float edge;
    if (sky) {
        scenePos = viewRay(uv) * 320.0;
        edge = 0.0;
    } else {
        scenePos = reconstructWorldPosition(uv, depth);
        edge = depthEdge(uv, depth);
    }

    vec3 color = base.rgb;

    // 先应用领域雾场，让其他屏幕空间特效叠在雾上，避免被雾盖住。
    for (int i = 0; i < 8; i++) {
        if (i >= EffectCount) {
            break;
        }
        vec4 data = effectData(i);
        int mode = int(data.x + 0.5);
        if (mode == 7) {
            color = applyMalevolentShrineDomain(color, scenePos, uv, effectCenter(i), data, effectColor(i));
        }
    }

    // 再应用斩击、目标血光和既有的冲击波/热浪/光柱等特效。
    for (int i = 0; i < 8; i++) {
        if (i >= EffectCount) {
            break;
        }

        vec4 data = effectData(i);
        int mode = int(data.x + 0.5);
        vec3 center = effectCenter(i);
        vec3 tint = effectColor(i);

        if (sky) {
            if (mode == 10) {
                color = applyMalevolentShrineFire(color, scenePos, uv, center, data, tint, edge);
            } else if (mode == 13) {
                color = applyMalevolentShrineFireLegacy(color, scenePos, uv, center, data, tint, edge);
            } else if (mode == 14) {
                color = applyMalevolentShrineBlackDomain(color, scenePos, uv, center, data, tint, edge);
            } else if (mode == 15) {
                color = applyMalevolentShrineBlackMist(color, scenePos, uv, center, data, tint, edge);
            } else if (mode == 16) {
                color = applyBlackCatHeadFog(color, scenePos, uv, center, data, tint, 320.0);
            } else if (mode == 11) {
                color = applyMalevolentShrineVoid(color, scenePos, uv, center, data, tint, edge);
            } else if (mode == 12) {
                color = applyMalevolentShrineStarfield(color, scenePos, uv, center, data, tint, edge);
            }
            continue;
        }

        if (mode == 7) {
            continue;
        }

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
        } else if (mode == 6) {
            color = applyDepthRefractionPressure(color, scenePos, uv, center, data, tint, edge);
        } else if (mode == 8) {
            color = applyMalevolentShrineSlash(color, scenePos, uv, center, data, tint);
        } else if (mode == 10) {
            color = applyMalevolentShrineFire(color, scenePos, uv, center, data, tint, edge);
        } else if (mode == 13) {
            color = applyMalevolentShrineFireLegacy(color, scenePos, uv, center, data, tint, edge);
        } else if (mode == 14) {
            color = applyMalevolentShrineBlackDomain(color, scenePos, uv, center, data, tint, edge);
        } else if (mode == 15) {
            color = applyMalevolentShrineBlackMist(color, scenePos, uv, center, data, tint, edge);
        } else if (mode == 16) {
            color = applyBlackCatHeadFog(color, scenePos, uv, center, data, tint, length(scenePos));
        } else if (mode == 11) {
            color = applyMalevolentShrineVoid(color, scenePos, uv, center, data, tint, edge);
        } else if (mode == 12) {
            color = applyMalevolentShrineStarfield(color, scenePos, uv, center, data, tint, edge);
        } else {
            color = applyMalevolentShrineTargetGlow(color, scenePos, uv, center, data, tint, edge);
        }
    }
    fragColor = vec4(color, base.a);
}
