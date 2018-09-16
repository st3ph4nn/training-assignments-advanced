package com.jme3.texture;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.texture.image.ColorSpace;

public class ImageReaderSaver extends Image implements Savable {

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
