varying vec3 N; // Normal vector
varying vec3 p; // Surface point
uniform vec3 camera_position; // Set in Java application
varying vec2 texture_coordinate; // Texture coordinate
uniform sampler2D my_color_texture; // Texture object
uniform float transparency; 

/**
 * Fragment shader: textured surface, lighting: Phong shading with Phong model.
 */
void main (void)
{
    
    // Read reflection material properties from OpenGL
    vec4 reflectionAmbient = gl_FrontMaterial.ambient;
    vec4 reflectionDiffuse = gl_FrontMaterial.diffuse;
    vec4 reflectionSpecular = gl_FrontMaterial.specular;
    vec4 textureColor = texture2D(my_color_texture, texture_coordinate);
    
    // Determine number of active lights
    int numberOfLights = 0;
    for ( int i = 0; i < gl_MaxLights; i++ ){
        if ( gl_LightSource[i].diffuse.x > -0.1 ){
            numberOfLights++;
        }
    }
    
    gl_FragColor = vec4(0,0,0,1);
    
    // Ambient color
    vec3 ambient;
    float ambientFactor = 0.5;
    ambient.x = reflectionAmbient.x * textureColor.x * ambientFactor;
    ambient.y = reflectionAmbient.y * textureColor.y * ambientFactor;
    ambient.z = reflectionAmbient.z * textureColor.z * ambientFactor;
    gl_FragColor.xyz += ambient;
    
    // Add diffuse and specular for each light
    for ( int i = 0; i < numberOfLights; i++ ){
        bool isSpot = gl_LightSource[i].spotCutoff > 0.1;
        bool isDirectionalLight = gl_LightSource[i].diffuse.w < 0.0;
        bool isPointLight = !isSpot && !isDirectionalLight;
        bool isActive = gl_LightSource[i].diffuse.x > -0.1;
        
        if ( !isActive ){
            continue;
        }
        
        // Point light, Spotlight
        vec3 L = normalize(gl_LightSource[i].position.xyz - p);
        if ( isDirectionalLight ){
            L =  normalize( gl_LightSource[i].position.xyz );
        }
        
        // Diffuse
        vec3 diffuse = vec3(0,0,0);
        if ( dot( N, L ) > 0.0 ){
            diffuse.x = reflectionDiffuse.x * textureColor.x * gl_LightSource[i].diffuse.x;
            diffuse.y = reflectionDiffuse.y * textureColor.y * gl_LightSource[i].diffuse.y;
            diffuse.z = reflectionDiffuse.z * textureColor.z * gl_LightSource[i].diffuse.z;
            diffuse = diffuse * dot( N, L );// / float(numberOfLights);
        }
        
        // Specular
        vec3 E = normalize( camera_position - p );
        vec3 R = normalize( reflect( L, N) );
        vec3 specular;
        specular.x = reflectionSpecular.x * gl_LightSource[i].specular.x;
        specular.y = reflectionSpecular.y * gl_LightSource[i].specular.y;
        specular.z = reflectionSpecular.z * gl_LightSource[i].specular.z;
        specular = specular * pow(abs(dot(R,E)), gl_FrontMaterial.shininess);// / float(numberOfLights);
        
        if ( isSpot ){
            float distance = dot( p, gl_LightSource[i].spotDirection) - dot(gl_LightSource[i].position.xyz, gl_LightSource[i].spotDirection);
            bool isInSpot = dot(-L, normalize(gl_LightSource[i].spotDirection)) > cos(gl_LightSource[i].spotCutoff);
            if ( isInSpot && distance > 0.0 ){
                gl_FragColor.xyz += diffuse + specular;
            }
        } else if ( isDirectionalLight ){
            gl_FragColor.xyz += diffuse + specular;
        } else if (isPointLight ){
            gl_FragColor.xyz += diffuse + specular;
        }
    }
    
    gl_FragColor = clamp( gl_FragColor, 0.0, 1.0 );
    gl_FragColor.a = transparency;
    
    //gl_FragColor = textureColor;
}