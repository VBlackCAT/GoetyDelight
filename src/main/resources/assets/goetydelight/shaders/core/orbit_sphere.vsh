#version 150

in vec3 Position;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float iTime;

out vec4 vertexColor;
out vec3 localNormal;
out float time;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    localNormal = normalize(mat3(ModelViewMat) * Normal);
    time = iTime;
}
