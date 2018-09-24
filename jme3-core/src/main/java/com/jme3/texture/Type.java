package com.jme3.texture;

public enum Type {

    /**
     * Two dimensional texture (default). A rectangle.
     */
    TwoDimensional,
    
    /**
     * An array of two dimensional textures. 
     */
    TwoDimensionalArray,

    /**
     * Three dimensional texture. (A cube)
     */
    ThreeDimensional,

    /**
     * A set of 6 TwoDimensional textures arranged as faces of a cube facing
     * inwards.
     */
    CubeMap;
}