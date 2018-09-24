package com.jme3.texture;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jme3.renderer.Renderer;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.NativeObject;

public interface IImage {

	/**
	 * @return True if the image needs to have mipmaps generated for it (as
	 *         requested by the texture). This stays true even after mipmaps have
	 *         been generated.
	 */
	boolean isGeneratedMipmapsRequired();

	/**
	 * Determine if the image is NPOT.
	 *
	 * @return if the image is a non-power-of-2 image, e.g. having dimensions that
	 *         are not powers of 2.
	 */
	boolean isNPOT();

	/**
	 * @return The number of samples (for multisampled textures).
	 * @see Image#setMultiSamples(int)
	 */
	int getMultiSamples();

	/**
	 * @param multiSamples
	 *            Set the number of samples to use for this image, setting this to a
	 *            value higher than 1 turns this image/texture into a multisample
	 *            texture (on OpenGL3.1 and higher).
	 */
	void setMultiSamples(int multiSamples);

	/**
	 * <code>setData</code> sets the data that makes up the image. This data is
	 * packed into an array of <code>ByteBuffer</code> objects.
	 *
	 * @param data
	 *            the data that contains the image information.
	 */
	void setData(ArrayList<ByteBuffer> data);

	/**
	 * <code>setData</code> sets the data that makes up the image. This data is
	 * packed into a single <code>ByteBuffer</code>.
	 *
	 * @param data
	 *            the data that contains the image information.
	 */
	void setData(ByteBuffer data);

	void setData(int index, ByteBuffer data);

	void addData(ByteBuffer data);

	/**
	 * Sets the mipmap sizes stored in this image's data buffer. Mipmaps are stored
	 * sequentially, and the first mipmap is the main image data. To specify no
	 * mipmaps, pass null and this will automatically be expanded into a single
	 * mipmap of the full
	 *
	 * @param mipMapSizes
	 *            the mipmap sizes array, or null for a single image map.
	 */
	void setMipMapSizes(int[] mipMapSizes);

	/**
	 * <code>setHeight</code> sets the height value of the image. It is typically a
	 * good idea to try to keep this as a multiple of 2.
	 *
	 * @param height
	 *            the height of the image.
	 */
	void setHeight(int height);

	/**
	 * <code>setDepth</code> sets the depth value of the image. It is typically a
	 * good idea to try to keep this as a multiple of 2. This is used for 3d images.
	 *
	 * @param depth
	 *            the depth of the image.
	 */
	void setDepth(int depth);

	/**
	 * <code>setWidth</code> sets the width value of the image. It is typically a
	 * good idea to try to keep this as a multiple of 2.
	 *
	 * @param width
	 *            the width of the image.
	 */
	void setWidth(int width);

	/**
	 * <code>setFormat</code> sets the image format for this image.
	 *
	 * @param format
	 *            the image format.
	 * @throws NullPointerException
	 *             if format is null
	 * @see Format
	 */
	void setFormat(Format format);

	/**
	 * <code>getFormat</code> returns the image format for this image.
	 *
	 * @return the image format.
	 * @see Format
	 */
	Format getFormat();

	/**
	 * <code>getWidth</code> returns the width of this image.
	 *
	 * @return the width of this image.
	 */
	int getWidth();

	/**
	 * <code>getHeight</code> returns the height of this image.
	 *
	 * @return the height of this image.
	 */
	int getHeight();

	/**
	 * <code>getDepth</code> returns the depth of this image (for 3d images).
	 *
	 * @return the depth of this image.
	 */
	int getDepth();

	/**
	 * <code>getData</code> returns the data for this image. If the data is
	 * undefined, null will be returned.
	 *
	 * @return the data for this image.
	 */
	List<ByteBuffer> getData();

	/**
	 * <code>getData</code> returns the data for this image. If the data is
	 * undefined, null will be returned.
	 *
	 * @return the data for this image.
	 */
	ByteBuffer getData(int index);

	/**
	 * Returns whether the image data contains mipmaps.
	 *
	 * @return true if the image data contains mipmaps, false if not.
	 */
	boolean hasMipmaps();

	/**
	 * Returns the mipmap sizes for this image.
	 *
	 * @return the mipmap sizes for this image.
	 */
	int[] getMipMapSizes();

	/**
	 * image loader is responsible for setting this attribute based on the color
	 * space in which the image has been encoded with. In the majority of cases,
	 * this flag will be set to sRGB by default since many image formats do not
	 * contain any color space information and the most frequently used colors space
	 * is sRGB
	 *
	 * The material loader may override this attribute to Lineat if it determines
	 * that such conversion must not be performed, for example, when loading normal
	 * maps.
	 *
	 * @param colorSpace
	 * 			@see ColorSpace. Set to sRGB to enable srgb -&gt; linear
	 *            conversion, Linear otherwise.
	 *
	 * @seealso Renderer#setLinearizeSrgbImages(boolean)
	 *
	 */
	void setColorSpace(ColorSpace colorSpace);

	/**
	 * Specifies that this image is an SRGB image and therefore must undergo an sRGB
	 * -&gt; linear RGB color conversion prior to being read by a shader and with
	 * the {@link Renderer#setLinearizeSrgbImages(boolean)} option is enabled.
	 *
	 * This option is only supported for the 8-bit color and grayscale image
	 * formats. Determines if the image is in SRGB color space or not.
	 *
	 * @return True, if the image is an SRGB image, false if it is linear RGB.
	 *
	 * @seealso Renderer#setLinearizeSrgbImages(boolean)
	 */
	ColorSpace getColorSpace();

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

	/**
	 * Deprecated units
	 */
	/**
	 * @deprecated This feature is no longer used by the engine
	 */
	void setEfficentData(Object efficientData);

	/**
	 * @deprecated This feature is no longer used by the engine
	 */
	Object getEfficentData();

}