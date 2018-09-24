/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.texture;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.FastMath;
import com.jme3.renderer.Caps;
import com.jme3.renderer.Renderer;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.image.LastTextureState;
import com.jme3.util.BufferUtils;
import com.jme3.util.NativeObject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <code>Image</code> defines a data format for a graphical image. The image is
 * defined by a format, a height and width, and the image data. The width and
 * height must be greater than 0. The data is contained in a byte buffer, and
 * should be packed before creation of the image object.
 *
 * @author Mark Powell
 * @author Joshua Slack
 * @version $Id: Image.java 4131 2009-03-19 20:15:28Z blaine.dev $
 */
public class Image extends NativeObject /* , Cloneable */ implements IImage {

	// image attributes
	protected Format format;
	protected int width, height, depth;
	protected int[] mipMapSizes;
	protected ArrayList<ByteBuffer> data;
	protected int multiSamples = 1;
	protected ColorSpace colorSpace = null;
	// protected int mipOffset = 0;

	// attributes relating to GL object
	protected boolean mipsWereGenerated = false;
	protected boolean needGeneratedMips = false;
	protected LastTextureState lastTextureState = new LastTextureState();

	/**
	 * Internal use only. The renderer stores the texture state set from the last
	 * texture so it doesn't have to change it unless necessary.
	 * 
	 * @return The image parameter state.
	 */
	private LastTextureState getLastTextureState() {
		return lastTextureState;
	}

	/**
	 * Internal use only. The renderer marks which images have generated mipmaps in
	 * VRAM and which do not, so it can generate them as needed.
	 * 
	 * @param generated
	 *            If mipmaps were generated or not.
	 */
	private void setMipmapsGenerated(boolean generated) {
		this.mipsWereGenerated = generated;
	}

	/**
	 * Internal use only. Check if the renderer has generated mipmaps for this image
	 * in VRAM or not.
	 * 
	 * @return If mipmaps were generated already.
	 */
	private boolean isMipmapsGenerated() {
		return mipsWereGenerated;
	}

	/**
	 * (Package private) Called by {@link Texture} when
	 * {@link #isMipmapsGenerated() } is false in order to generate mipmaps for this
	 * image.
	 */
	void setNeedGeneratedMipmaps() {
		needGeneratedMips = true;
	}

	/**
	 * Constructor instantiates a new <code>Image</code> object. All values are
	 * undefined.
	 */
	public Image() {
		super();
		data = new ArrayList<ByteBuffer>(1);
	}

	protected Image(int id) {
		super(id);
	}

	/**
	 * Constructor instantiates a new <code>Image</code> object. The attributes of
	 * the image are defined during construction.
	 *
	 * @param format
	 *            the data format of the image.
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of the image.
	 * @param data
	 *            the image data.
	 * @param mipMapSizes
	 *            the array of mipmap sizes, or null for no mipmaps.
	 * @param colorSpace
	 * @see ColorSpace the colorSpace of the image
	 */
	public Image(Format format, int width, int height, int depth, ArrayList<ByteBuffer> data, int[] mipMapSizes,
			ColorSpace colorSpace) {

		this();

		if (mipMapSizes != null) {
			if (mipMapSizes.length <= 1) {
				mipMapSizes = null;
			} else {
				needGeneratedMips = false;
				mipsWereGenerated = true;
			}
		}

		setFormat(format);
		this.width = width;
		this.height = height;
		this.data = data;
		this.depth = depth;
		this.mipMapSizes = mipMapSizes;
		this.colorSpace = colorSpace;
	}

	/**
	 * @see {@link #Image(com.jme3.texture.Image.Format, int, int, int, java.util.ArrayList, int[], boolean)}
	 * @param format
	 * @param width
	 * @param height
	 * @param depth
	 * @param data
	 * @param mipMapSizes
	 * @deprecated use
	 *             {@link #Image(com.jme3.texture.Image.Format, int, int, int, java.util.ArrayList, int[], boolean)}
	 */
	@Deprecated
	public Image(Format format, int width, int height, int depth, ArrayList<ByteBuffer> data, int[] mipMapSizes) {
		this(format, width, height, depth, data, mipMapSizes, ColorSpace.Linear);
	}

