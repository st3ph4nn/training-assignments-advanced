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
package com.jme3.texture.plugins;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.TextureKey;
import com.jme3.math.FastMath;
import com.jme3.texture.Image;
import com.jme3.texture.Format;
import com.jme3.util.BufferUtils;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * <code>TextureManager</code> provides static methods for building a
 * <code>Texture</code> object. Typically, the information supplied is the
 * filename and the texture properties.
 * 
 * @author Mark Powell
 * @author Joshua Slack - cleaned, commented, added ability to read 16bit true color and color-mapped TGAs.
 * @author Kirill Vainer - ported to jME3
 * @version $Id: TGALoader.java 4131 2009-03-19 20:15:28Z blaine.dev $
 */
public final class TGALoader implements AssetLoader {

    // 0 - no image data in file
    public static final int TYPE_NO_IMAGE = 0;
    // 1 - uncompressed, color-mapped image
    public static final int TYPE_COLORMAPPED = 1;
    // 2 - uncompressed, true-color image
    public static final int TYPE_TRUECOLOR = 2;
    // 3 - uncompressed, black and white image
    public static final int TYPE_BLACKANDWHITE = 3;
    // 9 - run-length encoded, color-mapped image
    public static final int TYPE_COLORMAPPED_RLE = 9;
    // 10 - run-length encoded, true-color image
    public static final int TYPE_TRUECOLOR_RLE = 10;
    // 11 - run-length encoded, black and white image
    public static final int TYPE_BLACKANDWHITE_RLE = 11;

