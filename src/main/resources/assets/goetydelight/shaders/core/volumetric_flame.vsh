#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float iTime;

out vec3 localPos;
out float vertexAlpha;
out vec2 texCoord;
out float time;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    localPos = Color.rgb * 2.0 - 1.0;
    vertexAlpha = Color.a;
    texCoord = UV0;
    time = iTime;
}
