package com.example.android.popular_movie.Utilities;

import android.content.Context;

public class LayoutUtilities {
    public static float convertPxToDp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }
}
