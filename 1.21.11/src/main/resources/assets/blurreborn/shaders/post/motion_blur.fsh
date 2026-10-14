#version 330

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

layout(std140) uniform BlurInfo {
    mat4 InvProj;
    mat4 InvRot;
    mat4 PrevRot;
    mat4 PrevProj;
    vec4 Delta;
    vec4 Params;
};

in vec2 texCoord;
out vec4 fragColor;

float nz(vec2 p) {
    return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
}

void main() {
    vec4 base = texture(InSampler, texCoord);
    float d = texture(DepthSampler, texCoord).r;
    vec4 pv = InvProj * vec4(texCoord * 2.0 - 1.0, d * 2.0 - 1.0, 1.0);
    pv /= pv.w;
    vec3 rel = (InvRot * vec4(pv.xyz, 0.0)).xyz;
    vec4 pc = PrevProj * (PrevRot * vec4(rel + Delta.xyz, 1.0));
    vec2 puv = pc.xy / pc.w * 0.5 + 0.5;
    vec2 vel = (texCoord - puv) * Params.x;
    float len = length(vel);
    if (pc.w <= 0.0 || len < 0.0004) {
        fragColor = base;
        return;
    }
    vel *= min(1.0, Params.y / len);
    float j = nz(gl_FragCoord.xy);
    vec3 sum = vec3(0.0);
    for (int i = 0; i < 14; i++) {
        float t = (float(i) + j) / 14.0 - 0.5;
        sum += texture(InSampler, texCoord + vel * t).rgb;
    }
    fragColor = vec4(sum / 14.0, base.a);
}