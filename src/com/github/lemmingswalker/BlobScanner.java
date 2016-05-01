package com.github.lemmingswalker;

/**
 * Created by doekewartena on 4/27/16.
 */
public class BlobScanner {

    public static void scan(int[] pixels,
                           int img_width,
                           int img_height,
                           int x1,
                           int y1,
                           int x2,
                           int y2,
                           int y_increment,
                           ThresholdChecker threshold_checker,
                           int border_color,
                           int[] contour_exist_scan_id_map,
                           int scan_id, // find some good code to random this number?
                           ContourData contour_data,
                           ContourDataProcessor contour_data_processor) {


        // check input parameters
        if (x1 == 0          || x1 == -1) x1 = 1;
        if (y1 == 0          || y1 == -1) y1 = 1;
        if (x2 == img_width  || x2 == -1) x2 = img_width-1;
        if (y2 == img_height || y2 == -1) y2 = img_height-1;

        // use assert or not? people never use it
        assert threshold_checker!= null : "threshold_checker is null";
        assert contour_data != null;
        assert contour_data_processor != null;


        // set up initial values
        int start_x = x1;
        int start_y = y1 + y_increment; // misleading or convenient?
        int max_x   = x1 + (x2 - x1);
        int max_y   = y1 + (y2 - y1);


        // find an edge
        //boolean reset_val = threshold_checker.result_of(border_color);
        boolean reset_val = false;

        boolean last_val_pass = reset_val; // name free?


        for (int y = start_y; y < max_y; y += y_increment) {



            for (int x = start_x; x < max_x; x++) {

                int index = y * img_width + x;
                if (contour_exist_scan_id_map[index] == scan_id) continue;

                int current_color = pixels[index];
                boolean current_val_pass = threshold_checker.result_of(current_color);

                if (current_val_pass && !last_val_pass) { // edge or corner

                    System.out.println("index: "+index);

                    // walk the contour
                    walk_contour(pixels,
                                img_width,
                                index,
                                threshold_checker,
                                contour_data,
                                contour_exist_scan_id_map,
                                scan_id);

                    if (contour_data.length > 0) {

                        // process the result
                        boolean should_continue = contour_data_processor.process(contour_data);
                        if (!should_continue) {
                            return;
                        }

//                        // update contour exist map: if we didn't return!
//                        for (int i = 0; i < contour_data.length; i++) {
//                            contour_exist_scan_id_map[contour_data.edge_indexes[i]] = scan_id;
//                        }
                    }

                }
                last_val_pass = current_val_pass;
            }
            last_val_pass = reset_val;
        }
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

    public static void walk_contour(int[] pixels,
                             int img_width,
                             int start_index,
                             ThresholdChecker threshold_checker,
                             ContourData contour_data,
                             int[] contour_exist_scan_id_map,
                             int scan_id) {


        final int UP = -img_width;
        final int DOWN = img_width;
        final int LEFT = -1;
        final int RIGHT = 1;

        int walker_index = start_index;


        // we come from the left now, so we could set the move direction to the left and it will find it's right way?
        // might duplicate start a few times?
        int move_direction = UP;
        int check_direction = LEFT;

        contour_exist_scan_id_map[walker_index] = scan_id;

        int idx = 0;
        contour_data.edge_indexes[idx++] = walker_index;

        int save_count = 0;


        while (true) {

            //if (save_count++ > 1000) break;


            //boolean check_dir_free = threshold_checker.result_of(pixels[walker_index + check_direction]);

            // check dir free
            if (threshold_checker.result_of(pixels[walker_index + check_direction])) {
                walker_index += check_direction;
                if (walker_index == start_index) break;

                contour_data.edge_indexes[idx++] = walker_index;
                contour_exist_scan_id_map[walker_index] = scan_id;

                // update move and check direction
                if (check_direction == RIGHT) {
                    move_direction = RIGHT;
                    check_direction = UP;
                }
                else if (check_direction == DOWN) {
                    move_direction = DOWN;
                    check_direction = RIGHT;
                }
                else if (check_direction == LEFT) {
                    move_direction = LEFT;
                    check_direction = DOWN;
                }
                else if (check_direction == UP) {
                    move_direction = UP;
                    check_direction = LEFT;
                }


            }
            // move dir free
            else if (threshold_checker.result_of(pixels[walker_index + move_direction])) {

                walker_index += move_direction;
                if (walker_index == start_index) break;

                contour_data.edge_indexes[idx++] = walker_index;
                contour_exist_scan_id_map[walker_index] = scan_id;


            }
            else {
                // we hit a wall, so turn right
                if (move_direction == UP) {
                    move_direction = RIGHT;
                    check_direction = UP;
                }
                else if (move_direction == RIGHT) {
                    move_direction = DOWN;
                    check_direction = RIGHT;
                }
                else if (move_direction == DOWN) {
                    move_direction = LEFT;
                    check_direction = DOWN;
                }
                else if (move_direction == LEFT) {
                    move_direction = UP;
                    check_direction = LEFT;
                }
            }
        }


        contour_data.length = idx;

        // tmp
//        contour_data.length = 100;
//        for (int i = 0; i < contour_data.length; i++) {
//            contour_data.edge_indexes[i] = start_index+i;
//        }


    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


}
