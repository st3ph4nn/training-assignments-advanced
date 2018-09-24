package com.jme3.shader;

public interface IUniform {

	int hashCode();

	boolean equals(Object obj);

	String toString();

	void setBinding(UniformBinding binding);

	void clearSetByCurrentMaterial();

	void clearValue();

	void setValue(VarType type, Object value);

	void setVector4Length(int length);

	void setVector4InArray(float x, float y, float z, float w, int index);

	boolean isUpdateNeeded();

	void clearUpdateNeeded();

	void reset();

	void deleteNativeBuffers();

}