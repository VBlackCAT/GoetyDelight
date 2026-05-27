#version 150

// Original Shadertoy-style flame by anatole duprat - XT95/2013.
// Adapted here into an entity-local volumetric raymarch shader.

uniform vec4 ColorModulator;
uniform vec3 CameraLocal;
uniform vec3 FlameColor;
uniform vec3 CoreColor;
uniform vec3 TipColor;
uniform vec3 SmokeColor;
uniform float intensity;

in vec3 localPos;
in float vertexAlpha;
in vec2 texCoord;
in float time;

out vec4 fragColor;

float noise(vec3 p) {
    vec3 i = floor(p);
    vec4 a = dot(i, vec3(1.0, 57.0, 21.0)) + vec4(0.0, 57.0, 21.0, 78.0);
    vec3 f = cos((p - i) * acos(-1.0)) * -0.5 + 0.5;
    a = mix(sin(cos(a) * a), sin(cos(1.0 + a) * (1.0 + a)), f.x);
    a.xy = mix(a.xz, a.yw, f.y);
    return mix(a.x, a.y, f.z);
}

float sphere(vec3 p, vec4 spr) {
    return length(spr.xyz - p) - spr.w;
}

float flame(vec3 p) {
    vec3 q = p;
    q.y = q.y * 1.18 + 0.12;
    float d = sphere(q * vec3(1.0, 0.52, 1.0), vec4(0.0, -0.58, 0.0, 0.82));
    float y = clamp(p.y + 1.0, 0.0, 2.0);
    float lick = noise(p * vec3(1.35, 1.70, 1.35) + vec3(0.0, -time * 2.25, 0.0));
    float detail = noise(p * 4.0 + vec3(time * 0.20, -time * 1.10, -time * 0.17));
    float twist = sin(p.x * 5.0 + p.z * 3.0 + time * 3.0) * 0.035 * y;
    return d + (lick + detail * 0.55) * 0.18 * y + twist;
}

vec2 intersectBox(vec3 ro, vec3 rd) {
    vec3 raySign = sign(rd);
    raySign = mix(vec3(1.0), raySign, step(vec3(0.0001), abs(rd)));
    vec3 safeRd = max(abs(rd), vec3(0.0001)) * raySign;
    vec3 inv = 1.0 / safeRd;
    vec3 t0 = (-vec3(1.0) - ro) * inv;
    vec3 t1 = ( vec3(1.0) - ro) * inv;
    vec3 tmin = min(t0, t1);
    vec3 tmax = max(t0, t1);
    float nearT = max(max(tmin.x, tmin.y), tmin.z);
    float farT = min(min(tmax.x, tmax.y), tmax.z);
    return vec2(nearT, farT);
}

vec4 raymarch(vec3 ro, vec3 rd, float startT, float endT) {
    vec3 color = vec3(0.0);
    float alpha = 0.0;
    float span = max(endT - startT, 0.001);
    float stepSize = span / 58.0;
    float t = max(startT, 0.0);

    for (int i = 0; i < 64; i++) {
        if (t > endT || alpha > 0.96) {
            break;
        }

        vec3 p = ro + rd * t;
        float sdf = flame(p);
        float density = smoothstep(0.10, -0.055, sdf);
        float y01 = clamp((p.y + 1.0) * 0.5, 0.0, 1.0);
        float core = smoothstep(0.12, -0.12, sdf + length(p.xz) * 0.08);
        float flicker = 0.78 + 0.22 * sin(time * 12.0 + p.y * 8.0 + noise(p * 3.0) * 5.0);
        float fadeTop = 1.0 - smoothstep(0.72, 1.0, y01);
        float fadeBottom = smoothstep(-1.0, -0.78, p.y);
        float sampleAlpha = density * flicker * fadeTop * fadeBottom * 0.105;

        vec3 sampleColor = mix(FlameColor, CoreColor, core);
        sampleColor = mix(sampleColor, TipColor, smoothstep(0.45, 0.95, y01) * (0.34 + density * 0.22));
        sampleColor = mix(sampleColor, SmokeColor, smoothstep(0.80, 1.0, y01) * (1.0 - density) * 0.38);

        color += (1.0 - alpha) * sampleColor * sampleAlpha * (1.8 + core * 1.5);
        alpha += (1.0 - alpha) * sampleAlpha;
        t += stepSize;
    }

    float halo = pow(max(0.0, 1.0 - length(localPos.xz) * 0.72), 2.7)
            * smoothstep(-0.96, -0.20, localPos.y)
            * (1.0 - smoothstep(0.55, 1.0, localPos.y));
    color += FlameColor * halo * 0.10;
    alpha = max(alpha, halo * 0.10);

    return vec4(color, alpha);
}

void main() {
    vec3 ro = CameraLocal;
    vec3 rd = normalize(localPos - ro);
    vec2 hit = intersectBox(ro, rd);

    if (hit.x > hit.y || hit.y < 0.0) {
        discard;
    }

    vec4 flameColor = raymarch(ro, rd, hit.x, hit.y);
    flameColor.rgb *= intensity;
    flameColor.a *= vertexAlpha;
    fragColor = flameColor * ColorModulator;
}
