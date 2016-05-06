package com.github.lemmingswalker;

/**
 * Created by doekewartena on 4/27/16.
 */
public class BlobScanner {

    public static boolean debug = false;
    public static int[] debug_hitpoints;


    public static void scan(int[] pixels,
                           int img_width,
                           int x1,
                           int y1,
                           int x2,
                           int y2,
                           int y_increment,
                           ThresholdChecker threshold_checker,
                           int[] contour_exist_scan_id_map,
                           int scan_id,
                           ContourData contour_data,
                           ContourDataProcessor contour_data_processor) {


        final int UP = -img_width;
        final int DOWN = img_width;
        final int LEFT = -1;
        final int RIGHT = 1;

        // todo check input parameters

        // find an edge
        for (int y = y1; y < y2; y += y_increment) {

            boolean current_val_pass = false; // used to set last_val_pass

            for (int x = x1; x < x2; x++) {

                boolean last_val_pass = current_val_pass;

                int index = y * img_width + x;
                int current_color = pixels[index];
                current_val_pass = threshold_checker.result_of(current_color);

                if (current_val_pass && !last_val_pass) { // true if edge or corner

                    if (debug) debug_hitpoints[index] = -65536;

                    if (contour_exist_scan_id_map[index] == scan_id) {
                        if (debug) debug_hitpoints[index] = -256;
                        continue;
                    }

                    // from here on walk the contour
                    int walker_index = index;

                    // single pixel test
                    int neighbour_count = 0;
                    if (threshold_checker.result_of(pixels[walker_index + LEFT]))  neighbour_count++;
                    if (threshold_checker.result_of(pixels[walker_index + UP]))    neighbour_count++;
                    if (threshold_checker.result_of(pixels[walker_index + RIGHT])) neighbour_count++;
                    if (threshold_checker.result_of(pixels[walker_index + DOWN]))  neighbour_count++;

                    if (neighbour_count == 0) {
                        continue; // single pixel
                    }

                    // we come from the left
                    int move_direction = UP;
                    int check_direction = LEFT;

                    contour_exist_scan_id_map[walker_index] = scan_id;

                    int idx = 0;
                    contour_data.edge_indexes[idx++] = walker_index;
                    contour_data.n_of_corners = 0;

                    int first_move = 0;

                    boolean do_test_against_first_move = false;

                    while (true) {

                        int next_index = -1;
                        boolean previous_was_corner = false;

                        while (next_index == -1) {

                            if (threshold_checker.result_of(pixels[walker_index + check_direction])) {

                                next_index = walker_index + check_direction;

                                if (check_direction == RIGHT) {
                                    move_direction = RIGHT;
                                    check_direction = UP;
                                } else if (check_direction == DOWN) {
                                    move_direction = DOWN;
                                    check_direction = RIGHT;
                                } else if (check_direction == LEFT) {
                                    move_direction = LEFT;
                                    check_direction = DOWN;
                                } else if (check_direction == UP) {
                                    move_direction = UP;
                                    check_direction = LEFT;
                                }

                                previous_was_corner = true;
                            }
                            else if (threshold_checker.result_of(pixels[walker_index + move_direction])) {
                                next_index = walker_index + move_direction;
                            }
                            else {
                                if (move_direction == UP) {
                                    move_direction = RIGHT;
                                    check_direction = UP;
                                } else if (move_direction == RIGHT) {
                                    move_direction = DOWN;
                                    check_direction = RIGHT;
                                } else if (move_direction == DOWN) {
                                    move_direction = LEFT;
                                    check_direction = DOWN;
                                } else if (move_direction == LEFT) {
                                    move_direction = UP;
                                    check_direction = LEFT;
                                }
                                previous_was_corner = true;
                            }
                        }

                        if (previous_was_corner) {
                            contour_data.corner_indexes[contour_data.n_of_corners++] = walker_index;
                        }

                        int the_move = next_index - walker_index;
                        walker_index += the_move;


                        if (first_move == 0) {
                            first_move = the_move;
                        }
                        else if (do_test_against_first_move) {
                            if (the_move == first_move) {
                                break;
                            }
                        }
                        else if (walker_index == index) {
                            // we need the next index to perform the test
                            do_test_against_first_move = true;
                        }
                        // use this if we don't want to allow traveling back on the same pixels
//                        else if (!allow_back_track && contour_exist_scan_id_map[walker_index] == scan_id) {
//                            //System.out.println("back travel");
//                            idx -= 1;
//                            //break;
//                        }

                        contour_data.edge_indexes[idx++] = walker_index;
                        contour_exist_scan_id_map[walker_index] = scan_id;

                    }

                    contour_data.length = idx;

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



}
