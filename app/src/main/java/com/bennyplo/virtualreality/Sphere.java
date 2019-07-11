package com.bennyplo.virtualreality;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES32;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class Sphere {
    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;"+"uniform mat4 uMVPMatrix;varying vec4 vColor;" +
                    "attribute vec3 aVertexNormal;"+
                    "attribute vec4 aVertexColor;"+
                    "uniform vec3 uPointLightingLocation;"+
                    "uniform vec3 uPointLightingColor;"+
                    "uniform vec3 uAmbientColor;"+
                    "varying vec3 vLightWeighting;"+
                    "uniform vec3 uDiffuseLightLocation;"+
                    "   uniform vec4 uDiffuseColor;" +//color of the diffuse light
                    "varying vec4 vDiffuseColor;" +
                    "varying float vPointLightWeighting;"+
                    "varying float vDiffuseLightWeighting;" +
                    "   uniform vec3 uAttenuation;"+//light attenuation
                    "uniform vec4 uSpecularColor;"+
                    "varying vec4 vSpecularColor;" +
                    "varying float vSpecularLightWeighting; "+
                    "uniform vec3 uSpecularLightLocation;"+
                    "uniform float uMaterialShininess;"+
                    "attribute vec2 aTextureCoordinate; "+//texture coordinate
                    "varying vec2 vTextureCoordinate;"+
                    "void main() {"+
                    "gl_Position = uMVPMatrix *vec4(aVertexPosition,1.0);" +
                    "vLightWeighting=vec3(1.0,1.0,1.0);     "+
                    "vec4 mvPosition=uMVPMatrix*vec4(aVertexPosition,1.0);"+
                    "vec3 lightDirection=normalize(uPointLightingLocation-mvPosition.xyz);" +
                    "vec3 diffuseLightDirection=normalize(uDiffuseLightLocation-mvPosition.xyz);"+
                    "    vec3 transformedNormal = normalize((uMVPMatrix * vec4(aVertexNormal, 0.0)).xyz);"+
                    "vLightWeighting=uAmbientColor;"+
                    "gl_PointSize = 40.0;"+
                    "vDiffuseColor=uDiffuseColor;" +
                    " vSpecularColor=uSpecularColor; "+
                    "float specularLightWeighting=0.0;" +
                    "  vec3 eyeDirection=normalize(-mvPosition.xyz);" +
                    "  vec3 specularlightDirection=normalize(uSpecularLightLocation-mvPosition.xyz);"+
                    "    vec3 inverseLightDirection = normalize(uPointLightingLocation);"+
                    "  vec3 reflectionDirection=reflect(-lightDirection,transformedNormal);" +
                    "vPointLightWeighting=distance(uPointLightingLocation,mvPosition.xyz);"+
                    "vPointLightWeighting=10.0/(vPointLightWeighting*vPointLightWeighting);"+
                    "vec3 vertexToLightSource = mvPosition.xyz-uPointLightingLocation;"+
                    "float diff_light_dist = length(vertexToLightSource);"+
                    "       float attenuation = 1.0 / (uAttenuation.x"+
                    "                           + uAttenuation.y * diff_light_dist" +
                    "                           + uAttenuation.z * diff_light_dist * diff_light_dist);"+
                    "float diffuseLightWeighting=0.0;"+
                    "diffuseLightWeighting =attenuation*max(dot(transformedNormal,lightDirection),0.0);"+
                    "          vDiffuseLightWeighting=diffuseLightWeighting;"+
                    "  specularLightWeighting=attenuation*pow(max(dot(reflectionDirection,eyeDirection), 0.0), uMaterialShininess);" +
                    "vSpecularLightWeighting=specularLightWeighting;"+
                    "vColor=aVertexColor;"+
                    "vTextureCoordinate=aTextureCoordinate;"+
                    "}";
    private final String fragmentShaderCode = "precision lowp float;varying vec4 vColor; "+
            "varying vec3 vLightWeighting;"+
            "varying vec4 vDiffuseColor;" +
            "varying float vDiffuseLightWeighting;" +
            "varying float vPointLightWeighting;"+
            "varying vec4 vSpecularColor;" +
            "varying float vSpecularLightWeighting; "+
            "varying vec2 vTextureCoordinate;"+
            "uniform sampler2D uTextureSampler;"+//texture
            "void main() {" +
            "vec4 diffuseColor=vDiffuseLightWeighting*vDiffuseColor;" +
            "vec4 specularColor=vSpecularLightWeighting*vSpecularColor;"+
            "vec4 fragmentColor=texture2D(uTextureSampler,vec2(vTextureCoordinate.s,vTextureCoordinate.t));"+//load the color texture
            "gl_FragColor=vec4(fragmentColor.rgb*vLightWeighting,fragmentColor.a)+specularColor+diffuseColor;"+//the fragment color
            "}";
    private final FloatBuffer vertexBuffer,normalBuffer,colorBuffer;
    private final IntBuffer indexBuffer;
    private final FloatBuffer textureBuffer;
    private final int mProgram;
    private int mPositionHandle,mNormalHandle,mColorHandle;
    //--------
    private int mTextureCoordHandle;
    //--------
    private int diffuseColorHandle;
    private int mMVPMatrixHandle;
    private int pointLightingLocationHandle,pointLightColorHandle,uAmbientColorHandle;
    private int diffuseLightLocationHandle;
    private int specularColorHandle,specularLightLocationHandle;
    private int materialShininessHandle;
    private int attenuateHandle;
    //--------
    private int TextureDataHandle,TextureHandle;
    //--------
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3,COLOR_PER_VERTEX=4;
    //---------
    static final int TEXTURE_PER_VERTEX=2;//no of texture coordinates per vertex
    private final int textureStride=TEXTURE_PER_VERTEX*4;//bytes per texture coordinates
    //---------
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride=COLOR_PER_VERTEX*4;
    static float SphereVertex[];
    static float SphereColor[];
    static int SphereIndex[];
    static float SphereNormal[];
    static float lightlocation[]=new float[3];
    static float diffuselightlocation[]=new float[3];
    static float attenuation[]=new float[3];//light attenuation
    static float diffusecolor[]=new float[4];//diffuse light colour
    static float specularcolor[]=new float[4];//specular highlight colour
    static float MaterialShininess=10f;//material shiness
    static float specularlightlocation[]=new float[3];//specular light location
    //--------
    static float TextureCoordinateData[];
    //--------s

    private  void createShpere(float radius,int nolatitude,int nolongitude) {
        float vertices[]=new float[65535];
        float normal[]=new float[65535];
        int pindex[]=new int[65535];
        float pcolor[]=new float[65535];
        float textureCoordData[]=new float[65535];
        int vertexindex=0;
        int normindex=0;
        int colorindex=0;
        int textureindex=0;
        int indx=0;
        float dist=0f;
        for (int row=0;row<=nolatitude;row++){
            double theta=row*Math.PI/nolatitude;
            double sinTheta=Math.sin(theta);
            double cosTheta=Math.cos(theta);
            for (int col=0;col<=nolongitude;col++)
            {
                double phi=col*2*Math.PI/nolongitude;
                double sinPhi=Math.sin(phi);
                double cosPhi=Math.cos(phi);
                double x=cosPhi*sinTheta;
                double y=cosTheta;
                double z=sinPhi*sinTheta;
                normal[normindex++]=(float)x;
                normal[normindex++]=(float)y;
                normal[normindex++]=(float)z;
                vertices[vertexindex++]=(float)(radius*x);
                vertices[vertexindex++]=(float)(radius*y)+dist;
                vertices[vertexindex++]=(float)(radius*z);
                pcolor[colorindex++] = 1f;
                pcolor[colorindex++] = 0;
                pcolor[colorindex++] = 0f;
                pcolor[colorindex++] = 1f;
                float u=(col/(float)nolongitude);
                float v=(row/(float)nolatitude);
                textureCoordData[textureindex++]=u;
                textureCoordData[textureindex++]=v;
            }
        }
        for (int row=0;row<nolatitude;row++)
        {
            for (int col=0;col<nolongitude;col++)
            {
                int first=(row*(nolongitude+1))+col;
                int second=first+nolongitude+1;
                pindex[indx++]=first;
                pindex[indx++]=second;
                pindex[indx++]=first+1;
                pindex[indx++]=second;
                pindex[indx++]=second+1;
                pindex[indx++]=first+1;
            }
        }

        SphereVertex= Arrays.copyOf(vertices,vertexindex);
        SphereIndex=Arrays.copyOf(pindex,indx);
        SphereNormal=Arrays.copyOf(normal,normindex);
        SphereColor=Arrays.copyOf(pcolor,colorindex);
        TextureCoordinateData=Arrays.copyOf(textureCoordData,textureindex);
    }

    public static int LoadTexture(final Context context, final int resourceId)
    {//load texture image from resoures
        final int[]textureHandle=new int [1];
        GLES32.glGenTextures(1,textureHandle,0);
        if (textureHandle[0]!=0)
        {
            final BitmapFactory.Options options=new BitmapFactory.Options();
            options.inScaled=false;
            final Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(),resourceId,options);
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D,textureHandle[0]);
            //set filtering
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_MIN_FILTER,GLES32.GL_NEAREST);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_MAG_FILTER,GLES32.GL_NEAREST);
            //load bitmap into bound texture
            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D,0,bitmap,0);
            bitmap.recycle();
        }
        else {
            throw new RuntimeException("Error loading texture!");
        }
        return textureHandle[0];
    }

    public Sphere(){

        createShpere(2,30,30);
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(SphereVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SphereVertex);
        vertexBuffer.position(0);
        IntBuffer ib=IntBuffer.allocate(SphereIndex.length);
        indexBuffer=ib;
        indexBuffer.put(SphereIndex);
        indexBuffer.position(0);
        ByteBuffer cb=ByteBuffer.allocateDirect(SphereColor.length*4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer=cb.asFloatBuffer();
        colorBuffer.put(SphereColor);
        colorBuffer.position(0);
        ByteBuffer nb = ByteBuffer.allocateDirect(SphereNormal.length * 4);// (# of coordinate values * 4 bytes per float)
        nb.order(ByteOrder.nativeOrder());
        normalBuffer=nb.asFloatBuffer();
        normalBuffer.put(SphereNormal);
        normalBuffer.position(0);
        ///============
        ByteBuffer tb=ByteBuffer.allocateDirect(TextureCoordinateData.length*4);
        tb.order(ByteOrder.nativeOrder());
        textureBuffer=tb.asFloatBuffer();
        textureBuffer.put(TextureCoordinateData);
        textureBuffer.position(0);
        ///============
        lightlocation[0]=10f;
        lightlocation[1]=10f;
        lightlocation[2]=10f;
        diffuselightlocation[0]=2f;
        diffuselightlocation[1]=0.2f;
        diffuselightlocation[2]=2;
        specularcolor[0]=1;
        specularcolor[1]=1;
        specularcolor[2]=1;
        specularcolor[3]=1;
        specularlightlocation[0]=-7;
        specularlightlocation[1]=-4;
        specularlightlocation[2]=2;
        ///============
        TextureDataHandle = LoadTexture(MainActivity.getContext(), R.drawable.world);
        //////////////////////
        // prepare shaders and OpenGL program
        int vertexShader = MyRenderer.loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES32.glCreateProgram();             // create empty OpenGL Program
        GLES32.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES32.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES32.glLinkProgram(mProgram);                  // link the  OpenGL program to create an executable
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition");
        // Enable a handle to the triangle vertices
        GLES32.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        MyRenderer.checkGlError("glVertexAttribPointer");
        mColorHandle=GLES32.glGetAttribLocation(mProgram,"aVertexColor");
        GLES32.glEnableVertexAttribArray(mColorHandle);
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, colorBuffer);

        mNormalHandle=GLES32.glGetAttribLocation(mProgram,"aVertexNormal");
        GLES32.glEnableVertexAttribArray(mNormalHandle);
        GLES32.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, normalBuffer);
        MyRenderer.checkGlError("glVertexAttribPointer");
        // get handle to shape's transformation matrix
        pointLightingLocationHandle=GLES32.glGetUniformLocation(mProgram, "uPointLightingLocation");
        diffuseLightLocationHandle=GLES32.glGetUniformLocation(mProgram,"uDiffuseLightLocation");
        diffuseColorHandle=GLES32.glGetUniformLocation(mProgram,"uDiffuseColor");
        diffusecolor[0]=1;diffusecolor[1]=1;diffusecolor[2]=1;diffusecolor[3]=1;
        attenuateHandle=GLES32.glGetUniformLocation(mProgram,"uAttenuation");
        attenuation[0]=1;attenuation[1]=0.14f;attenuation[2]=0.07f;
        pointLightColorHandle=GLES32.glGetUniformLocation(mProgram, "uPointLightingColor");
        uAmbientColorHandle=GLES32.glGetUniformLocation(mProgram,"uAmbientColor");
        MyRenderer.checkGlError("uAmbientColor");
        specularColorHandle=GLES32.glGetUniformLocation(mProgram,"uSpecularColor");
        specularLightLocationHandle=GLES32.glGetUniformLocation(mProgram,"uSpecularLightLocation");
        materialShininessHandle=GLES32.glGetUniformLocation(mProgram,"uMaterialShininess");
        mTextureCoordHandle=GLES32.glGetAttribLocation(mProgram,"aTextureCoordinate");//texture coordinates
        GLES32.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES32.glVertexAttribPointer(mTextureCoordHandle,TEXTURE_PER_VERTEX,GLES32.GL_FLOAT,false,textureStride,textureBuffer);
        TextureHandle=GLES32.glGetUniformLocation(mProgram,"uTextureSampler");//texture
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyRenderer.checkGlError("glGetUniformLocation-mMVPMatrixHandle");
    }

    public void draw(float[] mvpMatrix) {
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyRenderer.checkGlError("glUniformMatrix4fv");
        GLES32.glUniform3fv(pointLightingLocationHandle,1,lightlocation,0);
        GLES32.glUniform3fv(diffuseLightLocationHandle,1,diffuselightlocation,0);
        GLES32.glUniform4fv(diffuseColorHandle,1,diffusecolor,0);
        GLES32.glUniform3fv(attenuateHandle,1,attenuation,0);
        GLES32.glUniform3f(pointLightColorHandle,0.3f,0.3f,0.3f);
        GLES32.glUniform3f(uAmbientColorHandle,0.6f,0.6f,0.6f);
        GLES32.glUniform4fv(specularColorHandle,1,specularcolor,0);
        GLES32.glUniform1f(materialShininessHandle,MaterialShininess);
        GLES32.glUniform3fv(specularLightLocationHandle,1,specularlightlocation,0);
        //===================
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);//set the active texture to unit 0
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D,TextureDataHandle);//bind the texture to this unit
        GLES32.glUniform1i(TextureHandle,0);//tell the uniform sampler to use this texture i
        GLES32.glVertexAttribPointer(mTextureCoordHandle,TEXTURE_PER_VERTEX,GLES32.GL_FLOAT,false,textureStride,textureBuffer);
        //===================
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        GLES32.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, normalBuffer);
        // Draw the sphere

        GLES32.glDrawElements(GLES32.GL_TRIANGLES,SphereIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);

    }

    public void setLightLocation(float px,float py,float pz)
    {
        lightlocation[0]=px;
        lightlocation[1]=py;
        lightlocation[2]=pz;
    }
}
