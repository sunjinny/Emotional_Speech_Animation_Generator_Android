package vml.com.vm.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

/**
 * Helper class to draw a set of 3D points. 
 * 
 * @author Bang Seungbae
 *
 */
public class Marker
{
	/** vertex shader code */
    private final String vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition;" +
        "  gl_PointSize = 20.0; "+
        "}";

    /** fragment shader code */
    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}";

    /** openGL vertex buffer*/
    private final FloatBuffer vertexBuffer;
    
    /**shader - compiled program handle*/
    private final int mProgram;
    
    /**shader - vertex position handle*/
    private int mPositionHandle;
    
    /**shader - point color handle*/
    private int mColorHandle;
    
    /**shader - model view projection matrix handle handle*/
    private int mMVPMatrixHandle;
    
    /**shader - point size handle*/
    private int mThicknessHandle;
    
    /**shader - number of points in the set*/
    private int vtxCount;
   // private float[] mCoordinate;

    /** number of coordinates per vertex in this array*/
    static final int COORDS_PER_VERTEX = 3;

    /** vertex stride*/
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    /**default point color*/
    float mColor[] = { 1.0f, 1.0f, 0.0f, 1.0f };

    /**
     * Marker class constructor
     * @param coord Stacked coordinates of the point set stored in a x,y,z manner
     */
    public Marker(float[] coord)
    {
    	//this.mCoordinate = coord;
    	this.vtxCount = coord.length/3;
    	
        ByteBuffer bb = ByteBuffer.allocateDirect(coord.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
    	vertexBuffer.put(coord);
    	vertexBuffer.position(0);

        // prepare shaders and OpenGL program
        //int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        //int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        
        int vertexShader = VMShaderUtil.compileShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShader = VMShaderUtil.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }
   
    /**
     * Render the point set with the given model/View/Projection matrix
     * @param mvpMatrix model view projection matrix
     */
    public void draw(float[] mvpMatrix)
    {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        VMShaderUtil.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        VMShaderUtil.checkGlError("glUniformMatrix4fv");
        
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vtxCount);
        
        // Disable vertex array
        //GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
    
    /**
     * Add an additional poitn into  the set
     * @param coord 3D coordinates of the new point
     */
    public void translate(float[] coord)
    {
    	//this.mCoordinate = coord;
    	vertexBuffer.put(coord);
    	vertexBuffer.position(0);
    }
    
    /**
     * Modify the render color of the point set
     * @param color 3D coordinates in r,g,b space
     */
    public void setColor(float[] color){
    	this.mColor = color;
    }
    
    /**
     * Modify the render color of the point set
     * @param r red value in 0,1 range
     * @param g green value in 0,1 range
     * @param b blue value in 0,1 range
     */
    public void setColor(float r, float g, float b)
    {
    	this.mColor[0] = r;
    	this.mColor[1] = g;
    	this.mColor[2] = b;
    }
}
