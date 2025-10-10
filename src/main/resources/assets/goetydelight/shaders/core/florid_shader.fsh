#version 150

uniform vec4 ColorModulator;
uniform float iTime;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    // 使用纹理坐标作为归一化像素坐标
    vec2 uv = texCoord;

    // 创建随时间变化的渐变颜色
    vec3 col = 0.5 + 0.5 * cos(iTime + uv.xyx + vec3(0, 2, 4));

    // 输出最终颜色
    fragColor = vec4(col, 1.0) * ColorModulator;
}