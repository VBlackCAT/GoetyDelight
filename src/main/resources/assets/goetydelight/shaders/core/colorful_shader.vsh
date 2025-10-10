#version 150

in vec3 Position;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float iTime; // 时间uniform
uniform float intensity; // 强度uniform
uniform vec4 color; // 颜色uniform

out vec2 texCoord;
out float vTime;
out float vIntensity;
out vec4 vColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord = UV0;
    vTime = iTime;
    vIntensity = intensity;
    vColor = color; // 传递颜色到片段着色器
}