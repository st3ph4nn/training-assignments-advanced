package com.jme3.texture;

public enum WrapMode {
    /**
     * Only the fractional portion of the coordinate is considered.
     */
    Repeat,
    
    /**
     * Only the fractional portion of the coordinate is considered, but if
     * the integer portion is odd, we'll use 1 - the fractional portion.
     * (Introduced around OpenGL1.4) Falls back on Repeat if not supported.
     */
    MirroredRepeat,
    
    /**
     * coordinate will be clamped to [0,1]
     * 
     * @deprecated Not supported by OpenGL 3
     */
    @Deprecated
    Clamp,
    /**
     * mirrors and clamps the texture coordinate, where mirroring and
     * clamping a value f computes:
     * <code>mirrorClamp(f) = min(1, max(1/(2*N),
     * abs(f)))</code> where N
     * is the size of the one-, two-, or three-dimensional texture image in
     * the direction of wrapping. (Introduced after OpenGL1.4) Falls back on
     * Clamp if not supported.
     * 
     * @deprecated Not supported by OpenGL 3
     */
    @Deprecated
    MirrorClamp,
    
    /**
     * coordinate will be clamped to the range [-1/(2N), 1 + 1/(2N)] where N
     * is the size of the texture in the direction of clamping. Falls back
     * on Clamp if not supported.
     * 
     * @deprecated Not supported by OpenGL 3 or OpenGL ES 2
     */ 
    @Deprecated
    BorderClamp,
    /**
     * Wrap mode MIRROR_CLAMP_TO_BORDER_EXT mirrors and clamps to border the
     * texture coordinate, where mirroring and clamping to border a value f
     * computes:
     * <code>mirrorClampToBorder(f) = min(1+1/(2*N), max(1/(2*N), abs(f)))</code>
     * where N is the size of the one-, two-, or three-dimensional texture
     * image in the direction of wrapping." (Introduced after OpenGL1.4)
     * Falls back on BorderClamp if not supported.
     * 
     * @deprecated Not supported by OpenGL 3
     */
    @Deprecated
    MirrorBorderClamp,
    /**
     * coordinate will be clamped to the range [1/(2N), 1 - 1/(2N)] where N
     * is the size of the texture in the direction of clamping. Falls back
     * on Clamp if not supported.
     */
    EdgeClamp,
    
    /**
     * mirrors and clamps to edge the texture coordinate, where mirroring
     * and clamping to edge a value f computes:
     * <code>mirrorClampToEdge(f) = min(1-1/(2*N), max(1/(2*N), abs(f)))</code>
     * where N is the size of the one-, two-, or three-dimensional texture
     * image in the direction of wrapping. (Introduced after OpenGL1.4)
     * Falls back on EdgeClamp if not supported.
     * 
     * @deprecated Not supported by OpenGL 3
     */
    MirrorEdgeClamp;
}