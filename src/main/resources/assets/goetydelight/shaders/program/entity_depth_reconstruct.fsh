#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 ViewProjMat;
uniform mat4 InvViewProjMat;
uniform vec2 InSize;
uniform float Time;
uniform vec3 EffectCenter0;
uniform vec4 EffectData0;
uniform vec3 EffectColor0;

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

// ────────────────────────── 宇宙领域 视觉重制版 (Cosmic Domain, mode 17) ──────────────────────────
// 领域球里是一颗微缩星系 + 中心黑洞，密度严格锁在球内，球外只余流动日冕。
//   1. JWST 级 HDR 色板：暗紫罗兰 → 品红 → 青金石 → 核心白（非线性爆发曲线）；
//   2. Gargantua 级黑洞：反平方引力透镜 + 绝对黑体视界 + 极锐利光子环 + 多普勒增亮吸积盘；
//   3. 暗物质尘埃带（Dark Dust Lanes）：旋臂中夹杂不透光粉尘，让体积有沟壑与厚度；
//   4. 变形宽银幕星芒 + 真实恒星光谱（蓝巨星 / 黄矮星 / 红矮星）。
vec3 cosmicPalette(float t, vec3 tint) {
    float x = clamp(t, 0.0, 1.0);
    // 基础深空背景，比原来更深邃，带有一丝深空辐射的幽蓝
    vec3 abyss = vec3(0.005, 0.002, 0.015) + tint * 0.05;
    vec3 deepBlue = mix(vec3(0.02, 0.05, 0.25), tint, 0.4);
    vec3 magenta = mix(vec3(0.65, 0.10, 0.55), tint, 0.3);
    vec3 cyanGlow = mix(vec3(0.10, 0.85, 0.95), tint, 0.2);
    vec3 coreWhite = vec3(1.0, 0.95, 0.90);

    // 使用更平滑的非线性插值曲线，增强色彩的爆发感
    vec3 col = mix(abyss, deepBlue, smoothstep(0.0, 0.25, x));
    col = mix(col, magenta, smoothstep(0.20, 0.55, x));
    col = mix(col, cyanGlow, smoothstep(0.45, 0.85, x));
    col = mix(col, coreWhite, pow(smoothstep(0.75, 1.0, x), 2.0)); // 核心高光爆发

    return col;
}

