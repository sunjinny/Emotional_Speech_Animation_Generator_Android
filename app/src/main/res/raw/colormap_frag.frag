// rendermonkey default textured phong fragment shader
#ifdef GL_FRAGMENT_PRECISION_HIGH
   // Default precision
   precision highp float;
#else
   precision mediump float;
#endif


uniform int  iTextured;

uniform vec4 fvAmbient;
uniform vec4 fvSpecular;
uniform vec4 fvDiffuse;
uniform float fSpecularPower;
uniform vec3 fvLightPosition[2];
//index == 0 : body;
//index == 1 : eye
//index == 2 : hair
//index == 3 : cloth
uniform int iIndex;

uniform sampler2D baseMap;
uniform sampler2D bumpMap;
uniform sampler2D normalMap;

varying vec2 Texcoord;
varying vec3 Normal;
varying vec3 FragPos;
varying vec3 ViewDir;





const float PI = 3.14159265359;

// ----------------------------------------------------------------------------
float DistributionGGX(vec3 N, vec3 H, float roughness)
{
    float a = roughness*roughness;
    float a2 = a*a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;

    float nom   = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;

    return nom / denom;
}
// ----------------------------------------------------------------------------
float GeometrySchlickGGX(float NdotV, float roughness)
{
    float r = (roughness + 1.0);
    float k = (r*r) / 8.0;

    float nom   = NdotV;
    float denom = NdotV * (1.0 - k) + k;

    return nom / denom;
}
// ----------------------------------------------------------------------------
float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness)
{
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2 = GeometrySchlickGGX(NdotV, roughness);
    float ggx1 = GeometrySchlickGGX(NdotL, roughness);

    return ggx1 * ggx2;
}
// ----------------------------------------------------------------------------
vec3 fresnelSchlick(float cosTheta, vec3 F0)
{
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}
// ----------------------------------------------------------------------------