	/**
	 * Constructor instantiates a new <code>Image</code> object. The attributes of
	 * the image are defined during construction.
	 *
	 * @param format
	 *            the data format of the image.
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of the image.
	 * @param data
	 *            the image data.
	 * @param mipMapSizes
	 *            the array of mipmap sizes, or null for no mipmaps.
	 * @param colorSpace
	 * @see ColorSpace the colorSpace of the image
	 */
	public Image(Format format, int width, int height, ByteBuffer data, int[] mipMapSizes, ColorSpace colorSpace) {

		this();

		if (mipMapSizes != null && mipMapSizes.length <= 1) {
			mipMapSizes = null;
		} else {
			needGeneratedMips = false;
			mipsWereGenerated = true;
		}

		setFormat(format);
		this.width = width;
		this.height = height;
		if (data != null) {
			this.data = new ArrayList<ByteBuffer>(1);
			this.data.add(data);
		}
		this.mipMapSizes = mipMapSizes;
		this.colorSpace = colorSpace;
	}

	/**
	 * @see {@link #Image(com.jme3.texture.Image.Format, int, int, java.nio.ByteBuffer, int[], boolean)}
	 * @param format
	 * @param width
	 * @param height
	 * @param data
	 * @param mipMapSizes
	 * @deprecated use
	 *             {@link #Image(com.jme3.texture.Image.Format, int, int, java.nio.ByteBuffer, int[], boolean)}
	 */
	@Deprecated
	public Image(Format format, int width, int height, ByteBuffer data, int[] mipMapSizes) {
		this(format, width, height, data, mipMapSizes, ColorSpace.Linear);
	}

	/**
	 * Constructor instantiates a new <code>Image</code> object. The attributes of
	 * the image are defined during construction.
	 *
	 * @param format
	 *            the data format of the image.
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of the image.
	 * @param data
	 *            the image data.
	 * @param colorSpace
	 * @see ColorSpace the colorSpace of the image
	 */
	public Image(Format format, int width, int height, int depth, ArrayList<ByteBuffer> data, ColorSpace colorSpace) {
		this(format, width, height, depth, data, null, colorSpace);
	}

	/**
	 * @see {@link #Image(com.jme3.texture.Image.Format, int, int, int, java.util.ArrayList, boolean)}
	 * @param format
	 * @param width
	 * @param height
	 * @param depth
	 * @param data
	 * @deprecated use
	 *             {@link #Image(com.jme3.texture.Image.Format, int, int, int, java.util.ArrayList, boolean)}
	 */
	@Deprecated
	public Image(Format format, int width, int height, int depth, ArrayList<ByteBuffer> data) {
		this(format, width, height, depth, data, ColorSpace.Linear);
	}

	/**
	 * Constructor instantiates a new <code>Image</code> object. The attributes of
	 * the image are defined during construction.
	 *
	 * @param format
	 *            the data format of the image.
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of the image.
	 * @param data
	 *            the image data.
	 * @param colorSpace
	 * @see ColorSpace the colorSpace of the image
	 */
	public Image(Format format, int width, int height, ByteBuffer data, ColorSpace colorSpace) {
		this(format, width, height, data, null, colorSpace);
	}