vec3 applyCosmicDomain(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float sphereR = max(data.y, 0.001);
    float progress = clamp(data.z, 0.0, 1.0);
    float intensity = max(data.w, 0.0);
    float energy = intensity * (1.0 - smoothstep(0.86, 1.0, progress));

    if (energy <= 0.002) return color;

    vec3 ro = vec3(0.0);
    vec3 rd = normalize(viewRay(uv));
    float maxDistance = length(scenePos);
    float invRadius = 1.0 / sphereR;
    vec3 toCenter = center - ro;
    float centerProj = dot(rd, toCenter);
    float impact = sqrt(max(dot(toCenter, toCenter) - centerProj * centerProj, 0.0));
    float centerDist = length(toCenter);

    // ── 中心黑洞 (Gargantua 风格) ──
    float horizon = max(sphereR * 0.12, 0.05); // 稍微放大视界以增强压迫感
    float coreMask = 0.0;
    vec3 coreGlow = vec3(0.0);
    vec3 background = color;
    vec2 coreUv = cameraRelativeWorldToUv(center);

    if (centerProj > 0.05 && impact < horizon * 12.0 && coreUv.x > -100.0) {
        // 吸积结构本身是有体积的（halo 外缘约 5.6*horizon）。镜头一旦落进这团结构内部
        // （最常见的"领域挂在自己身上"：球心仅在镜头下方约 0.7 格），impact 会整屏满足条件 ——
        // 视界 coreMask 把下半屏连地形一起涂黑，halo 再叠一层与距离无关的平光，
        // 合起来就是那层跟着玩家跑、号称"无限范围"的光幕。
        // 所以整块黑洞按"镜头确实站到结构之外"淡入；coreAhead 仍然负责"结构不能在墙后被穿出来"。
        float coreShellRadius = horizon * 5.6;
        float coreOutside = smoothstep(coreShellRadius * 0.40, coreShellRadius, centerDist);
        float coreAhead = 1.0 - smoothstep(maxDistance, maxDistance + horizon * 1.2, centerProj);
        float coreVisible = coreOutside * coreAhead;
        vec2 axis = uv - coreUv;
        float axisLen = length(axis);
        vec2 outDir = axisLen > 0.0001 ? axis / axisLen : vec2(0.0, 1.0);

        // 更真实的引力透镜效应 (遵循反平方衰减)
        float uvPerMeter = length(projectedWorldOffset(center, vec3(1.0, 0.0, 0.0)));
        float lensFactor = horizon / max(impact, horizon * 0.5);
        float bend = uvPerMeter * pow(lensFactor, 2.2) * 1.8 * min(energy, 1.0);
        background = mix(background,
                texture(DiffuseSampler, clamp(uv + outDir * bend, vec2(0.0), vec2(1.0))).rgb,
                coreVisible);

        // 绝对黑体视界：完美吞噬
        coreMask = (1.0 - smoothstep(horizon * 0.95, horizon * 1.02, impact)) * coreVisible;
        background *= 1.0 - coreMask * min(energy, 1.0);

        // 多普勒增亮吸积盘 (Doppler Beamed Accretion Disk)
        vec2 beamAxis = normalize(vec2(-0.85, 0.5));
        float beamSide = 0.5 + 0.5 * dot(outDir, beamAxis); // 0 为远离(暗红)，1 为靠近(亮蓝/白)
        float dopplerShift = pow(beamSide, 2.5) * 2.5 + 0.2; // 极端的亮暗对比

        // 光子环 (极度锐利且明亮)
        float photonRing = exp(-pow(abs(impact - horizon * 1.05) / (horizon * 0.04), 2.0));
        // 主吸积盘
        float accretionDisk = exp(-pow(abs(impact - horizon * 1.6) / (horizon * 0.4), 1.5));

        vec3 hotColor = mix(vec3(0.9, 0.3, 0.1), vec3(0.8, 0.95, 1.0), beamSide);
        float flicker = 1.0 + 0.08 * sin(Time * 5.0 + axisLen * 50.0);

        coreGlow = hotColor * (photonRing * 3.5 + accretionDisk * 1.2)
                * dopplerShift * min(energy, 1.5) * flicker * coreVisible;
    }

    // ── 宇宙日冕边界层 ──
    vec3 rimTint = mix(tint, vec3(0.4, 0.8, 1.0), 0.6);
    float outside = max(impact - sphereR, 0.0);
    // 日冕必须紧贴轮廓。原来 exp(-3*outside/R) 的尾巴宽达 ~R/3：
    // 镜头靠近球壳时，"与领域垂直"的整片方向都落在尾巴里，淡色光会铺满视野周边，
    // 一转头就像一层淡色影子扫过屏幕（宇宙/月球共有的那层）。
    float corona = exp(-outside / max(sphereR * 0.11, 0.30));
    corona *= 1.0 - smoothstep(0.30, 0.60, outside / sphereR);

    // 球壳的真实几何：近端/远端壳交点距离。
    // 注意 impact 只决定"射线离球心多远"，它跟"壳在不在前面"无关 ——
    // h = sphereR^2 - impact^2，所以原来 `h < 0` 仅仅等价于 `impact > sphereR`：
    // 领域跑到镜头背后、或被墙挡住时，朝前的射线垂距照样能超过半径，
    // 于是日冕和细边会被画到前方任意距离的地板/墙上（光环"无限范围"乱跑）。
    float shellSpan = sqrt(max(sphereR * sphereR - impact * impact, 0.0));
    float shellNear = centerProj - shellSpan; // 近端壳交（miss 时退化为球面最近点）
    float shellFar = centerProj + shellSpan;  // 远端壳交
    // 壳必须"在镜头前方"且"不晚于首个可见表面"，边界才算真的被看到
    float shellVisible = (shellFar > 0.0 && shellNear < maxDistance) ? 1.0 : 0.0;
    // 只有镜头离开领域球之后才存在"球体轮廓"；贴在内壁/外壁时不再出现跟着视角的巨环
    float silhouette = smoothstep(1.0, 1.30, centerDist / sphereR);

    // 边界噪声改用世界空间方向，避免纹理跟着镜头滑动
    float tShell = max(shellNear, 0.0);
    vec3 shellVec = rd * tShell - center;
    float shellLen = length(shellVec);
    vec3 shellDir = shellLen > 0.0001 ? shellVec / shellLen : vec3(0.0, 1.0, 0.0);
    float edgeNoise = valueNoise(shellDir.xz * 4.0 + vec2(Time * 0.06, -Time * 0.045)) * 0.5 + 0.5;
    float edgeRing = exp(-abs(impact - sphereR) / (sphereR * 0.03)) * edgeNoise
            * shellVisible * silhouette;
    vec3 rimGlow = rimTint * edgeRing * 1.2 * energy;

    vec3 oc = ro - center;
    float b = dot(oc, rd);
    float c = dot(oc, oc) - sphereR * sphereR;
    float h = b * b - c;

    if (h < 0.0) {
        return background + rimGlow + rimTint * corona * 0.4 * energy * shellVisible * silhouette;
    }

    h = sqrt(h);
    float tNear = max(-b - h, 0.0);
    float tFar = min(-b + h, maxDistance);
    // 领域整个在可见几何之后：返回未被透镜/视界改动过的原色，否则会把黑斑和扭曲画到墙面上
    if (tNear >= tFar) return color;

    // ── 动态星系盘与体积积分 ──
    float tilt = 0.45 + 0.15 * sin(Time * 0.08); // 略微增加倾角让盘面更立体
    float ct = cos(tilt), st = sin(tilt);
    vec3 discNormal = vec3(0.0, ct, st);
    vec3 discAxisX = vec3(1.0, 0.0, 0.0);
    vec3 discAxisY = vec3(0.0, -st, ct);

    const int COSMIC_STEPS = 48; // 提升精度
    float span = max(tFar - tNear, 0.001);
    float stepSize = span / float(COSMIC_STEPS);
    float stepNorm = stepSize * invRadius;
    float dither = bayerDither(uv);
    float cellSize = sphereR * 0.05;

    float transmittance = 1.0;
    vec3 accumulated = vec3(0.0);

    for (int i = 0; i < COSMIC_STEPS; i++) {
        float t = tNear + (float(i) + dither) * stepSize;
        vec3 q = (ro + rd * t) - center;
        float rn = length(q) * invRadius;

        float sphereMask = smoothstep(1.0, 0.85, rn);
        if (sphereMask <= 0.005) continue;

        float height = dot(q, discNormal);
        float heightNorm = abs(height) * invRadius;
        vec2 discPos = vec2(dot(q, discAxisX), dot(q, discAxisY));
        float discR = length(discPos) * invRadius;
        float angle = atan(discPos.y, discPos.x);

        float thickness = mix(0.12, 0.35, smoothstep(0.0, 0.6, discR));
        float discVertical = exp(-heightNorm / max(thickness, 0.02));

        // 流体扭曲与星系旋臂 (加入对数螺旋)
        vec2 nebUv = discPos * (3.0 * invRadius) + vec2(Time * 0.02, -Time * 0.015);
        float warp = fbm(nebUv * 1.5 + vec2(-Time * 0.05));
        float neb = fbm(nebUv * 2.5 + warp * 1.2);

        // 对数螺旋线方程创造宏伟星系臂
        float logR = log(max(discR, 0.01));
        float spiral = angle * 2.0 - logR * 5.0 + Time * 0.3 + warp * 2.0;
        float arms = pow(cos(spiral) * 0.5 + 0.5, 3.5); // 更尖锐的旋臂

        // 【关键升级】暗物质/尘埃带遮蔽：吞噬光线的黑色裂纹
        float dustNoise = fbm(discPos * 5.0 * invRadius - vec2(Time * 0.03));
        float darkDust = smoothstep(0.4, 0.8, dustNoise) * arms * 0.8;

        float radialFade = smoothstep(0.02, 0.2, discR) * (1.0 - smoothstep(0.65, 1.0, discR));
        float armWeight = arms * discVertical * radialFade;

        // 弥散星云底色
        float haze = sphereMask * (0.1 + 0.9 * neb) * (1.0 - smoothstep(0.5, 1.0, rn)) * 0.25;

        // 极轴喷流 (脉冲星/黑洞喷流)
        float axialDist = length(q - discNormal * height) * invRadius;
        float jet = exp(-axialDist * axialDist * 80.0) * exp(-heightNorm * 1.5)
                  * (0.6 + 0.4 * valueNoise(vec2(height * invRadius * 15.0 - Time * 4.0, 0.0)));

        // ── 电影级稠密星点 (Anamorphic Flares) ──
        vec3 cell = floor(q / max(cellSize, 0.0001));
        float rnd = starHash(cell);
        float starWeight = 0.0;
        vec3 starColor = vec3(1.0);

        if (rnd > 0.82) { // 更稀疏但更明亮的星空
            vec3 starPos = (cell + 0.5) * cellSize + randomDirection(cell) * (cellSize * 0.4);
            vec3 diff = q - starPos;
            float sd = length(diff);
            float sr = max(cellSize * mix(0.03, 0.12, fract(rnd * 13.7)), 0.0001);

            float core = exp(-pow(sd / (sr * 0.4), 2.0)); // 锐利核心

            // JJ Abrams 风格的变形宽银幕十字星芒
            vec3 ad = abs(diff);
            float flareX = exp(-ad.x * 25.0 / sr) * exp(-ad.y * 3.0 / sr) * exp(-ad.z * 3.0 / sr);
            float flareY = exp(-ad.y * 25.0 / sr) * exp(-ad.x * 3.0 / sr) * exp(-ad.z * 3.0 / sr);
            float crossFlare = (flareX + flareY) * 1.2;

            float phase = Time * mix(1.0, 4.0, fract(rnd * 9.3)) + starHash(cell + 5.0) * 6.28;
            float twinkle = mix(0.4, 1.0, pow(sin(phase) * 0.5 + 0.5, 2.0)); // 平滑闪烁

            // 真实的恒星光谱分布 (蓝巨星 -> 黄矮星 -> 红矮星)
            float tempHash = fract(rnd * 23.9);
            starColor = mix(vec3(0.5, 0.8, 1.0), vec3(1.0, 0.9, 0.7), smoothstep(0.0, 0.6, tempHash));
            starColor = mix(starColor, vec3(1.0, 0.3, 0.2), smoothstep(0.8, 1.0, tempHash)); // 少量红星

            starWeight = (core * 2.5 + crossFlare) * twinkle * sphereMask;
        }

        // 光学积分
        float discWeight = discVertical * radialFade * (0.2 + 0.8 * neb) * 0.6;
        float totalWeight = (discWeight + armWeight * 1.5 + haze * 0.4 + jet * 1.5);
        float gasDensity = totalWeight * energy;

        if (gasDensity > 0.001) {
            vec3 gasColor = cosmicPalette(neb * 0.4 + warp * 0.6 + arms * 0.2, tint);
            // 叠加黑洞喷流的炽热蓝色
            gasColor = mix(gasColor, vec3(0.4, 0.9, 1.0), (jet * 1.5) / max(totalWeight, 0.001));

            // 应用暗尘带吸收，强化立体感
            gasColor *= (1.0 - darkDust);

            // 非线性透明度，让云雾边缘更丝滑
            float gasAlpha = 1.0 - exp(-pow(gasDensity * stepNorm * 4.0, 1.2));
            accumulated += (gasColor * (1.0 + armWeight) + rimTint * 0.012) * gasAlpha * transmittance;
            // 尘埃带遮挡背景光（钳制到 [0,1]：Intensity 拉高时 1 - gasAlpha*(1+darkDust*0.5) 可能变负，
            // 会让紧随其后的星光项反号变成"减光"，这里兜底）
            transmittance *= clamp(1.0 - gasAlpha * (1.0 + darkDust * 0.5), 0.0, 1.0);
        }

        if (starWeight > 0.0) {
            float starAlpha = 1.0 - exp(-starWeight * stepSize * 4.0 * energy);
            // 星光是自发光，不受尘埃严重削弱，甚至能照亮局部
            accumulated += starColor * starAlpha * 2.5 * transmittance;
        }

        if (transmittance < 0.01) break;
    }

    float amount = clamp(1.0 - transmittance, 0.0, 1.0);
    vec3 integrated = accumulated / max(amount, 0.001);

    // HDR Bloom 叠加组合
    vec3 result = mix(background, integrated, amount);

    // 视界绝对遮蔽后，将光子环与吸积盘以高光加成叠上去
    result *= 1.0 - coreMask * min(energy, 1.0);
    result += coreGlow;
    result += rimGlow;

    // 深空底光辐射已经并入体积积分（跟着星云密度走），这里不再做全屏平铺 ——
    // 那种 `rimTint * amount * 0.03` 的写法是纯屏幕铺色，会跟着玩家一直糊在视野上。

    return result;
}
// ────────────────────────── 寂灭之月领域 (Lunar Domain, mode 18) ──────────────────────────
// 与宇宙领域对立：不做透光体积积分，而是用 SDF 实体步进雕刻一颗固态月亮 ——
// 环形山/月海/碎石由噪声等值线生成，太空级硬阴影明暗交界线，暗部只留一丝地球反照的冷光，
// 领域内漂浮反重力月壤，月球边缘有月食级逆光月晕。
// 注意：本文件既有的 fbm / valueNoise 只接受 vec2，3D 噪声复用 catNoise3 / catFbm3。
float lunarTerrain(vec3 p) {
    float terrain = catFbm3(p * 3.5) * 0.30;

    // 环形山：噪声等值线边界拱起成环，等值线内圈下陷成坑底
    float n1 = catFbm3(p * 8.0 + vec3(1.0));
    float craters = pow(abs(n1 - 0.5) * 2.0, 2.5);
    float pits = smoothstep(0.4, 0.0, abs(n1 - 0.5));
    terrain += craters * 0.25;
    terrain -= pits * 0.15;

    // 月壤碎石的颗粒感
    terrain += catFbm3(p * 24.0) * 0.05;
    return terrain;
}

