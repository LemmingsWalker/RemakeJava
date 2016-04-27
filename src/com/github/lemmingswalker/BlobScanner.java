package com.github.lemmingswalker;

/**
 * Created by doekewartena on 4/27/16.
 */

/*

// add only the indexes that make up the contour?


result = index
-1 = end
-2 is end of edge?



scan only 1 blob, return pixel index of where we ended
 */


/*

    // usage:


    f


 */

public class BlobScanner {



    // not here, so we can have the method static?
    final static int X = 0;
    final static int Y = 1;
    final static int WIDTH = 2;
    final static int HEIGHT = 3;


    interface ThresholdChecker {
        boolean result_of(int color);
    }


    // remove roi? user can add a border with border_color, then provide a start_index inside the wanted roi

    public int scan(int[] pixels,
                     int img_width,
                     int img_height,
                     int[] roi,
                     boolean scan_over_x,
                     int jump_increment,
                     ThresholdChecker threshold_checker,
                     int border_color,
                     int[] contour_exist_scan_id_map,
                     int scan_id,
                     int[] result) {

        // check parameters

        //assert result!= null; // todo test

        if (roi != null) {
            // todo check roi bounds
        } else {
            roi = new int[]{0, 0, img_width, img_height};
        }

        // todo check contour_exist_scan_id_map



        // set up initial values

        int result_index = 0;

        final int UP = -img_width;
        final int DOWN = img_width;
        final int LEFT = -1;
        final int RIGHT = 1;

        int x_increment;
        int y_increment;
        int start_x;
        int start_y;
        int max_x;
        int max_y;

        if (scan_over_x) {
            x_increment = 1;
            y_increment = jump_increment;
            start_x = roi[0];
            start_y = roi[1] + jump_increment;
        } else {
            x_increment = jump_increment;
            y_increment = 1;
            start_x = roi[0] + jump_increment;
            start_y = roi[1];
        }

        max_x = roi[0] + roi[2];
        max_y = roi[1] + roi[3];

        // start scanning

        boolean last_val_pass = threshold_checker.result_of(border_color);  // name free?

        for (int x = start_x; x < max_x; x += x_increment) {
            for (int y = start_y; y < max_y; y += y_increment) {

                int index = y * img_width + x;
                int current_color = pixels[index];

                boolean current_val_pass = threshold_checker.result_of(current_color);

                // if true we hit an edge or corner
                if (current_val_pass && !last_val_pass) {

                    if (contour_exist_scan_id_map[index] == scan_id) continue;

                    // todo still use a blob_creator?

                    int walker_index = index;
                    contour_exist_scan_id_map[walker_index] = scan_id;

                    //int px = walker_index % img_width;
                    //int py = (walker_index - x) / img_width;

                    boolean down_is_free = threshold_checker.result_of(pixels[index + DOWN]);
                    boolean up_is_free = threshold_checker.result_of(pixels[index + UP]);
                    boolean left_is_free = threshold_checker.result_of(pixels[index + LEFT]);
                    boolean right_is_free = threshold_checker.result_of(pixels[index + RIGHT]);

                    int move_direction;
                    int check_direction;

                    if (down_is_free && !right_is_free) { // DOWN free
                        move_direction = DOWN;
                        check_direction = RIGHT;
                    }
                    else if (right_is_free && !up_is_free) { // RIGHT free
                        move_direction = RIGHT;
                        check_direction = UP;
                    }
                    else if (up_is_free && !left_is_free ) { // UP free
                        move_direction = UP;
                        check_direction = LEFT;
                    }

                    else if (left_is_free && !down_is_free) { // LEFT free
                        move_direction = LEFT;
                        check_direction = DOWN;
                    }
                    else {
                        // isolated pixel
                        // is this a problem?
                        // it might if we use a blobCreator again !important
                        continue;
                    }

                    // todo c++ etc.



                    while (true) {

                        boolean move_dir_free = threshold_checker.result_of(pixels[index + move_direction]);
                        if (move_dir_free) {
                             // add edge
                            // ...
                            contour_exist_scan_id_map[walker_index] = scan_id;

                            walker_index += move_direction;
                            //px = walker_index % img_width;
                            //py = (walker_index - x) / img_width;

                            if (walker_index == index) break; // where back at the start

                            boolean check_dir_free = threshold_checker.result_of(pixels[walker_index + check_direction]);
                            if (check_dir_free) {

                                // add px, py

                                walker_index += check_direction;

                                if (walker_index == index) break; // where back at the start







                            }








                        }


                    }







                }

                last_val_pass = current_val_pass;
            }
        }


        return -1;
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

    public void walk_contour(int[] pixels,
                             int img_width,
                             int img_height,
                             int[] roi,
                             boolean scan_over_x,
                             int jump_increment,
                             ThresholdChecker threshold_checker,
                             int border_color,
                             int[] contour_exist_scan_id_map,
                             int scan_id,
                             int[] result) {



    }



    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .





}
