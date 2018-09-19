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

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.CloneableSmartAsset;
import com.jme3.asset.TextureKey;
import com.jme3.export.*;
import com.jme3.util.PlaceholderAssets;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>Texture</code> defines a texture object to be used to display an
 * image on a piece of geometry. The image to be displayed is defined by the
 * <code>Image</code> class. All attributes required for texture mapping are
 * contained within this class. This includes mipmapping if desired,
 * magnificationFilter options, apply options and correction options. Default
 * values are as follows: minificationFilter - NearestNeighborNoMipMaps,
 * magnificationFilter - NearestNeighbor, wrap - EdgeClamp on S,T and R, apply -
 * Modulate, environment - None.
 *
 * @see com.jme3.texture.Image
 * @author Mark Powell
 * @author Joshua Slack
 * @version $Id: Texture.java 4131 2009-03-19 20:15:28Z blaine.dev $
 */
public abstract class Texture implements CloneableSmartAsset, Savable, Cloneable, ITexture {

    /**
     * The name of the texture (if loaded as a resource).
     */
    private String name = null;

    /**
     * The image stored in the texture
     */
    private Image image = null;

    /**
     * The texture key allows to reload a texture from a file
     * if needed.
     */
    private TextureKey key = null;

    private MinFilter minificationFilter = MinFilter.BilinearNoMipMaps;
    private MagFilter magnificationFilter = MagFilter.Bilinear;
    private ShadowCompareMode shadowCompareMode = ShadowCompareMode.Off;
    private int anisotropicFilter;

