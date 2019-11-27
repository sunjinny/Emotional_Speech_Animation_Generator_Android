// rendermonkey default textured phong vertex shader
uniform mat4 matViewProjectionInverseTranspose;
uniform mat4 matViewProjection;
uniform sampler2D bumpMap;
uniform float iTime;
uniform vec3 fvEyeTranslation;
uniform vec3 fvEyePosition;
uniform vec3 fvEyeRotation;
uniform float fHeadNoddingAngle;
//index == 0 : body;
//index == 1 : eye
uniform int iIndex;

attribute vec4 rm_Vertex;
attribute vec4 rm_Normal;
attribute vec2 rm_TexCoord0;

varying vec2 Texcoord;
varying vec3 Normal;
varying vec3 FragPos;
varying vec3 ViewDir;
varying float normalA;

vec4 rotate(vec4 p, vec3 angle){
    float A = angle.x;
    float B = angle.y;
    float C = angle.z;
    mat4 rotataionMatrix = mat4(
        vec4( cos(B)*cos(C),                       -cos(B)*sin(C),                         sin(B),        0),
        vec4( sin(A)*sin(B)*cos(C) + cos(A)*sin(C),-sin(A)*sin(B)*sin(C) + cos(A)*cos(C), -sin(A)*cos(B), 0),
        vec4(-cos(A)*sin(B)*cos(C) + sin(A)*sin(C), cos(A)*sin(B)*sin(C) + sin(A)*cos(C),  cos(A)*cos(B), 0),
        vec4(0,0,0,1)
    );
    p *= rotataionMatrix;
    return p;
}

vec3 rotateX(vec3 p, float angle, float isHomo){
    mat4 rotateMat = mat4(
		vec4(1.0, 0.0,         0.0,        0.0),
		vec4(0.0, cos(angle), -sin(angle), 0.0),
		vec4(0.0, sin(angle),  cos(angle), 0.0),
		vec4(0.0, 0.0,         0.0,        1.0)
    );
    //vec4 tempP = vec4(p, isHomo) * rotateMat;
    vec4 tempP = rotateMat * vec4(p, isHomo);
    return tempP.xyz;
}

vec4 translatePos(vec4 p, vec3 t){
    mat4 translateMat = mat4(
    	vec4(1.0, 0.0, 0.0, t.x),
    	vec4(0.0, 1.0, 0.0, t.y),
        vec4(0.0, 0.0, 1.0, t.z),
        vec4(0.0, 0.0, 0.0, 1.0)
    );
    p *= translateMat;
    return p;
}


void main( void )
{
    vec4 ecPosition = rm_Vertex;
    float weight = texture2D(bumpMap, rm_TexCoord0.xy).z;
    vec3 bonePosition = vec3(0.0, 1.0, 0.0);

    ecPosition = rotate(ecPosition, radians(fvEyeRotation.xyz));

    ecPosition = translatePos(ecPosition, bonePosition);
    ecPosition = translatePos(ecPosition, fvEyeTranslation);
    ecPosition = rotate(ecPosition, vec3(sin(iTime * 0.1) * weight, 0, 0));
    //ecPosition = rotate(ecPosition, vec3(fHeadNoddingAngle * weight, 0, 0));
    ecPosition = translatePos(ecPosition, -fvEyeTranslation);
    ecPosition = translatePos(ecPosition, -bonePosition);

    FragPos = ecPosition.xyz;
    gl_Position = matViewProjection * ecPosition;
    Texcoord    = rm_TexCoord0.xy;

    //vec4 fvObjectPosition = matViewProjection * rm_Vertex;
    vec4 fvObjectPosition = matViewProjection * ecPosition;

    //Normal         = (matViewProjectionInverseTranspose * rm_Normal).xyz;

    ViewDir = fvEyePosition - fvObjectPosition.xyz;

    vec4 newNormal = rotate(vec4(rm_Normal.xyz, 0.0), vec3(sin(iTime * 0.1) * weight, 0, 0));

    newNormal.xyz = (matViewProjection * newNormal).xyz;


    Normal =  newNormal.xyz;
}