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
package com.jme3.texture.image;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.texture.Image;
import java.nio.ByteBuffer;

public class DefaultImageRaster extends ImageRaster {
    
  
    private final int width;
    private final int height;
    private final int offset;
    private final boolean convertToLinear;
 
    
    private void rangeCheck(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new IllegalArgumentException("x and y must be inside the image dimensions:" 
                                                + x + ", " + y + " in:" + width + ", " + height);
        }
    }
    
    public DefaultImageRaster(Image image, int slice, int mipMapLevel, boolean convertToLinear) {
        int[] mipMapSizes = image.getMipMapSizes();
        int availableMips = mipMapSizes != null ? mipMapSizes.length : 1;
        
        if (mipMapLevel >= availableMips) {
            throw new IllegalStateException("Cannot create image raster for mipmap level #" + mipMapLevel + ". "
                                          + "Image only has " + availableMips + " mipmap levels.");
        }
        
        if (image.hasMipmaps()) {
            this.width  = Math.max(1, image.getWidth()  >> mipMapLevel);
            this.height = Math.max(1, image.getHeight() >> mipMapLevel);
            
            int mipOffset = 0;
            for (int i = 0; i < mipMapLevel; i++) {
                mipOffset += mipMapSizes[i];
            }
            
            this.offset = mipOffset;
        } else {
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.offset = 0;
        }
        
        super.image = image;
        super.slice = slice;
        
        // Conversion to linear only needed if image's color space is sRGB.
        this.convertToLinear = convertToLinear && image.getColorSpace() == ColorSpace.sRGB;
        
        super.buffer = image.getData(slice);
        super.codec = ImageCodec.lookup(image.getFormat());
        
        setTempBasedOnCodec();
    }
    
    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setPixel(int x, int y, ColorRGBA color) {
        rangeCheck(x, y);
        
        if (convertToLinear) {
            // Input is linear, needs to be converted to sRGB before writing
            // into image.
            color = color.getAsSrgb();
        }
        
        // Check flags for grayscale
        checkFlagsForGrayscale(color);
        codec.writeComponents(super.getBuffer(), x, y, width, offset, components, temp);
        image.setUpdateNeeded();
    }
    

    @Override
    public ColorRGBA getPixel(int x, int y, ColorRGBA store) {
        rangeCheck(x, y);
        
        codec.readComponents(getBuffer(), x, y, width, offset, components, temp);
        storeValidator(store, components);
        
        if (convertToLinear) {
            // Input image is sRGB, need to convert to linear.
            store.setAsSrgb(store.r, store.g, store.b, store.a);
        }
        
        return store;
    }
}