    /**
     * @return A cloned Texture object.
     */
    @Override
    public Texture clone(){
        try {
            return (Texture) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    /**
     * Constructor instantiates a new <code>Texture</code> object with default
     * attributes.
     */
    public Texture() {
    }

    /* (non-Javadoc)
	 * @see com.jme3.texture.ITexture#getMinFilter()
	 */
    @Override
	public MinFilter getMinFilter() {
        return minificationFilter;
    }

    /* (non-Javadoc)
	 * @see com.jme3.texture.ITexture#setMinFilter(com.jme3.texture.Texture.MinFilter)
	 */
    @Override
	public void setMinFilter(MinFilter minificationFilter) {
        if (minificationFilter == null) {
            throw new IllegalArgumentException(
                    "minificationFilter can not be null.");
        }
        this.minificationFilter = minificationFilter;
        if (minificationFilter.usesMipMapLevels() && image != null && !image.isGeneratedMipmapsRequired() && !image.hasMipmaps()) {
            image.setNeedGeneratedMipmaps();
        }
    }

    /* (non-Javadoc)
	 * @see com.jme3.texture.ITexture#getMagFilter()
	 */
    @Override
	public MagFilter getMagFilter() {
        return magnificationFilter;
    }

    /* (non-Javadoc)
	 * @see com.jme3.texture.ITexture#setMagFilter(com.jme3.texture.Texture.MagFilter)
	 */
    @Override
	public void setMagFilter(MagFilter magnificationFilter) {
        if (magnificationFilter == null) {
            throw new IllegalArgumentException(
                    "magnificationFilter can not be null.");
        }
        this.magnificationFilter = magnificationFilter;
    }

    /* (non-Javadoc)
	 * @see com.jme3.texture.ITexture#getShadowCompareMode()
	 */
    @Override
	public ShadowCompareMode getShadowCompareMode(){
        return shadowCompareMode;
    }

    /* (non-Javadoc)
	 * @see com.jme3.texture.ITexture#setShadowCompareMode(com.jme3.texture.Texture.ShadowCompareMode)
	 */
    @Override
	public void setShadowCompareMode(ShadowCompareMode compareMode){
        if (compareMode == null){
            throw new IllegalArgumentException(
                    "compareMode can not be null.");
        }
        this.shadowCompareMode = compareMode;
    }

    /**
     * <code>setImage</code> sets the image object that defines the texture.
     *
     * @param image
     *            the image that defines the texture.
     */
    public void setImage(Image image) {
        this.image = image;
        
        // Test if mipmap generation required.
        setMinFilter(getMinFilter());
    }

    /**
     * @param key The texture key that was used to load this texture
     */
    public void setKey(AssetKey key){
        this.key = (TextureKey) key;
    }

    public AssetKey getKey(){
        return this.key;
    }

    /**
     * <code>getImage</code> returns the image data that makes up this
     * texture. If no image data has been set, this will return null.
     *
     * @return the image data that makes up the texture.
     */
    public Image getImage() {
        return image;
    }

    /**
     * <code>setWrap</code> sets the wrap mode of this texture for a
     * particular axis.
     *
     * @param axis
     *            the texture axis to define a wrapmode on.
     * @param mode
     *            the wrap mode for the given axis of the texture.
     * @throws IllegalArgumentException
     *             if axis or mode are null or invalid for this type of texture
     */
    public abstract void setWrap(WrapAxis axis, WrapMode mode);

    /**
     * <code>setWrap</code> sets the wrap mode of this texture for all axis.
     *
     * @param mode
     *            the wrap mode for the given axis of the texture.
     * @throws IllegalArgumentException
     *             if mode is null or invalid for this type of texture
     */
    public abstract void setWrap(WrapMode mode);

    /**
     * <code>getWrap</code> returns the wrap mode for a given coordinate axis
     * on this texture.
     *
     * @param axis
     *            the axis to return for
     * @return the wrap mode of the texture.
     * @throws IllegalArgumentException
     *             if axis is null or invalid for this type of texture
     */
    public abstract WrapMode getWrap(WrapAxis axis);

    public abstract Type getType();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
	 * @see com.jme3.texture.ITexture#getAnisotropicFilter()
	 */
    @Override
	public int getAnisotropicFilter() {
        return anisotropicFilter;
    }

    /* (non-Javadoc)
	 * @see com.jme3.texture.ITexture#setAnisotropicFilter(int)
	 */
    @Override
	public void setAnisotropicFilter(int level) {
        anisotropicFilter = Math.max(0, level);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[name=").append(name);
        if (image != null) {
            sb.append(", image=").append(image.toString());
        }

        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Texture other = (Texture) obj;
        
        // NOTE: Since images are generally considered unique assets in jME3,
        // using the image's equals() implementation is not neccessary here
        // (would be too slow)
        if (this.image != other.image) {
            return false;
        }
        if (this.minificationFilter != other.minificationFilter) {
            return false;
        }
        if (this.magnificationFilter != other.magnificationFilter) {
            return false;
        }
        if (this.shadowCompareMode != other.shadowCompareMode) {
            return false;
        }
        if (this.anisotropicFilter != other.anisotropicFilter) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        // NOTE: Since images are generally considered unique assets in jME3,
        // using the image's hashCode() implementation is not neccessary here
        // (would be too slow)
        hash = 67 * hash + (this.image != null ? System.identityHashCode(this.image) : 0);
        hash = 67 * hash + (this.minificationFilter != null ? this.minificationFilter.hashCode() : 0);
        hash = 67 * hash + (this.magnificationFilter != null ? this.magnificationFilter.hashCode() : 0);
        hash = 67 * hash + (this.shadowCompareMode != null ? this.shadowCompareMode.hashCode() : 0);
        hash = 67 * hash + this.anisotropicFilter;
        return hash;
    }

   /* (non-Javadoc)
 * @see com.jme3.texture.ITexture#createSimpleClone(com.jme3.texture.Texture)
 */
    @Override
	@Deprecated
    public ITexture createSimpleClone(Texture rVal) {
        rVal.setMinFilter(minificationFilter);
        rVal.setMagFilter(magnificationFilter);
        rVal.setShadowCompareMode(shadowCompareMode);
        rVal.setAnisotropicFilter(anisotropicFilter);
        rVal.setImage(image); // NOT CLONED.
        rVal.setKey(key);
        rVal.setName(name);
        return rVal;
    }

    /**
     * @deprecated Use {@link Texture#clone()} instead.
     */
    @Deprecated
    public abstract ITexture createSimpleClone();

    @Override
    public void write(JmeExporter e) throws IOException {
        OutputCapsule capsule = e.getCapsule(this);
        capsule.write(name, "name", null);
        
        if (key == null){
            // no texture key is set, try to save image instead then
            capsule.write(image, "image", null);
        }else{
            capsule.write(key, "key", null);
        }
        
        capsule.write(anisotropicFilter, "anisotropicFilter", 1);
        capsule.write(minificationFilter, "minificationFilter",
                MinFilter.BilinearNoMipMaps);
        capsule.write(magnificationFilter, "magnificationFilter",
                MagFilter.Bilinear);
    }

    public void read(JmeImporter e) throws IOException {
        InputCapsule capsule = e.getCapsule(this);
        name = capsule.readString("name", null);
        key = (TextureKey) capsule.readSavable("key", null);
        
        // load texture from key, if available
        if (key != null) {
            // key is available, so try the texture from there.
            try {
                Texture loadedTex = e.getAssetManager().loadTexture(key);
                image = loadedTex.getImage();
            } catch (AssetNotFoundException ex){
                Logger.getLogger(Texture.class.getName()).log(Level.SEVERE, "Cannot locate texture {0}", key);
                image = PlaceholderAssets.getPlaceholderImage(e.getAssetManager());
            }
        }else{
            // no key is set on the texture. Attempt to load an embedded image
            image = (Image) capsule.readSavable("image", null);
            if (image == null){
                // TODO: what to print out here? the texture has no key or data, there's no useful information .. 
                // assume texture.name is set even though the key is null
                Logger.getLogger(Texture.class.getName()).log(Level.SEVERE, "Cannot load embedded image {0}", toString() );
            }
        }

        anisotropicFilter = capsule.readInt("anisotropicFilter", 1);
        minificationFilter = capsule.readEnum("minificationFilter",
                MinFilter.class,
                MinFilter.BilinearNoMipMaps);
        magnificationFilter = capsule.readEnum("magnificationFilter",
                MagFilter.class, MagFilter.Bilinear);
    }
}
