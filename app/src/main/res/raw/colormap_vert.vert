// rendermonkey default textured phong vertex shader
uniform mat4 matViewProjectionInverseTranspose;
uniform mat4 matViewProjection;
uniform sampler2D bumpMap;
uniform float iTime;
uniform vec3 fvEyeTranslation;
uniform vec3 fvEyePosition;

attribute vec4 rm_Vertex;
attribute vec4 rm_Normal;
attribute vec2 rm_TexCoord0;

varying vec2 Texcoord;
varying vec3 Normal;
varying vec3 FragPos;
varying vec3 ViewDir;





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

vec3 translatePos(vec3 p, vec3 t, float isHomo){
    mat4 translateMat = mat4(
    	vec4(1.0, 0.0, 0.0, t.x),
    	vec4(0.0, 1.0, 0.0, t.y),
        vec4(0.0, 0.0, 1.0, t.z),
        vec4(0.0, 0.0, 0.0, 1.0)
    );
    vec4 tempP = vec4(p, isHomo) * translateMat;
    return tempP.xyz;
}


void main( void )
{
    vec3 ecPosition = rm_Vertex.xyz;
    float weight = texture2D(bumpMap, rm_TexCoord0.xy).z * 0.6;
    vec3 bonePosition = vec3(0.0, 1.0, 0.0);
    //float newTime = sin(iTime * 0.05);
    float newTime = 0.0;


    ecPosition = translatePos(ecPosition, bonePosition, 1.0);
    ecPosition = translatePos(ecPosition, fvEyeTranslation, 1.0);
    ecPosition = rotateX(ecPosition, newTime * weight, 1.0);
    ecPosition = translatePos(ecPosition, -fvEyeTranslation, 1.0);
    ecPosition = translatePos(ecPosition, -bonePosition, 1.0);

    FragPos = ecPosition;
    gl_Position = matViewProjection * vec4(ecPosition, 1.0);
    Texcoord    = rm_TexCoord0.xy;

    //vec4 fvObjectPosition = matViewProjection * rm_Vertex;
    vec4 fvObjectPosition = matViewProjection * vec4(ecPosition, 1.0);

    //Normal         = (matViewProjectionInverseTranspose * rm_Normal).xyz;

    ViewDir = fvEyePosition - fvObjectPosition.xyz;

    vec3 newNormal = rotateX(rm_Normal.xyz, newTime * weight, 0.0);

    newNormal = (matViewProjection * vec4(newNormal, 0.0)).xyz;



    Normal =  newNormal;
}