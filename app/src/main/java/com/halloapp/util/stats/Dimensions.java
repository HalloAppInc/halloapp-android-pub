package com.halloapp.util.stats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.BuildConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Dimensions {
    private final Map<String, String> map;

    public Dimensions(Map<String, String> map) {
        this.map = new HashMap<>(map);

        addUniversalDimensions();
    }

    private void addUniversalDimensions() {
        this.map.put("version", BuildConfig.VERSION_NAME);
    }

    @NonNull
    public Set<String> getKeys() {
        return map.keySet();
    }

    @Nullable
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Dimensions dim = (Dimensions) o;

        Set<String> myKeys = getKeys();
        Set<String> theirKeys = dim.getKeys();
        if (myKeys.size() != theirKeys.size()) {
            return false;
        }
        for (String key : myKeys) {
            if (!theirKeys.contains(key)) {
                return false;
            }
            String myVal = get(key);
            String theirVal = dim.get(key);
            if (!myVal.equals(theirVal)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        List<String> keys = new ArrayList(getKeys());
        Collections.sort(keys);
        for (String key : keys) {
            result = prime * prime * result + prime * key.hashCode() + get(key).hashCode();
        }
        return result;
    }

    public static class Builder {

        private Map<String, String> map = new HashMap<>();

        public Builder put(String key, String value) {
            map.put(key, value);
            return this;
        }

        public Dimensions build() {
            return new Dimensions(map);
        }
    }
}
