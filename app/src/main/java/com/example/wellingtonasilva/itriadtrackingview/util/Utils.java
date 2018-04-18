package com.example.wellingtonasilva.itriadtrackingview.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class Utils
{
    public static String loadJSONFromAsset(Context context, String fileJson)
    {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileJson);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
