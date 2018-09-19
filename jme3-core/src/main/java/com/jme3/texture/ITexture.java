package com.jme3.texture;

public interface ITexture {

	/**
	 * @return the MinificationFilterMode of this texture.
	 */
	MinFilter getMinFilter();

	/**
	 * @param minificationFilter
	 *            the new MinificationFilterMode for this texture.
	 * @throws IllegalArgumentException
	 *             if minificationFilter is null
	 */
	void setMinFilter(MinFilter minificationFilter);

	/**
	 * @return the MagnificationFilterMode of this texture.
	 */
	MagFilter getMagFilter();

	/**
	 * @param magnificationFilter
	 *            the new MagnificationFilter for this texture.
	 * @throws IllegalArgumentException
	 *             if magnificationFilter is null
	 */
	void setMagFilter(MagFilter magnificationFilter);

	/**
	 * @return The ShadowCompareMode of this texture.
	 * @see ShadowCompareMode
	 */
	ShadowCompareMode getShadowCompareMode();

	/**
	 * @param compareMode
	 *            the new ShadowCompareMode for this texture.
	 * @throws IllegalArgumentException
	 *             if compareMode is null
	 * @see ShadowCompareMode
	 */
	void setShadowCompareMode(ShadowCompareMode compareMode);

	/**
	 * @return the anisotropic filtering level for this texture. Default value
	 * is 0 (use value from config), 
	 * 1 means 1x (no anisotropy), 2 means x2, 4 is x4, etc.
	 */
	int getAnisotropicFilter();

	/**
	 * @param level
	 *            the anisotropic filtering level for this texture.
	 */
	void setAnisotropicFilter(int level);

	/** Retrieve a basic clone of this Texture (ie, clone everything but the
	 * image data, which is shared)
	 *
	 * @return Texture
	 * 
	 * @deprecated Use {@link Texture#clone()} instead.
	 */
	ITexture createSimpleClone(Texture rVal);

}