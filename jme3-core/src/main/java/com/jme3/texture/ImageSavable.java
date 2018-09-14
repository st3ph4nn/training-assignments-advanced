
public class ImageSavable implements Savable {

	
	
	 * Sets the update needed flag, while also checking if mipmaps
     * need to be regenerated.
     */
    @Override
    public void setUpdateNeeded() {
        super.setUpdateNeeded();
        if (isGeneratedMipmapsRequired() && !hasMipmaps()) {
            // Mipmaps are no longer valid, since the image was changed.
            setMipmapsGenerated(false);
        }
    }
	
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
	    
	    @Override
	    public void deleteObject(Object rendererObject) {
	        ((Renderer)rendererObject).deleteImage(this);
	    }

	    @Override
	    public NativeObject createDestructableClone() {
	        return new Image(id);
	    }

	    @Override
	    public long getUniqueId() {
	        return ((long)OBJTYPE_TEXTURE << 32) | ((long)id);
	    }
	    
	    /**
	     * @return A shallow clone of this image. The data is not cloned.
	     */
	    @Override
	    public Image clone(){
	        Image clone = (Image) super.clone();
	        clone.mipMapSizes = mipMapSizes != null ? mipMapSizes.clone() : null;
	        clone.data = data != null ? new ArrayList<ByteBuffer>(data) : null;
	        clone.lastTextureState = new LastTextureState();
	        clone.setUpdateNeeded();
	        return clone;
	    }
	
	    @Override
	    public String toString(){
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

	    @Override
	    public boolean equals(Object other) {
	        if (other == this) {
	            return true;
	        }
	        if (!(other instanceof Image)) {
	            return false;
	        }
	        Image that = (Image) other;
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
	        if (this.getMipMapSizes() != null
	                && !Arrays.equals(this.getMipMapSizes(), that.getMipMapSizes()))
	            return false;
	        if (this.getMipMapSizes() == null && that.getMipMapSizes() != null)
	            return false;
	        if (this.getMultiSamples() != that.getMultiSamples())
	            return false;
	        
	        return true;
	    }

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

	    @Override
	    public void write(JmeExporter e) throws IOException {
	        OutputCapsule capsule = e.getCapsule(this);
	        capsule.write(format, "format", Format.RGBA8);
	        capsule.write(width, "width", 0);
	        capsule.write(height, "height", 0);
	        capsule.write(depth, "depth", 0);
	        capsule.write(mipMapSizes, "mipMapSizes", null);
	        capsule.write(multiSamples, "multiSamples", 1);
	        capsule.writeByteBufferArrayList(data, "data", null);
	        capsule.write(colorSpace, "colorSpace", null);
	    }

	    @Override
	    public void read(JmeImporter e) throws IOException {
	        InputCapsule capsule = e.getCapsule(this);
	        format = capsule.readEnum("format", Format.class, Format.RGBA8);
	        width = capsule.readInt("width", 0);
	        height = capsule.readInt("height", 0);
	        depth = capsule.readInt("depth", 0);
	        mipMapSizes = capsule.readIntArray("mipMapSizes", null);
	        multiSamples = capsule.readInt("multiSamples", 1);
	        data = (ArrayList<ByteBuffer>) capsule.readByteBufferArrayList("data", null);
	        colorSpace = capsule.readEnum("colorSpace", ColorSpace.class, null);

	        if (mipMapSizes != null) {
	            needGeneratedMips = false;
	            mipsWereGenerated = true;
	        }
	    }
	    
	    
}
