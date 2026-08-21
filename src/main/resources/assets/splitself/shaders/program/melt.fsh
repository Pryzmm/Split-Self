#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;
uniform float Amount;

in vec2 texCoord;
out vec4 fragColor;

float hash(float n) {
    return fract(sin(n * 127.1) * 43758.5453);
}

void main() {
    vec2 uv = texCoord;

    float col = floor(uv.x * 600.0);
    float drip = hash(col);

    float smear = uv.y * drip * Amount * 0.35;

    float wobble = sin(uv.y * 18.0 + Time * 1.5 + col) * 0.01 * Amount;

    vec2 sampleUv = clamp(vec2(uv.x + wobble, uv.y - smear), 0.0, 1.0);
    fragColor = texture(DiffuseSampler, sampleUv);
}