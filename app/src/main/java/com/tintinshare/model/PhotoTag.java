package com.tintinshare.model;


import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by longjianlin on 15/3/19.
 */
public class PhotoTag {
    private final float mX;
    private final float mY;

    public PhotoTag(float x, float y) {
        mX = x;
        mY = y;
    }

    public PhotoTag(JSONObject object) throws JSONException {
        this((float) object.getDouble("x"), (float) object.getDouble("y"));
    }

    public PhotoTag(float x, float y, float bitmapWidth, float bitmapHeight) {
        this(100 * x / bitmapWidth, 100 * y / bitmapHeight);
    }

    public PhotoTag(float x, float y, int bitmapWidth, int bitmapHeight) {
        this(x, y, (float) bitmapHeight, (float) bitmapWidth);
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }


    public JSONObject toJsonObject() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("x", mX);
        object.put("y", mY);
        return object;
    }
}
