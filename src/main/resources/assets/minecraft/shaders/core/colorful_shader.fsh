#version 150

uniform sampler2D Sample0;
uniform vec4 ColorModulator;
uniform vec4 color; // 添加颜色uniform

in vec2 texCoord;
in float vTime;
in float vIntensity;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sample0, texCoord);

    // 使用uniform变量创建效果
    vec3 rgbEffect = texColor.rgb * color.rgb;
    float alphaEffect = texColor.a * color.a;

    // 添加时间效果（示例：随时间变化的脉冲）
    float pulse = (sin(vTime) + 1.0) * 0.5 * vIntensity;
    rgbEffect = mix(rgbEffect, rgbEffect * 1.5, pulse);

    fragColor = vec4(rgbEffect, alphaEffect) * ColorModulator;
}