	/**
	 * @see {@link #Image(com.jme3.texture.Image.Format, int, int, java.nio.ByteBuffer, boolean)}
	 * @param format
	 * @param width
	 * @param height
	 * @param data
	 * @deprecated use
	 *             {@link #Image(com.jme3.texture.Image.Format, int, int, java.nio.ByteBuffer, boolean)}
	 */
	@Deprecated
	public Image(Format format, int width, int height, ByteBuffer data) {
		this(format, width, height, data, null, ColorSpace.Linear);
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#isGeneratedMipmapsRequired()
	 */
	@Override
	public boolean isGeneratedMipmapsRequired() {
		return needGeneratedMips;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#isNPOT()
	 */
	@Override
	public boolean isNPOT() {
		return width != 0 && height != 0 && (!FastMath.isPowerOfTwo(width) || !FastMath.isPowerOfTwo(height));
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#getMultiSamples()
	 */
	@Override
	public int getMultiSamples() {
		return multiSamples;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setMultiSamples(int)
	 */
	@Override
	public void setMultiSamples(int multiSamples) {
		if (multiSamples <= 0)
			throw new IllegalArgumentException("multiSamples must be > 0");

		if (getData(0) != null)
			throw new IllegalArgumentException("Cannot upload data as multisample texture");

		if (hasMipmaps())
			throw new IllegalArgumentException("Multisample textures do not support mipmaps");

		this.multiSamples = multiSamples;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setData(java.util.ArrayList)
	 */
	@Override
	public void setData(ArrayList<ByteBuffer> data) {
		this.data = data;
		setUpdateNeeded();
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setData(java.nio.ByteBuffer)
	 */
	@Override
	public void setData(ByteBuffer data) {
		this.data = new ArrayList<ByteBuffer>(1);
		this.data.add(data);
		setUpdateNeeded();
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setData(int, java.nio.ByteBuffer)
	 */
	@Override
	public void setData(int index, ByteBuffer data) {
		if (index >= 0) {
			while (this.data.size() <= index) {
				this.data.add(null);
			}
			this.data.set(index, data);
			setUpdateNeeded();
		} else {
			throw new IllegalArgumentException("index must be greater than or equal to 0.");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#addData(java.nio.ByteBuffer)
	 */
	@Override
	public void addData(ByteBuffer data) {
		if (this.data == null)
			this.data = new ArrayList<ByteBuffer>(1);
		this.data.add(data);
		setUpdateNeeded();
	}
 
	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setMipMapSizes(int[])
	 */
	@Override
	public void setMipMapSizes(int[] mipMapSizes) {
		if (mipMapSizes != null && mipMapSizes.length <= 1)
			mipMapSizes = null;

		this.mipMapSizes = mipMapSizes;

		if (mipMapSizes != null) {
			needGeneratedMips = false;
			mipsWereGenerated = false;
		} else {
			needGeneratedMips = true;
			mipsWereGenerated = false;
		}

		setUpdateNeeded();
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setHeight(int)
	 */
	@Override
	public void setHeight(int height) {
		this.height = height;
		setUpdateNeeded();
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setDepth(int)
	 */
	@Override
	public void setDepth(int depth) {
		this.depth = depth;
		setUpdateNeeded();
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setWidth(int)
	 */
	@Override
	public void setWidth(int width) {
		this.width = width;
		setUpdateNeeded();
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setFormat(com.jme3.texture.Format)
	 */
	@Override
	public void setFormat(Format format) {
		if (format == null) {
			throw new NullPointerException("format may not be null.");
		}

		this.format = format;
		setUpdateNeeded();
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#getFormat()
	 */
	@Override
	public Format getFormat() {
		return format;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#getWidth()
	 */
	@Override
	public int getWidth() {
		return width;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#getHeight()
	 */
	@Override
	public int getHeight() {
		return height;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#getDepth()
	 */
	@Override
	public int getDepth() {
		return depth;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#getData()
	 */
	@Override
	public List<ByteBuffer> getData() {
		return data;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#getData(int)
	 */
	@Override
	public ByteBuffer getData(int index) {
		if (data.size() > index)
			return data.get(index);
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#hasMipmaps()
	 */
	@Override
	public boolean hasMipmaps() {
		return mipMapSizes != null;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#getMipMapSizes()
	 */
	@Override
	public int[] getMipMapSizes() {
		return mipMapSizes;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setColorSpace(com.jme3.texture.image.ColorSpace)
	 */
	@Override
	public void setColorSpace(ColorSpace colorSpace) {
		this.colorSpace = colorSpace;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#getColorSpace()
	 */
	@Override
	public ColorSpace getColorSpace() {
		return colorSpace;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#clone()
	 */
	@Override
	public Image clone() {
		Image clone = (Image) super.clone();
		clone.mipMapSizes = mipMapSizes != null ? mipMapSizes.clone() : null;
		clone.data = data != null ? new ArrayList<ByteBuffer>(data) : null;
		clone.lastTextureState = new LastTextureState();
		clone.setUpdateNeeded();
		return clone;
	}
	
	/*
	 * Sets the update needed flag, while also checking if mipmaps need to be
	 * regenerated.
	 */
	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setUpdateNeeded()
	 */
	@Override
	public void setUpdateNeeded() {
		super.setUpdateNeeded();
		if (isGeneratedMipmapsRequired() && !hasMipmaps()) {
			// Mipmaps are no longer valid, since the image was changed.
			setMipmapsGenerated(false);
		}
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#resetObject()
	 */
	@Override
	public void resetObject() {
		this.id = -1;
		this.mipsWereGenerated = false;
		this.lastTextureState.reset();
		setUpdateNeeded();
	}

	@Override
	protected void deleteNativeBuffers() {
		for (ByteBuffer buf : data) {
			BufferUtils.destroyDirectBuffer(buf);
		}
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#deleteObject(java.lang.Object)
	 */
	@Override
	public void deleteObject(Object rendererObject) {
		((Renderer) rendererObject).deleteImage(this);
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#createDestructableClone()
	 */
	@Override
	public NativeObject createDestructableClone() {
		return new Image(id);
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#getUniqueId()
	 */
	@Override
	public long getUniqueId() {
		return ((long) OBJTYPE_TEXTURE << 32) | ((long) id);
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append("[size=").append(width).append("x").append(height);

		if (depth > 1)
			sb.append("x").append(depth);

		sb.append(", format=").append(format.name());

		if (hasMipmaps())
			sb.append(", mips");

		if (getId() >= 0)
			sb.append(", id=").append(id);

		sb.append("]");

		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof Image)) {
			return false;
		}
		IImage that = (IImage) other;
		if (this.getFormat() != that.getFormat())
			return false;
		if (this.getWidth() != that.getWidth())
			return false;
		if (this.getHeight() != that.getHeight())
			return false;
		if (this.getData() != null && !this.getData().equals(that.getData()))
			return false;
		if (this.getData() == null && that.getData() != null)
			return false;
		if (this.getMipMapSizes() != null && !Arrays.equals(this.getMipMapSizes(), that.getMipMapSizes()))
			return false;
		if (this.getMipMapSizes() == null && that.getMipMapSizes() != null)
			return false;
		if (this.getMultiSamples() != that.getMultiSamples())
			return false;

		return true;
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + (this.format != null ? this.format.hashCode() : 0);
		hash = 97 * hash + this.width;
		hash = 97 * hash + this.height;
		hash = 97 * hash + this.depth;
		hash = 97 * hash + Arrays.hashCode(this.mipMapSizes);
		hash = 97 * hash + (this.data != null ? this.data.hashCode() : 0);
		hash = 97 * hash + this.multiSamples;
		return hash;
	}

	/**
	 * Deprecated units
	 */
	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#setEfficentData(java.lang.Object)
	 */
	@Override
	@Deprecated
	public void setEfficentData(Object efficientData) {
	}

	/* (non-Javadoc)
	 * @see com.jme3.texture.IImage#getEfficentData()
	 */
	@Override
	@Deprecated
	public Object getEfficentData() {
		return null;
	}
	
}
