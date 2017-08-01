package com.github.lemmingswalker;

import com.sun.istack.internal.NotNull;

/**
 * Created by doekewartena on 4/27/16.
 */
public class BlobScanner {

    /**
    Returns the amount of contours it processed. Usually update scan_id with this amount.
    */
    // remove int[] pixels so we can operate on other types too?
    // what to do with single pixel blobs?
    // right now if we want them this blobscanner is useless right?
    final static int ROI_OUT_OF_BOUNDS = -1;

    // todo, something like this to pass as a parameter?
    // Properties? what is a better name then data?
    class BlobScanData {
        int[] pixels;
        int img_width;
        int img_height;
        int x1;
        int y1;
        int x2;
        int y2;
        int y_increment;
        ThresholdChecker threshold_checker;
        int[] scan_id_map;
        int scan_id;
        ContourData contour_data;
        ContourDataProcessor contour_data_processor;
    }

    class WalkContourData {
        int[] pixels;
        int img_width;
        int start_index;
        ThresholdChecker threshold_checker;
        int[] scan_id_map;
        int scan_id;
        ContourData contour_data;
    }

    public static int scan(int[] pixels,
                            int img_width,
                            int img_height,
                            int x1,
                            int y1,
                            int x2,
                            int y2,
                            int y_increment,
                   @NotNull ThresholdChecker threshold_checker,
                   @NotNull int[] scan_id_map,
                            int scan_id,
                   @NotNull ContourData contour_data,
                   @NotNull ContourDataProcessor contour_data_processor)
    {


        // x1 = min(x1, x2) or do that on the calling site?
        // check input parameters
        // make x1 etc also normalized here?
        if (x1 < 0 || y1 < 0 || x2 > img_width || y2 > img_height)
        {
            // we could also fix values instead
            // System.out.println("Error: BlobScanner.scan(): region of interest is outside image bounds!");
            return ROI_OUT_OF_BOUNDS;
        }

        final int start_scan_id = scan_id;

        // find an edge
        outer_loop:
        for (int y = y1; y < y2; y += y_increment)
        {

            boolean current_val_pass = false; // used to set last_val_pass

            for (int x = x1; x < x2; x++)
            {

                boolean last_val_pass = current_val_pass;
                int index = y * img_width + x;
                current_val_pass = threshold_checker.result_of(pixels, index);

                if (current_val_pass && !last_val_pass)
                { // true if edge or corner

                    if (scan_id_map[index] >= start_scan_id)
                    {
                        continue;
                    }

                    boolean ok = walk_contour(
                            pixels,
                            img_width,
                            index,
                            threshold_checker,
                            scan_id_map,
                            scan_id,
                            contour_data);

                    if (ok) scan_id++;

                    boolean should_continue = contour_data_processor.process(contour_data);

                    if (!should_continue)
                    {
                        break outer_loop;
                    }

                }
            }
        }
        int n_of_contours_found = scan_id - start_scan_id;
        return n_of_contours_found;
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

    // use returning int over boolean as it is better to update in feature without breaking?
    // maybe a code for single pixel?
    public static boolean walk_contour(int[] pixels,
                           int img_width,
                           int start_index,
                           @NotNull ThresholdChecker threshold_checker,
                           @NotNull int[] scan_id_map,
                           int scan_id,
                           @NotNull ContourData contour_data)
    {

        boolean succes;

        final int UNDEFINED = 0;

        final int UP = -img_width;
        final int DOWN = img_width;
        final int LEFT = -1;
        final int RIGHT = 1;

        // single pixel test
        int neighbour_count = 0;
        if (threshold_checker.result_of(pixels, start_index + LEFT))  neighbour_count++;
        if (threshold_checker.result_of(pixels, start_index + UP))    neighbour_count++;
        if (threshold_checker.result_of(pixels, start_index + RIGHT)) neighbour_count++;
        if (threshold_checker.result_of(pixels, start_index + DOWN))  neighbour_count++;

        if (neighbour_count != 0)
        {
            int walker_index = start_index;
            int move_direction = UP;
            int check_direction = LEFT;

            scan_id_map[walker_index] = scan_id;

            contour_data.n_of_indexes = 0;
            contour_data.contour_indexes[contour_data.n_of_indexes++] = walker_index;
            contour_data.n_of_corners = 0;

            boolean start_is_on_corner = !threshold_checker.result_of(pixels, walker_index + move_direction);

            int the_first_move = UNDEFINED;

            boolean test_first_move_against_the_move = false;

            // from here on walk the contour
            while (true)
            {

                int the_move = UNDEFINED;
                boolean previous_was_corner = false;

                while (the_move == UNDEFINED)
                {

                    //todo, handle the_first_move inside this scope?
                    if (threshold_checker.result_of(pixels, walker_index + check_direction))
                    {

                        the_move = check_direction;

                        if (check_direction == RIGHT)
                        {
                            move_direction = RIGHT;
                            check_direction = UP;
                        } else if (check_direction == DOWN)
                        {
                            move_direction = DOWN;
                            check_direction = RIGHT;
                        } else if (check_direction == LEFT)
                        {
                            move_direction = LEFT;
                            check_direction = DOWN;
                        } else if (check_direction == UP)
                        {
                            move_direction = UP;
                            check_direction = LEFT;
                        }

                        previous_was_corner = true;
                    }
                    else if (threshold_checker.result_of(pixels, walker_index + move_direction)) {
                        the_move = move_direction;
                    }
                    else {
                        if (move_direction == UP)
                        {
                            move_direction = RIGHT;
                            check_direction = UP;
                        } else if (move_direction == RIGHT)
                        {
                            move_direction = DOWN;
                            check_direction = RIGHT;
                        } else if (move_direction == DOWN)
                        {
                            move_direction = LEFT;
                            check_direction = DOWN;
                        } else if (move_direction == LEFT)
                        {
                            move_direction = UP;
                            check_direction = LEFT;
                        }
                        previous_was_corner = true;
                    }
                }

                if (previous_was_corner)
                {
                    contour_data.corner_indexes[contour_data.n_of_corners++] = walker_index;
                }

                walker_index += the_move;

                // todo? final static int NO_MOVE = 0; // UNMOVED // UNDEFINED <<<<<<
                if (the_first_move == UNDEFINED)
                {
                    the_first_move = the_move;
                }
                else if (test_first_move_against_the_move)
                {
                    if (the_first_move == the_move)
                    {
                        if (!start_is_on_corner)
                        {
                            // what?
                            contour_data.corner_indexes[contour_data.n_of_corners++] = contour_data.corner_indexes[0];
                        }
                        break;
                    }
                    test_first_move_against_the_move = false;
                }
                else if (walker_index == start_index)
                {
                    // we need to test it next iteration
                    test_first_move_against_the_move = true;
                }

                contour_data.contour_indexes[contour_data.n_of_indexes++] = walker_index;
                scan_id_map[walker_index] = scan_id;

            }

            succes = true; // success

        }
        else {
            succes = false; // single pixel // todo (also, if return_single_pixel?) better to return then the option to keep thins simplified?
        }

        return succes;

    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

}


/*

close before end of walk contour:
 */
// use this if we don't want to allow traveling back on the same pixels
// it could be correct atm but horrible inefficient?

// 2.
// If backtravel is not allowed,
// could we store in an efficient way where to continue?
// (walking back over what we have with a certain check direction?)
//                        else if (!allow_back_travel && scan_id_map[walker_index] == scan_id) {
//                            //System.out.println("back travel");
//                            contour_data.n_of_indexes--;
//                            // todo
//                            // what about the corners?
//                            // do an assert here to figure that out?
//                            boolean should_continue = contour_data_processor.process(contour_data);
//                            scan_id++;
//                            if (!should_continue) {
//                                break outer_loop;
//                            }
//                            break contour_loop;
//                        }

// 3
// instead, we could also only add the indexes if they are not on the id_map
// this way it's a simple check
// that would be this in the required places:
// if (prevent_overlap && scan_id_map[walker_index] != scan_id) {
// branch prediction should kick in pretty good here


/*
It should still be possible to skip values in x as well?
However, from a skip we have to search to the right if it is an edge

 for (int y = y1; y < y2; y += y_increment) {

            boolean current_val_pass = false; // used to set last_val_pass

            for (int x = x1; x < x2; x += x_increment) {

                boolean last_val_pass = current_val_pass;

                int index = y * img_width + x;
                int current_color = pixels[index];
                current_val_pass = threshold_checker.result_of(current_color);

                if (current_val_pass && !last_val_pass) { // true if edge or corner

maybe a max forward check?


 */