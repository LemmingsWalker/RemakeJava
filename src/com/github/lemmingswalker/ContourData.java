package com.github.lemmingswalker;

/**
 * Created by doekewartena on 4/30/16.
 */
public class ContourData {
    public int length;
    public int[] edge_indexes; // name pixel_indexes?
    public boolean[] is_corner;
    public int n_of_corners;
    public int[] corner_indexes;
    public boolean save_edges; // prone for error when people set to false and the edge_exist_id_map doesn't update
    // boolean is_closed?
}
