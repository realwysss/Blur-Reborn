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
    vec4 Extra;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    bool mv = Params.z > 0.5;
    float d = mv ? texture(DepthSampler, texCoord).r : 1.0;
    vec4 pv = InvProj * vec4(texCoord * 2.0 - 1.0, d * 2.0 - 1.0, 1.0);
    pv /= pv.w;
    vec3 rel = (InvRot * vec4(pv.xyz, 0.0)).xyz;
    vec3 dl = mv ? Delta.xyz : vec3(0.0);
    vec4 pc = PrevProj * (PrevRot * vec4(rel + dl, 1.0));
    vec2 puv = pc.xy / pc.w * 0.5 + 0.5;
    vec2 vel = (texCoord - puv) * Params.x;
    float r = clamp(length((texCoord - 0.5) * vec2(Extra.w, 1.0)) / 0.8, 0.0, 1.0);
    vel *= mix(1.0, smoothstep(0.0, 1.0, r), Extra.y);
    float len = length(vel);
    float px = len * Extra.z;
    if (pc.w <= 0.0 || px < 0.5) {
        fragColor = vec4(0.0);
        return;
    }
    vel *= min(1.0, Params.y / len);
    px = length(vel) * Extra.z * 0.5;
    int n = int(clamp(ceil(px / 1.5), 3.0, Extra.x));
    vec3 sum = vec3(0.0);
    for (int i = 0; i < 24; i++) {
        if (i >= n) break;
        float t = (float(i) + 0.5) / float(n) - 0.5;
        sum += texture(InSampler, texCoord + vel * t).rgb;
    }
    fragColor = vec4(sum / float(n), smoothstep(0.5, 2.0, px * 2.0));
}