    public Object load(AssetInfo info) throws IOException {
        if (!(info.getKey() instanceof TextureKey)) {
            throw new IllegalArgumentException("Texture assets must be loaded using a TextureKey");
        }

        boolean flip = ((TextureKey) info.getKey()).isFlipY();
        InputStream in = null;
        try {
            in = info.openStream();
            Image img = load(in, flip);
            return img;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * <code>loadImage</code> is a manual image loader which is entirely
     * independent of AWT. OUT: RGB888 or RGBA8888 Image object
     * 
     * 
    
     * @param in
     *            InputStream of an uncompressed 24b RGB or 32b RGBA TGA
     * @param flip
     *            Flip the image vertically
     * @return <code>Image</code> object that contains the
     *         image, either as a RGB888 or RGBA8888
     * @throws java.io.IOException
     */
    public static Image load(InputStream in, boolean flip) throws IOException {
        TGAHeader tgaHeader = new TGAHeader(in, flip);
    	boolean flipH = false;

        // Skip image ID
        if (tgaHeader.getIdLength() > 0) {
            tgaHeader.getDis().skip(tgaHeader.getIdLength());
        }

        ColorMapEntry[] cMapEntries = null;
        cMapEntries = constructColorMapType(tgaHeader, cMapEntries);

        // Allocate image data array
        byte[] rawData = null;
        int dl;
        if (tgaHeader.getPixelDepth() == 32) {
            rawData = new byte[tgaHeader.getWidth() * tgaHeader.getHeight() * 4];
            dl = 4;
        } else {
            rawData = new byte[tgaHeader.getWidth() * tgaHeader.getHeight() * 3];
            dl = 3;
        }
        
        Format format = formatImageDataArray(tgaHeader, cMapEntries, rawData, dl);

        in.close();
        // Get a pointer to the image memory
        ByteBuffer scratch = imageMemoryPointer(rawData);
        // Create the Image object
        return createTextureImageObject(tgaHeader, format, scratch);
    }

	private static Format formatImageDataArray(TGAHeader tgaHeader, ColorMapEntry[] cMapEntries,
			byte[] rawData, int dl) throws IOException {
        int rawDataIndex = 0;
		
		if (tgaHeader.getImageType() == TYPE_TRUECOLOR) {
            // Faster than doing a 16-or-24-or-32 check on each individual pixel,
            // just make a seperate loop for each.
            return determineTrueColorFormatBasedOnPixelDepth(tgaHeader, rawData, dl, rawDataIndex);
        } else if (tgaHeader.getImageType() == TYPE_TRUECOLOR_RLE) {
            // Faster than doing a 16-or-24-or-32 check on each individual pixel,
            // just make a seperate loop for each.
            return determineTrueColorRleBasedOnPixelDepth(tgaHeader, rawData, dl, rawDataIndex);
        } else if (tgaHeader.getImageType() == TYPE_COLORMAPPED) {
            return determineColorMappedFormatBasedOnBytesPerIndex(tgaHeader, cMapEntries, rawData, dl, rawDataIndex);
        } else {
            throw new IOException("Monochrome and RLE colormapped images are not supported");
        }
	}

	private static ByteBuffer imageMemoryPointer(byte[] rawData) {
		ByteBuffer scratch = BufferUtils.createByteBuffer(rawData.length);
        scratch.clear();
        scratch.put(rawData);
        scratch.rewind();
		return scratch;
	}

	private static Image createTextureImageObject(TGAHeader tgaHeader, Format format, ByteBuffer scratch) {
		Image textureImage = new Image();
        textureImage.setFormat(format);
        textureImage.setWidth(tgaHeader.getWidth());
        textureImage.setHeight(tgaHeader.getHeight());
        textureImage.setData(scratch);
		return textureImage;
	}

	private static Format determineColorMappedFormatBasedOnBytesPerIndex(TGAHeader tgaHeader, ColorMapEntry[] cMapEntries, byte[] rawData, int dl, int rawDataIndex)
			throws IOException {
		Format format;
		int bytesPerIndex = tgaHeader.getPixelDepth() / 8;
		if (bytesPerIndex == 1) {
		    for (int i = 0; i <= (tgaHeader.getHeight() - 1); i++) {
		        if (!tgaHeader.isFlip()) {
		            rawDataIndex = (tgaHeader.getHeight() - 1 - i) * tgaHeader.getWidth() * dl;
		        }
		        for (int j = 0; j < tgaHeader.getWidth(); j++) {
		            int index = tgaHeader.getDis().readUnsignedByte();
		            if (index >= cMapEntries.length || index < 0) {
		                throw new IOException("TGA: Invalid color map entry referenced: " + index);
		            }

		            ColorMapEntry entry = cMapEntries[index];
		            rawData[rawDataIndex++] = entry.blue;
		            rawData[rawDataIndex++] = entry.green;
		            rawData[rawDataIndex++] = entry.red;
		            if (dl == 4) {
		                rawData[rawDataIndex++] = entry.alpha;
		            }

		        }
		    }
		} else if (bytesPerIndex == 2) {
		    for (int i = 0; i <= (tgaHeader.getHeight() - 1); i++) {
		        if (!tgaHeader.isFlip()) {
		            rawDataIndex = (tgaHeader.getHeight() - 1 - i) * tgaHeader.getWidth() * dl;
		        }
		        for (int j = 0; j < tgaHeader.getWidth(); j++) {
		            int index = TGAHeader.flipEndian(tgaHeader.getDis().readShort());
		            if (index >= cMapEntries.length || index < 0) {
		                throw new IOException("TGA: Invalid color map entry referenced: " + index);
		            }

		            ColorMapEntry entry = cMapEntries[index];
		            rawData[rawDataIndex++] = entry.blue;
		            rawData[rawDataIndex++] = entry.green;
		            rawData[rawDataIndex++] = entry.red;
		            if (dl == 4) {
		                rawData[rawDataIndex++] = entry.alpha;
		            }
		        }
		    }
		} else {
		    throw new IOException("TGA: unknown colormap indexing size used: " + bytesPerIndex);
		}

		format = dl == 4 ? Format.RGBA8 : Format.RGB8;
		return format;
	}

	private static Format determineTrueColorRleBasedOnPixelDepth(TGAHeader tgaHeader, byte[] rawData, int dl, int rawDataIndex) throws IOException {
		byte red;
		byte green;
		byte blue;
		byte alpha;
		if (tgaHeader.getPixelDepth() == 32) {
		    for (int i = 0; i <= (tgaHeader.getHeight() - 1); ++i) {
		        if (!tgaHeader.isFlip()) {
		            rawDataIndex = (tgaHeader.getHeight() - 1 - i) * tgaHeader.getWidth() * dl;
		        }

		        for (int j = 0; j < tgaHeader.getWidth(); ++j) {
		            // Get the number of pixels the next chunk covers (either packed or unpacked)
		            int count = tgaHeader.getDis().readByte();
		            if ((count & 0x80) != 0) {
		                // Its an RLE packed block - use the following 1 pixel for the next <count> pixels
		                count &= 0x07f;
		                j += count;
		                blue = tgaHeader.getDis().readByte();
		                green = tgaHeader.getDis().readByte();
		                red = tgaHeader.getDis().readByte();
		                alpha = tgaHeader.getDis().readByte();
		                while (count-- >= 0) {
		                    rawData[rawDataIndex++] = red;
		                    rawData[rawDataIndex++] = green;
		                    rawData[rawDataIndex++] = blue;
		                    rawData[rawDataIndex++] = alpha;
		                }
		            } else {
		                // Its not RLE packed, but the next <count> pixels are raw.
		                j += count;
		                while (count-- >= 0) {
		                    blue = tgaHeader.getDis().readByte();
		                    green = tgaHeader.getDis().readByte();
		                    red = tgaHeader.getDis().readByte();
		                    alpha = tgaHeader.getDis().readByte();
		                    rawData[rawDataIndex++] = red;
		                    rawData[rawDataIndex++] = green;
		                    rawData[rawDataIndex++] = blue;
		                    rawData[rawDataIndex++] = alpha;
		                }
		            }
		        }
		    }
		    return Format.RGBA8;
		} else if (tgaHeader.getPixelDepth() == 24) {
		    for (int i = 0; i <= (tgaHeader.getHeight() - 1); i++) {
		        if (!tgaHeader.isFlip()) {
		            rawDataIndex = (tgaHeader.getHeight() - 1 - i) * tgaHeader.getWidth() * dl;
		        }
		        for (int j = 0; j < tgaHeader.getWidth(); ++j) {
		            // Get the number of pixels the next chunk covers (either packed or unpacked)
		            int count = tgaHeader.getDis().readByte();
		            if ((count & 0x80) != 0) {
		                // Its an RLE packed block - use the following 1 pixel for the next <count> pixels
		                count &= 0x07f;
		                j += count;
		                blue = tgaHeader.getDis().readByte();
		                green = tgaHeader.getDis().readByte();
		                red = tgaHeader.getDis().readByte();
		                while (count-- >= 0) {
		                    rawData[rawDataIndex++] = red;
		                    rawData[rawDataIndex++] = green;
		                    rawData[rawDataIndex++] = blue;
		                }
		            } else {
		                // Its not RLE packed, but the next <count> pixels are raw.
		                j += count;
		                while (count-- >= 0) {
		                    blue = tgaHeader.getDis().readByte();
		                    green = tgaHeader.getDis().readByte();
		                    red = tgaHeader.getDis().readByte();
		                    rawData[rawDataIndex++] = red;
		                    rawData[rawDataIndex++] = green;
		                    rawData[rawDataIndex++] = blue;
		                }
		            }
		        }
		    }
		    return Format.RGB8;
		} else if (tgaHeader.getPixelDepth() == 16) {
		    byte[] data = new byte[2];
		    float scalar = 255f / 31f;
		    for (int i = 0; i <= (tgaHeader.getHeight() - 1); i++) {
		        if (!tgaHeader.isFlip()) {
		            rawDataIndex = (tgaHeader.getHeight() - 1 - i) * tgaHeader.getWidth() * dl;
		        }
		        for (int j = 0; j < tgaHeader.getWidth(); j++) {
		            // Get the number of pixels the next chunk covers (either packed or unpacked)
		            int count = tgaHeader.getDis().readByte();
		            if ((count & 0x80) != 0) {
		                // Its an RLE packed block - use the following 1 pixel for the next <count> pixels
		                count &= 0x07f;
		                j += count;
		                data[1] = tgaHeader.getDis().readByte();
		                data[0] = tgaHeader.getDis().readByte();
		                blue = (byte) (int) (getBitsAsByte(data, 1, 5) * scalar);
		                green = (byte) (int) (getBitsAsByte(data, 6, 5) * scalar);
		                red = (byte) (int) (getBitsAsByte(data, 11, 5) * scalar);
		                while (count-- >= 0) {
		                    rawData[rawDataIndex++] = red;
		                    rawData[rawDataIndex++] = green;
		                    rawData[rawDataIndex++] = blue;
		                }
		            } else {
		                // Its not RLE packed, but the next <count> pixels are raw.
		                j += count;
		                while (count-- >= 0) {
		                    data[1] = tgaHeader.getDis().readByte();
		                    data[0] = tgaHeader.getDis().readByte();
		                    blue = (byte) (int) (getBitsAsByte(data, 1, 5) * scalar);
		                    green = (byte) (int) (getBitsAsByte(data, 6, 5) * scalar);
		                    red = (byte) (int) (getBitsAsByte(data, 11, 5) * scalar);
		                    rawData[rawDataIndex++] = red;
		                    rawData[rawDataIndex++] = green;
		                    rawData[rawDataIndex++] = blue;
		                }
		            }
		        }
		    }
		    return Format.RGB8;
		} else {
		    throw new IOException("Unsupported TGA true color depth: " + tgaHeader.getPixelDepth());
		}
	}

	private static Format determineTrueColorFormatBasedOnPixelDepth(TGAHeader tgaHeader, byte[] rawData, int dl, int rawDataIndex) throws IOException {
		byte red;
		byte green;
		byte blue;
		byte alpha;
		if (tgaHeader.getPixelDepth() == 16) {
		    byte[] data = new byte[2];
		    float scalar = 255f / 31f;
		    for (int i = 0; i <= (tgaHeader.getHeight() - 1); i++) {
		        if (!tgaHeader.isFlip()) {
		            rawDataIndex = (tgaHeader.getHeight() - 1 - i) * tgaHeader.getWidth() * dl;
		        }
		        for (int j = 0; j < tgaHeader.getWidth(); j++) {
		            data[1] = tgaHeader.getDis().readByte();
		            data[0] = tgaHeader.getDis().readByte();
		            rawData[rawDataIndex++] = (byte) (int) (getBitsAsByte(data, 1, 5) * scalar);
		            rawData[rawDataIndex++] = (byte) (int) (getBitsAsByte(data, 6, 5) * scalar);
		            rawData[rawDataIndex++] = (byte) (int) (getBitsAsByte(data, 11, 5) * scalar);
		            if (dl == 4) {
		                // create an alpha channel
		                alpha = getBitsAsByte(data, 0, 1);
		                if (alpha == 1) {
		                    alpha = (byte) 255;
		                }
		                rawData[rawDataIndex++] = alpha;
		            }
		        }
		    }

		    return dl == 4 ? Format.RGBA8 : Format.RGB8;
		} else if (tgaHeader.getPixelDepth() == 24) {
		    for (int y = 0; y < tgaHeader.getHeight(); y++) {
		        if (!tgaHeader.isFlip()) {
		            rawDataIndex = (tgaHeader.getHeight() - 1 - y) * tgaHeader.getWidth() * dl;
		        } else {
		            rawDataIndex = y * tgaHeader.getWidth() * dl;
		        }

		        tgaHeader.getDis().readFully(rawData, rawDataIndex, tgaHeader.getWidth() * dl);
//                    for (int x = 0; x < width; x++) {
		        //read scanline
//                        blue = dis.readByte();
//                        green = dis.readByte();
//                        red = dis.readByte();
//                        rawData[rawDataIndex++] = red;
//                        rawData[rawDataIndex++] = green;
//                        rawData[rawDataIndex++] = blue;
//                    }
		    }
		    return Format.BGR8;
		} else if (tgaHeader.getPixelDepth() == 32) {
		    for (int i = 0; i <= (tgaHeader.getHeight() - 1); i++) {
		        if (!tgaHeader.isFlip()) {
		            rawDataIndex = (tgaHeader.getHeight() - 1 - i) * tgaHeader.getWidth() * dl;
		        }

		        for (int j = 0; j < tgaHeader.getWidth(); j++) {
		            blue = tgaHeader.getDis().readByte();
		            green = tgaHeader.getDis().readByte();
		            red = tgaHeader.getDis().readByte();
		            alpha = tgaHeader.getDis().readByte();
		            rawData[rawDataIndex++] = red;
		            rawData[rawDataIndex++] = green;
		            rawData[rawDataIndex++] = blue;
		            rawData[rawDataIndex++] = alpha;
		        }
		    }
		    return Format.RGBA8;
		} else {
		    throw new IOException("Unsupported TGA true color depth: " + tgaHeader.getPixelDepth());
		}
	}

	private static ColorMapEntry[] constructColorMapType(TGAHeader tgaHeader, ColorMapEntry[] cMapEntries) throws IOException {
		if (tgaHeader.getColorMapType() != 0) {
            // read the color map.
            int bytesInColorMap = (tgaHeader.getcMapDepth()*tgaHeader.getcMapLength()) >> 3;
            int bitsPerColor = Math.min(tgaHeader.getcMapDepth() / 3, 8);

            byte[] cMapData = new byte[bytesInColorMap];
            tgaHeader.getDis().read(cMapData);

            // Only go to the trouble of constructing the color map
            // table if this is declared a color mapped image.
            cMapEntries = constructColorMappedImage(tgaHeader, cMapEntries, bitsPerColor,
					cMapData);
        }
		return cMapEntries;
	}

	private static ColorMapEntry[] constructColorMappedImage(TGAHeader tgaHeader, ColorMapEntry[] cMapEntries, int bitsPerColor, byte[] cMapData) {
		if (tgaHeader.getImageType() == TYPE_COLORMAPPED || tgaHeader.getImageType() == TYPE_COLORMAPPED_RLE) {
		    cMapEntries = new ColorMapEntry[tgaHeader.getcMapLength()];
		    int alphaSize = tgaHeader.getcMapDepth() - (3 * bitsPerColor);
		    float scalar = 255f / (FastMath.pow(2, bitsPerColor) - 1);
		    float alphaScalar = 255f / (FastMath.pow(2, alphaSize) - 1);
		    for (int i = 0; i < tgaHeader.getcMapLength(); i++) {
		        ColorMapEntry entry = returnColorMapEntryAsByte(tgaHeader, bitsPerColor, cMapData, alphaSize,
						scalar, alphaScalar, i);
		        cMapEntries[i] = entry;
		    }
		}
		return cMapEntries;
	}

	private static ColorMapEntry returnColorMapEntryAsByte(TGAHeader tgaHeader, int bitsPerColor, byte[] cMapData,
			int alphaSize, float scalar, float alphaScalar, int i) {
		ColorMapEntry entry = new ColorMapEntry();
		int offset = tgaHeader.getcMapDepth() * i;
		entry.red = (byte) (int) (getBitsAsByte(cMapData, offset, bitsPerColor) * scalar);
		entry.green = (byte) (int) (getBitsAsByte(cMapData, offset + bitsPerColor, bitsPerColor) * scalar);
		entry.blue = (byte) (int) (getBitsAsByte(cMapData, offset + (2 * bitsPerColor), bitsPerColor) * scalar);
		if (alphaSize <= 0) {
		    entry.alpha = (byte) 255;
		} else {
		    entry.alpha = (byte) (int) (getBitsAsByte(cMapData, offset + (3 * bitsPerColor), alphaSize) * alphaScalar);
		}
		return entry;
	}

    private static byte getBitsAsByte(byte[] data, int offset, int length) {
        int offsetBytes = offset / 8;
        int indexBits = offset % 8;
        int rVal = 0;

        // start at data[offsetBytes]...  spill into next byte as needed.
        for (int i = length; --i >= 0;) {
            byte b = data[offsetBytes];
            int test = indexBits == 7 ? 1 : 2 << (6 - indexBits);
            if ((b & test) != 0) {
                if (i == 0) {
                    rVal++;
                } else {
                    rVal += (2 << i - 1);
                }
            }
            indexBits++;
            if (indexBits == 8) {
                indexBits = 0;
                offsetBytes++;
            }
        }

        return (byte) rVal;
    }



    static class ColorMapEntry {

        byte red, green, blue, alpha;

        @Override
        public String toString() {
            return "entry: " + red + "," + green + "," + blue + "," + alpha;
        }
    }
}
