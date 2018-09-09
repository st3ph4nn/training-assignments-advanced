package com.jme3.texture.plugins;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TGAHeader {

	private boolean flip;
	private boolean flipH = false;
	private DataInputStream dis;
	private int idLength;
	private int colorMapType;
	private int imageType;
	private short cMapLength;
	private int cMapDepth;
	private int width;
	private int height;
	private int pixelDepth;
    private int imageDescriptor;
	
	public TGAHeader(InputStream in, boolean flip) throws IOException {
		// open a stream to the file
        this.dis = new DataInputStream(new BufferedInputStream(in));
        
     // ---------- Start Reading the TGA header ---------- //
        // length of the image id (1 byte)
        this.idLength = dis.readUnsignedByte();

        // Type of color map (if any) included with the image
        // 0 - no color map data is included
        // 1 - a color map is included
        this.colorMapType = dis.readUnsignedByte();

        // Type of image being read:
        this.imageType = dis.readUnsignedByte();
        
     // Read Color Map Specification (5 bytes)
        // Index of first color map entry (if we want to use it, uncomment and remove extra read.)
//        short cMapStart = flipEndian(dis.readShort());
        dis.readShort();
        // number of entries in the color map
        this.cMapLength = flipEndian(dis.readShort());
        // number of bits per color map entry
        this.cMapDepth = dis.readUnsignedByte();

        // Read Image Specification (10 bytes)
        // horizontal coordinate of lower left corner of image. (if we want to use it, uncomment and remove extra read.)
//        int xOffset = flipEndian(dis.readShort());
        dis.readShort();
        // vertical coordinate of lower left corner of image. (if we want to use it, uncomment and remove extra read.)
//        int yOffset = flipEndian(dis.readShort());
        dis.readShort();
        // width of image - in pixels
        this.width = flipEndian(dis.readShort());
        // height of image - in pixels
        this.height = flipEndian(dis.readShort());
        // bits per pixel in image.
        this.pixelDepth = dis.readUnsignedByte();
        this.imageDescriptor = dis.readUnsignedByte();
        
        if ((imageDescriptor & 32) != 0) // bit 5 : if 1, flip top/bottom ordering
        {
            this.flip = !flip;
        }
        if ((imageDescriptor & 16) != 0) // bit 4 : if 1, flip left/right ordering
        {
            this.flipH = !false;
        }

        // ---------- Done Reading the TGA header ---------- //
        
	}
	
	
    public boolean isFlip() {
		return flip;
	}


	public void setFlip(boolean flip) {
		this.flip = flip;
	}


	public boolean isFlipH() {
		return flipH;
	}



	public void setFlipH(boolean flipH) {
		this.flipH = flipH;
	}



	public DataInputStream getDis() {
		return dis;
	}



	public void setDis(DataInputStream dis) {
		this.dis = dis;
	}



	public int getIdLength() {
		return idLength;
	}



	public void setIdLength(int idLength) {
		this.idLength = idLength;
	}



	public int getColorMapType() {
		return colorMapType;
	}



	public void setColorMapType(int colorMapType) {
		this.colorMapType = colorMapType;
	}



	public int getImageType() {
		return imageType;
	}



	public void setImageType(int imageType) {
		this.imageType = imageType;
	}



	public short getcMapLength() {
		return cMapLength;
	}



	public void setcMapLength(short cMapLength) {
		this.cMapLength = cMapLength;
	}



	public int getcMapDepth() {
		return cMapDepth;
	}



	public void setcMapDepth(int cMapDepth) {
		this.cMapDepth = cMapDepth;
	}



	public int getWidth() {
		return width;
	}



	public void setWidth(int width) {
		this.width = width;
	}



	public int getHeight() {
		return height;
	}



	public void setHeight(int height) {
		this.height = height;
	}



	public int getPixelDepth() {
		return pixelDepth;
	}



	public void setPixelDepth(int pixelDepth) {
		this.pixelDepth = pixelDepth;
	}



	public int getImageDescriptor() {
		return imageDescriptor;
	}



	public void setImageDescriptor(int imageDescriptor) {
		this.imageDescriptor = imageDescriptor;
	}







	/**
     * <code>flipEndian</code> is used to flip the endian bit of the header
     * file.
     * 
     * @param signedShort
     *            the bit to flip.
     * @return the flipped bit.
     */
    protected static short flipEndian(short signedShort) {
        int input = signedShort & 0xFFFF;
        return (short) (input << 8 | (input & 0xFF00) >>> 8);
    }

}
