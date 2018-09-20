package com.jme3.texture;

import java.nio.ByteBuffer;

import com.jme3.util.NativeObject;

public interface IImage {

	void addData(ByteBuffer data);

	/**
	 * @return A shallow clone of this image. The data is not cloned.
	 */
	IImage clone();

	/*
	 * Sets the update needed flag, while also checking if mipmaps need to be
	 * regenerated.
	 */
	void setUpdateNeeded();

	void resetObject();

	void deleteObject(Object rendererObject);

	NativeObject createDestructableClone();

	long getUniqueId();

	String toString();

	boolean equals(Object other);

	int hashCode();

}