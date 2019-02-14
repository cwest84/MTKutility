package com.adtdev.ProgressCircle;

import android.support.annotation.NonNull;

/**
 * Created by Anton on 06.06.2018.
 */

public final class PatternProgressTextAdapter implements ProgressCircle.ProgressTextAdapter {

    private String pattern;

    public PatternProgressTextAdapter(String pattern) {
        this.pattern = pattern;
    }

    @NonNull
    @Override
    public String formatText(double currentProgress) {
        return String.format(pattern, currentProgress);
    }
}