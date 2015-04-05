package com.photoshare.model;

/**
 * Created by longjianlin on 15/3/19.
 */
public enum UploadQuality {
    LOW(640, 75), MEDIUM(1024, 80), HIGH(2048, 85), ORIGINAL(Integer.MAX_VALUE, 90);

    private final int mMaxDimension, mJpegQuality;

    private UploadQuality(int maxDimension, int jpegQuality) {
        mMaxDimension = maxDimension;
        mJpegQuality = jpegQuality;
    }

    public int getMaxDimension() {
        return mMaxDimension;
    }

    public int getJpegQuality() {
        return mJpegQuality;
    }

    public boolean requiresResizing() {
        return mMaxDimension < Integer.MAX_VALUE;
    }

    public static UploadQuality mapFromButtonId(int buttonId) {
        return UploadQuality.LOW;
    }

    public static UploadQuality mapFromPreference(String value) {
        UploadQuality returnValue = MEDIUM;

        if ("0".equals(value)) {
            returnValue = LOW;
        } else if ("1".equals(value)) {
            returnValue = MEDIUM;
        } else if ("2".equals(value)) {
            returnValue = HIGH;
        } else if ("3".equals(value)) {
            returnValue = ORIGINAL;
        }

        return returnValue;
    }
}
