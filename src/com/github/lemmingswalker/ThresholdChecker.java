package com.github.lemmingswalker;

/**
 * Created by doekewartena on 4/28/16.
 */


public interface ThresholdChecker {
    /**
     *
     * @param index
     * @return ... if color is above threshold
     */
    boolean result_of(int[] pixels, int index);
}
