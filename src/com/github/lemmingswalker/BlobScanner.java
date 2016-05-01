package com.github.lemmingswalker;

/**
 * Created by doekewartena on 4/27/16.
 */
public class BlobScanner {

    public static void scan(int[] pixels,
                           int img_width,
                           int x1,
                           int y1,
                           int x2,
                           int y2,
                           int y_increment,
                           ThresholdChecker threshold_checker,
                           int[] contour_exist_scan_id_map,
                           int scan_id, // find some good code to random this number?
                           ContourData contour_data,
                           ContourDataProcessor contour_data_processor,
                           int[] debug_hitpoints) {


        // todo check input parameters




        // find an edge
        for (int y = y1; y < y2; y += y_increment) {

            boolean current_val_pass = false; // used to set last_val_pass

            for (int x = x1; x < x2; x++) {

                boolean last_val_pass = current_val_pass;

                int index = y * img_width + x;
                int current_color = pixels[index];
                current_val_pass = threshold_checker.result_of(current_color);

                if (current_val_pass && !last_val_pass) { // edge or corner

                    if (contour_exist_scan_id_map[index] == scan_id) {
                        continue;
                    }


                    debug_hitpoints[index] = -256;
                    contour_exist_scan_id_map[index] = -256;
                    //System.out.println("index: "+index);

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
                    }
                }
            }
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

        if (contour_exist_scan_id_map != null) contour_exist_scan_id_map[walker_index] = scan_id;

        int idx = 0;
        contour_data.edge_indexes[idx++] = walker_index;

        while (true) {

            //boolean check_dir_free = threshold_checker.result_of(pixels[walker_index + check_direction]);

            // check dir free
            if (threshold_checker.result_of(pixels[walker_index + check_direction])) {
                walker_index += check_direction;
                if (walker_index == start_index) break;

                contour_data.edge_indexes[idx++] = walker_index;
                if (contour_exist_scan_id_map != null) contour_exist_scan_id_map[walker_index] = scan_id;

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
                if (contour_exist_scan_id_map != null) contour_exist_scan_id_map[walker_index] = scan_id;


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
