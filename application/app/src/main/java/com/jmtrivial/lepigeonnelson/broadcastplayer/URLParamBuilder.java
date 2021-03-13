package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.os.Build;
import android.util.ArrayMap;

import androidx.annotation.RequiresApi;

import java.util.Map;

public class URLParamBuilder {
    private ArrayMap<String, String> values;

    public URLParamBuilder() {
        values = new ArrayMap<>();
    }

    public URLParamBuilder addParameter(String key, String value) {
        values.put(key, value);
        return this;
    }
    public URLParamBuilder addParameter(String key, float value) {
        return addParameter(key, String.valueOf(value));
    }
    public URLParamBuilder addParameter(String key, double value) {
        return addParameter(key, String.valueOf(value));
    }

    public URLParamBuilder addParameter(String key, long value) {
        return addParameter(key, String.valueOf(value));
    }

    public String toString() {
        String result = "?";
        boolean first = true;
        for(Map.Entry<String, String> v: values.entrySet()) {
            if (first)
                first = false;
            else
                result += "&";
            result += v.getKey() + "=" + v.getValue();
        }
        return result;
    }
}
