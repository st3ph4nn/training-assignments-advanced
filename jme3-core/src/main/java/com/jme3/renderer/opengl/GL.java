package com.jme3.renderer.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public interface GL {

	void resetStats();

	void glActiveTexture(int texture);

	void glAttachShader(int program, int shader);

	void glBindBuffer(int target, int buffer);

	void glBindTexture(int target, int texture);

	void glBlendEquationSeparate(int colorMode, int alphaMode);

	void glBlendFunc(int sfactor, int dfactor);

	void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha);

	void glBufferData(int target, long data_size, int usage);

	void glBufferData(int target, FloatBuffer data, int usage);

	void glBufferData(int target, ShortBuffer data, int usage);

	void glBufferData(int target, ByteBuffer data, int usage);

	void glBufferSubData(int target, long offset, FloatBuffer data);

	void glBufferSubData(int target, long offset, ShortBuffer data);

	void glBufferSubData(int target, long offset, ByteBuffer data);

	void glClear(int mask);

	void glClearColor(float red, float green, float blue, float alpha);

	void glColorMask(boolean red, boolean green, boolean blue, boolean alpha);

	void glCompileShader(int shader);

	void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border,
			ByteBuffer data);

	void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
			ByteBuffer data);

	int glCreateProgram();

	int glCreateShader(int shaderType);

	void glCullFace(int mode);

	void glDeleteBuffers(IntBuffer buffers);

	void glDeleteProgram(int program);

	void glDeleteShader(int shader);

	void glDeleteTextures(IntBuffer textures);

	void glDepthFunc(int func);

	void glDepthMask(boolean flag);

	void glDepthRange(double nearVal, double farVal);

	void glDetachShader(int program, int shader);

	void glDisable(int cap);

	void glDisableVertexAttribArray(int index);

	void glDrawArrays(int mode, int first, int count);

	void glDrawRangeElements(int mode, int start, int end, int count, int type, long indices); /// GL2+

	void glEnable(int cap);

	void glEnableVertexAttribArray(int index);

	void glGenBuffers(IntBuffer buffers);

	void glGenTextures(IntBuffer textures);

	int glGetAttribLocation(int program, String name);

	void glGetBoolean(int pname, ByteBuffer params);

	void glGetBufferSubData(int target, long offset, ByteBuffer data);

	int glGetError();

	void glGetInteger(int pname, IntBuffer params);

	void glGetProgram(int program, int pname, IntBuffer params);

	String glGetProgramInfoLog(int program, int maxSize);

	void glGetShader(int shader, int pname, IntBuffer params);

	String glGetShaderInfoLog(int shader, int maxSize);

	String glGetString(int name);

	int glGetUniformLocation(int program, String name);

	boolean glIsEnabled(int cap);

	void glLineWidth(float width);

	void glLinkProgram(int program);

	void glPixelStorei(int pname, int param);

	void glPolygonOffset(float factor, float units);

	void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer data);

	void glReadPixels(int x, int y, int width, int height, int format, int type, long offset);

	void glScissor(int x, int y, int width, int height);

	void glShaderSource(int shader, String[] string, IntBuffer length);

	void glStencilFuncSeparate(int face, int func, int ref, int mask);

	void glStencilOpSeparate(int face, int sfail, int dpfail, int dppass);

	void glTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int format,
			int type, ByteBuffer data);

	void glTexParameterf(int target, int pname, float param);

	void glTexParameteri(int target, int pname, int param);

	void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
			ByteBuffer data);

	void glUniform1(int location, FloatBuffer value);

	void glUniform1(int location, IntBuffer value);

	void glUniform1f(int location, float v0);

	void glUniform1i(int location, int v0);

	void glUniform2(int location, IntBuffer value);

	void glUniform2(int location, FloatBuffer value);

	void glUniform2f(int location, float v0, float v1);

	void glUniform3(int location, IntBuffer value);

	void glUniform3(int location, FloatBuffer value);

	void glUniform3f(int location, float v0, float v1, float v2);

	void glUniform4(int location, FloatBuffer value);

	void glUniform4(int location, IntBuffer value);

	void glUniform4f(int location, float v0, float v1, float v2, float v3);

	void glUniformMatrix3(int location, boolean transpose, FloatBuffer value);

	void glUniformMatrix4(int location, boolean transpose, FloatBuffer value);

	void glUseProgram(int program);

	void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer);

	void glViewport(int x, int y, int width, int height);

}