float mapMoon(vec3 p, vec3 moonCenter, float radius) {
    vec3 q = (p - moonCenter) / radius;
    float baseDist = length(q) - 1.0;
    // 只有贴近表面才做地形置换，远离时直接返回球面距离
    if (baseDist < 0.2 && baseDist > -0.1) {
        return (baseDist - lunarTerrain(q) * 0.12) * radius;
    }
    return baseDist * radius;
}

vec3 getMoonNormal(vec3 p, vec3 moonCenter, float radius) {
    vec2 e = vec2(0.005 * radius, 0.0);
    return normalize(vec3(
            mapMoon(p + e.xyy, moonCenter, radius) - mapMoon(p - e.xyy, moonCenter, radius),
            mapMoon(p + e.yxy, moonCenter, radius) - mapMoon(p - e.yxy, moonCenter, radius),
            mapMoon(p + e.yyx, moonCenter, radius) - mapMoon(p - e.yyx, moonCenter, radius)
    ));
}

vec3 applyLunarDomain(vec3 color, vec3 scenePos, vec2 uv, vec3 center, vec4 data, vec3 tint, float edge) {
    float sphereR = max(data.y, 0.001);
    float progress = clamp(data.z, 0.0, 1.0);
    float intensity = max(data.w, 0.0);
    float energy = intensity * (1.0 - smoothstep(0.86, 1.0, progress));
    if (energy <= 0.002) return color;

    vec3 ro = vec3(0.0);
    vec3 rd = normalize(viewRay(uv));
    float maxDistance = length(scenePos);

    vec3 toCenter = center - ro;
    float centerProj = dot(rd, toCenter);
    float centerDist = length(toCenter);
    float impact = sqrt(max(centerDist * centerDist - centerProj * centerProj, 0.0));

    // ── 领域外壳：银灰细边 + 冷日冕 ──
    // 判定与宇宙领域一致：壳必须在镜头前方、不晚于首个可见表面，且镜头离开领域球才有"轮廓"。
    // 只看 impact 的话，领域跑到背后或被墙挡住时，边会被画到任意距离的地板和墙上。
    vec3 rimTint = mix(tint, vec3(0.60, 0.65, 0.70), 0.5);
    float outside = max(impact - sphereR, 0.0);
    // 与宇宙领域同理：日冕收窄到紧贴轮廓，避免淡银色铺满视野周边
    float corona = exp(-outside / max(sphereR * 0.11, 0.30));
    corona *= 1.0 - smoothstep(0.30, 0.60, outside / sphereR);

    float shellSpan = sqrt(max(sphereR * sphereR - impact * impact, 0.0));
    float shellNear = centerProj - shellSpan;
    float shellFar = centerProj + shellSpan;
    float shellVisible = (shellFar > 0.0 && shellNear < maxDistance) ? 1.0 : 0.0;
    float silhouette = smoothstep(1.0, 1.30, centerDist / sphereR);

    // 边界噪声改用世界空间方向，避免纹理跟着镜头滑动
    float tShell = max(shellNear, 0.0);
    vec3 shellVec = rd * tShell - center;
    float shellLen = length(shellVec);
    vec3 shellDir = shellLen > 0.0001 ? shellVec / shellLen : vec3(0.0, 1.0, 0.0);
    float edgeRing = exp(-abs(impact - sphereR) / (sphereR * 0.02))
            * (0.8 + 0.2 * valueNoise(shellDir.xz * 6.0 + vec2(Time * 0.05, -Time * 0.04)))
            * shellVisible * silhouette;
    vec3 domainRimGlow = rimTint * edgeRing * 0.8 * energy;

    vec3 oc = ro - center;
    float b = dot(oc, rd);
    float c = dot(oc, oc) - sphereR * sphereR;
    float h = b * b - c;
    if (h < 0.0) {
        return color + domainRimGlow + rimTint * corona * 0.3 * energy * shellVisible * silhouette;
    }

    h = sqrt(h);
    float tNear = max(-b - h, 0.0);
    float tFar = min(-b + h, maxDistance);
    if (tNear >= tFar) return color;

    // ── SDF 实体月球：挂在领域中心上方，"压在头顶" ──
    // 与球心同心时，领域挂自己身上会让镜头直接落在月球实体内部（整屏只剩一张灰面），
    // 所以月球整体上移 0.52R、半径取 0.33R，正好压在领域中心正上方，且整个月亮仍在领域球内。
    vec3 moonCenter = center + vec3(0.0, sphereR * 0.52, 0.0);
    float moonRadius = sphereR * 0.33;
    vec3 lightDir = normalize(vec3(cos(Time * 0.1), 0.3, sin(Time * 0.1)));

    vec3 toMoon = moonCenter - ro;
    float moonProj = dot(rd, toMoon);
    float moonDist = sqrt(max(dot(toMoon, toMoon) - moonProj * moonProj, 0.0));
    float moonSpan = sqrt(max(moonRadius * moonRadius - moonDist * moonDist, 0.0));
    float cameraOutsideMoon = length(toMoon) > moonRadius ? 1.0 : 0.0;
    // 实心月球：近端壳交在镜头前方、且不被近处几何挡住
    float moonVisible = cameraOutsideMoon
            * ((moonProj + moonSpan > 0.0 && moonProj - moonSpan < maxDistance) ? 1.0 : 0.0);
    // 月晕：发光位置在月球外缘，要按它自己的三维深度判遮挡，
    // 否则擦着月亮旁边过去的射线会把辉光印到近处的墙面上（同一类"无限范围"问题）
    float haloOutside = max(moonDist - moonRadius, 0.0);
    float haloDepth = sqrt(moonProj * moonProj + haloOutside * haloOutside);
    float moonHaloVisible = cameraOutsideMoon
            * ((moonProj + moonSpan > 0.0 && haloDepth < maxDistance) ? 1.0 : 0.0);

    bool hitMoon = false;
    vec3 hitPos = vec3(0.0);
    if (moonVisible > 0.5) {
        float t = tNear;
        for (int i = 0; i < 54; i++) {
            vec3 p = ro + rd * t;
            float d = mapMoon(p, moonCenter, moonRadius);
            if (d < 0.001 * t) {
                hitMoon = true;
                hitPos = p;
                break;
            }
            t += d * 0.65; // 保守步长：地形置换让 SDF 略微低估，防止穿模
            if (t > tFar) break;
        }
    }

    // 领域内部压暗，做出深空死寂的底
    vec3 domainBg = mix(color * 0.1, vec3(0.01, 0.012, 0.015), min(energy, 1.0));
    vec3 finalColor = domainBg;

    if (hitMoon) {
        vec3 norm = getMoonNormal(hitPos, moonCenter, moonRadius);
        vec3 q = normalize(hitPos - moonCenter);

        float albedoNoise = clamp(lunarTerrain(q), 0.0, 1.0);
        vec3 albedo = mix(vec3(0.25, 0.26, 0.28), vec3(0.85, 0.88, 0.90), albedoNoise);

        // 太空没有大气散射：明暗交界线几乎是硬边
        float ndotl = dot(norm, lightDir);
        float harshLight = smoothstep(0.0, 0.05, ndotl);
        // 月壤的逆向反射：正对光源观察时泛起高光
        float backscatter = pow(max(dot(rd, -lightDir), 0.0), 4.0) * 0.5;
        vec3 litColor = albedo * (harshLight + backscatter) * 1.5;
        vec3 darkColor = albedo * vec3(0.02, 0.03, 0.05); // 暗部只留一丝地球反照

        // 暗部边缘的菲涅尔轮廓光，强化几何压迫感
        float rim = pow(1.0 - max(dot(norm, -rd), 0.0), 3.0);
        vec3 rimColor = rimTint * rim * (1.0 - harshLight) * 0.8;

        finalColor = mix(darkColor, litColor, harshLight) + rimColor;
        finalColor *= energy;
    } else {
        // 月食级逆光月晕：只有月球真的可见时才出现（否则就是一层跟着视角的光幕）
        // 只留紧贴月缘的逆光辉光：原来 0.6*月半径 的尾巴会在整个上半屏铺一层淡银色，
        // 而月亮一直悬在玩家头顶，看起来就是"跟着视角移动的淡色影子"。
        float glowIntensity = exp(-haloOutside / (moonRadius * 0.28))
                * (1.0 - smoothstep(0.55, 1.00, haloOutside / moonRadius));
        vec3 moonHalo = rimTint * glowIntensity * pow(max(dot(rd, lightDir), 0.0), 4.0) * 1.5 * moonHaloVisible;

        // 反重力月壤：坐标取领域中心相对量，跟镜头平移无关（不会跟着玩家游动）
        float ash = 0.0;
        for (int j = 1; j <= 3; j++) {
            float fj = float(j);
            vec3 ashPos = (ro + rd * (tNear + sphereR * 0.3 * fj) - center) * (3.0 / sphereR)
                    + vec3(Time * 0.05, -Time * 0.15, Time * 0.02) * fj;
            ash += pow(max(catNoise3(ashPos * 8.0), 0.0), 12.0) * (1.0 / fj);
        }

        finalColor += moonHalo * energy + vec3(0.9, 0.95, 1.0) * ash * 3.0 * energy;
    }

    // 与场景融合：贴着领域边缘（弦长很短）时淡出
    float amount = smoothstep(tNear, tNear + sphereR * 0.1, tFar);
    vec3 result = mix(color, finalColor, amount * min(energy, 1.0));
    result += domainRimGlow;
    return result;
}
void main() {
    vec2 uv = gl_FragCoord.xy / InSize;
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
    vec4 data = EffectData0;
    int mode = int(data.x + 0.5);
    vec3 center = EffectCenter0;
    vec3 tint = EffectColor0;

    // 每个特效各占一次 draw，这里只处理当前绑定的这一个。
    // mode 7（领域雾）仍然先于其它模式处理，保持原来「雾在底层」的层次。
    if (mode == 7) {
        color = applyMalevolentShrineDomain(color, scenePos, uv, center, data, tint);
    } else if (sky) {
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
        } else if (mode == 17) {
            color = applyCosmicDomain(color, scenePos, uv, center, data, tint, edge);
        } else if (mode == 18) {
            color = applyLunarDomain(color, scenePos, uv, center, data, tint, edge);
        }
    } else if (mode == 0) {
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
    } else if (mode == 17) {
        color = applyCosmicDomain(color, scenePos, uv, center, data, tint, edge);
    } else if (mode == 18) {
        color = applyLunarDomain(color, scenePos, uv, center, data, tint, edge);
    } else {
        color = applyMalevolentShrineTargetGlow(color, scenePos, uv, center, data, tint, edge);
    }
    fragColor = vec4(color, base.a);
}
