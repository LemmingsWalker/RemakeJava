package com.github.lemmingswalker;

/**
 * Created by doekewartena on 4/30/16.
 */
public interface ContourDataProcessor {
    /**
     *
     * @param contourData
     * @return return true to continue scanning
     */
    boolean process(ContourData contourData);
}
