package com.bennyplo.virtualreality;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class CharacterA {
    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;"+//vertex of an object
                    " attribute vec4 aVertexColor;"+//the colour  of the object
                    "     uniform mat4 uMVPMatrix;"+//model view  projection matrix
                    "    varying vec4 vColor;"+//variable to be accessed by the fragment shader
                    "    void main() {" +
                    "        gl_Position = uMVPMatrix* vec4(aVertexPosition, 1.0);"+//calculate the position of the vertex
                    "        vColor=aVertexColor;}";//get the colour from the application program

    private final String fragmentShaderCode =
            "precision lowp float;"+ //need to set to low in order to show the depth map
                    "varying vec4 vColor;"+ //variable from the vertex shader
                    "void main() {"+
                    "float depth=1.0-gl_FragCoord.z;"+//to show closer surface to be brighter, and further away surface darker
                    "gl_FragColor = vColor;"+
                    "}";
    private final FloatBuffer vertexBuffer,colorBuffer;
    private final IntBuffer indexBuffer;
    private final int mProgram;
    private int mPositionHandle,mColorHandle;
    private int mMVPMatrixHandle;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int COLOR_PER_VERTEX = 4;
    private int vertexCount;// number of vertices
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride=COLOR_PER_VERTEX*4;//4 bytes per vertex
    static float CharAVertex[] ={
            -0.2f,1f,-0.3f,//0
            -0.2f,1f,0.3f,//1
            0.2f,1f,-0.3f,//2
            0.2f,1f,0.3f,//3
            -1f,-1f,-0.5f,//4
            -1f,-1f,0.5f,//5
            -0.6f,-1f,-0.5f,//6
            -0.6f,-1f,0.5f,//7
            0.6f,-1f,0.5f,//8
            0.6f,-1f,-0.5f,//9
            1,-1,0.5f,//10
            1,-1,-0.5f,//11
            0,0.8f,0.3f,//12
            0,0.8f,-0.3f,//13
            0.25f,0.1f,0.382f,//14
            0.25f,0.1f,-0.382f,//15
            -0.25f,0.1f,0.382f,//16
            -0.25f,0.1f,-0.382f,//17
            0.32f,-0.1f,0.41f,//18
            0.32f,-0.1f,-0.41f,//19
            -0.32f,-0.1f,0.41f,//20
            -0.32f,-0.1f,-0.41f,//21
    };
    static int CharIndex[]={
            //0,1,2,2,3,1,//top
            1,0,2,1,3,2,//top
            //0,4,5,5,1,0,//left
            4,0,5,5,1,0,//left
            4,5,6,6,7,5,//left bottom
            1,5,7,7,3,1,//left front
            //0,4,6,6,2,0,//left back
            4,0,6,2,6,0,//left back
            //3,10,11,11,3,2,//right
            3,10,11,11,3,2,//right
            8,9,10,10,11,9,//right bottom
            //3,10,8,8,3,1,//right front
            10,3,8,8,3,1,//right front
            //2,11,9,9,2,0,//right back
            2,11,9,2,9,0,//right back
            //12,13,6,6,7,12,//left inner
            6,12,13,7,6,12,//left inner
            //12,8,9,9,13,12,//right inner
            9,8,12,9,13,12,//right inner
            //14,15,16,16,17,15,//inner top
            14,15,16,15,17,16,//inner top
            19,18,20,20,21,19,//inner bottom
            //14,18,20,20,16,14,//inner front
            18,14,20,16,20,14,//inner front
            15,19,21,21,17,15,//inner back
    };
    static float CharAColor[]={
            0.0f,0.0f,1.0f,1.0f,//0
            0.0f,0.0f,1.0f,1.0f,//1
            0.0f,0.0f,1.0f,1.0f,//2
            0.0f,0.0f,1.0f,1.0f,//3
            0.0f,1.0f,0.0f,1.0f,//4
            0.0f,1.0f,0.0f,1.0f,//5
            0.0f,1.0f,0.0f,1.0f,//6
            0.0f,1.0f,0.0f,1.0f,//7
            0.0f,1.0f,0.0f,1.0f,//8
            0.0f,1.0f,0.0f,1.0f,//9
            0.0f,1.0f,0.0f,1.0f,//10
            0.0f,1.0f,0.0f,1.0f,//11
            0.0f,0.0f,1.0f,1.0f,//12
            0.0f,0.0f,1.0f,1.0f,//13
            0.0f,0.0f,1.0f,1.0f,//14
            0.0f,0.0f,1.0f,1.0f,//15
            0.0f,0.0f,1.0f,1.0f,//16
            0.0f,0.0f,1.0f,1.0f,//17
            0.0f,1.0f,0.0f,1.0f,//18
            0.0f,1.0f,0.0f,1.0f,//19
            0.0f,1.0f,0.0f,1.0f,//20
            0.0f,1.0f,0.0f,1.0f,//21
    };


    public CharacterA(){
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(CharAVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(CharAVertex);
        vertexBuffer.position(0);
        vertexCount=CharAVertex.length/COORDS_PER_VERTEX;
        ByteBuffer cb=ByteBuffer.allocateDirect(CharAColor.length * 4);// (# of coordinate values * 4 bytes per float)
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(CharAColor);
        colorBuffer.position(0);
        IntBuffer ib=IntBuffer.allocate(CharIndex.length);
        indexBuffer=ib;
        indexBuffer.put(CharIndex);
        indexBuffer.position(0);
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
        //get the handle to vertex shader's aVertexColor member
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor");
        // Enable a handle to the  colour
        GLES32.glEnableVertexAttribArray(mColorHandle);
        // Prepare the colour coordinate data
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    public void draw(float[] mvpMatrix) {
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        // Draw the pentagon prism
        //GLES32.glDrawArrays(GLES32.GL_TRIANGLES,0, vertexCount);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,CharIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);
    }
}
