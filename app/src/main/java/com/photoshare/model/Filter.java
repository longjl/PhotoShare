package com.photoshare.model;

import com.photoshare.R;

/**
 * Created by longjianlin on 15/3/19.
 */
public enum Filter {

    // DO NOT CHANGE ORDER DUE TO INSTANT UPLOAD FILTER PREF!
    ORIGINAL(R.string.filter_original),
    INSTAFIX(R.string.filter_instafix),
    ANSEL(R.string.filter_ansel),
    TESTINO(R.string.filter_testino),
    XPRO(R.string.filter_xpro),
    RETRO(R.string.filter_retro),
    BW(R.string.filter_bw),
    SEPIA(R.string.filter_sepia),
    CYANO(R.string.filter_cyano),
    GEORGIA(R.string.filter_georgia),
    SAHARA(R.string.filter_sahara),
    HDR(R.string.filter_hdr);

    public static Filter mapFromId(int id) {
        try {
            return values()[id];
        } catch (Exception e) {
            return null;
        }
    }

    public static Filter mapFromPref(String preference) {
        Filter returnValue;
        try {
            int id = Integer.parseInt(preference);
            returnValue = mapFromId(id);
        } catch (Exception e) {
            returnValue = ORIGINAL;
        }
        return returnValue;
    }

    private final int mLabelId;

    private Filter(int labelId) {
        mLabelId = labelId;
    }

    public int getId() {
        return ordinal();
    }

    public int getLabelId() {
        return mLabelId;
    }

    public String mapToPref() {
        return String.valueOf(getId());
    }
}