float hash13(vec3 p3)
{
	p3  = fract(p3 * .1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float hash12(vec2 p)
{
	vec3 p3  = fract(vec3(p.xyx) * .1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

///  3 out, 2 in...
vec3 hash32(vec2 p)
{
	vec3 p3 = fract(vec3(p.xyx) * vec3(.1031, .1030, .0973));
    p3 += dot(p3, p3.yxz+33.33);
    return fract((p3.xxy+p3.yzz)*p3.zyx);
}

///  3 out, 3 in...
vec3 hash33(vec3 p3)
{
	p3 = fract(p3 * vec3(.1031, .1030, .0973));
    p3 += dot(p3, p3.yxz+33.33);
    return fract((p3.xxy + p3.yxx)*p3.zyx);

}

void main( void )
{
    vec4 texture = texture2D(baseMap, Texcoord);
    vec3 albedo = pow(texture.xyz, vec3(2.2));
    vec3 metallic = vec3(0.2);
    float roughness = 0.0;
    if(iIndex == 1){
        roughness = 0.2;
    }else{
        roughness = clamp(hash13(albedo) * 0.8, 0.5, 1.0);
    }
    vec3 col = vec3(0.0);

    vec3 N = normalize(Normal + hash33(albedo * 0.5) * 0.2);
    //vec3 N = normalize(Normal);
    //index == 0 : body;
    //index == 1 : eye
    //index == 2 : hair
    //index == 3 : cloth
    //index == 4 : mouth
    if(iIndex == 0 || iIndex == 2){
        //N = normalize((texture2D(normalMap, Texcoord).xyz-0.5)*2.0 + N);
    }

    if(gl_FrontFacing == false)
        N *= -1.0;
    vec3 V = normalize(ViewDir);
    vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo, metallic);

    // reflectance equation
    vec3 Lo = vec3(0.0);
    for(int i = 0; i < 2; ++i)
    {

        vec3 L = normalize(fvLightPosition[i] - FragPos);
        vec3 H = normalize(V + L);
        vec3 radiance = vec3(4.5 * float(i + 1));


        // Cook-Torrance BRDF
        float NDF = DistributionGGX(N, H, roughness);
        float G   = GeometrySmith(N, V, L, roughness);
        vec3 F     = fresnelSchlick(max(dot(H, V), 0.0), F0);

        vec3 kS = F;
        vec3 kD = vec3(1.0) - kS;
        kD *= 1.0 - metallic;

        vec3 numerator    = NDF * G * F;
        float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0);
        vec3 specular     = numerator / max(denominator, 0.001);

        // add to outgoing radiance Lo
        float NdotL = max(dot(N, L), 0.0);
        Lo += (kD * albedo / PI + specular) * radiance * NdotL;
    }
    vec3 ambient = vec3(0.3) * albedo * 1.0; //* ao;
    col = ambient + Lo;

/*
    for(int i = 0; i < 2; ++i)
    {
        vec3 L = normalize(fvLightPosition[i] - FragPos);
        float I = pow(clamp(dot(V, -(L + N * 1.0)), 0.0, 1.0), 8.0) * 0.1;
        float oneDiv = 1.0/255.0;
        vec3 lightCol = vec3(235, 64, 52) * oneDiv;

        col += I * lightCol;
    }
*/
    vec3 L = normalize(-ViewDir + vec3(40.0, 20.0, 30.0));
    float I = pow(clamp(dot(V, -(L + N * 1.0)), 0.0, 1.0), 16.0) * 0.1;
    float oneDiv = 1.0/255.0;
    vec3 lightCol = vec3(235, 255, 255) * oneDiv;

    //col += I * lightCol;


    //gamma correction
    col = col / (col + vec3(1.0));
    col = pow(col, vec3(1.0/2.2));

   gl_FragColor = vec4(col, texture.a);
}


/*
void main( void )
{
    vec3  fvNormal         = normalize( Normal );
    if(gl_FrontFacing==false)
        fvNormal= -fvNormal;

    vec3 fvViewDirection  = normalize(ViewDir);
    vec3  fvLightDirection = normalize(fvLightPosition - FragPos);

    float fNDotL           = dot( fvNormal, fvLightDirection );


    vec3  fvReflection     = normalize( ( ( 2.0 * fvNormal ) * fNDotL ) - fvLightDirection );
    float fRDotV          = max( 0.0, dot( fvReflection, fvViewDirection ) );

    //fresnel start
    //F0 for skin is 0.028
    vec3 H = normalize(fvViewDirection + fvLightDirection);
    float spec = pow(max(dot(Normal, H), 0.0), fSpecularPower);
    //float F0 = 0.028;
    float F0 = 0.028;

    float base = 1.0 - dot(fvViewDirection, H);
    float exponential = pow(base, 5.0);
    float fresnel = exponential + F0 * (1.0 - exponential);
    //fresnel end

    vec4  fvBaseColor      = texture2D( baseMap, Texcoord);

    vec4  fvTotalAmbient   = fvAmbient * fvBaseColor;

    vec4  fvTotalDiffuse   = fvDiffuse* fNDotL;
    if(iTextured==1)
    {
       fvTotalDiffuse    = fvTotalDiffuse*fvBaseColor;
    }
   //vec4  fvTotalSpecular  = fvSpecular * ( pow( fRDotV, fSpecularPower ) ) * 2.0;
   vec4  fvTotalSpecular  = fvSpecular * spec * fresnel;
   //vec4 fvTotalSpecular = fvSpecular * pow(clamp(dot(H, fvNormal),0.0, 1.0), fSpecularPower);
   //gl_FragColor = ( fvTotalAmbient + fvTotalDiffuse + fvTotalSpecular ) * 0.0001 +  1.0 -  dot(fvViewDirection, Normal);
   //gl_FragColor = ( fvTotalAmbient + fvTotalDiffuse + fvTotalSpecular ) * 0.0001 +  vec4(fvViewDirection, 1.0);
   gl_FragColor = ( fvTotalAmbient + fvTotalDiffuse + fvTotalSpecular );
      //gl_FragColor = ( fvTotalAmbient + fvTotalDiffuse + fvTotalSpecular ) * 0.0001 +  vec4(vec3(dot(-vec3(0.5,-0.3,-1), Normal)), 1.0);
}
*/