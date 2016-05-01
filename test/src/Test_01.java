import com.github.lemmingswalker.BlobScanner;
import com.github.lemmingswalker.ContourData;
import com.github.lemmingswalker.ContourDataProcessor;
import com.github.lemmingswalker.ThresholdChecker;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Created by doekewartena on 4/28/16.
 */
public class Test_01 extends PApplet {

    public static void main(String[] args) {
        PApplet.main("Test_01", args);
    }


    PGraphics img;
    PGraphics dbg_img;
    PGraphics edge_exist_id_map;


    int scan_results = 0;

    int IMG = 0;
    int RESULT = 1;
    int EDGE_MAP = 2;

    int display_mode = RESULT;

    int scale = 5;



    @Override
    public void settings() {
        size(640, 480);
        noSmooth();
    }

    @Override
    public void setup() {

        randomSeed(1);

        img = createGraphics(640, 480);

        img.noSmooth();

        img.beginDraw();
        img.background(0);
        img.noStroke();
        img.fill(255);
        for (int i=0;i<20;i++) {
            float r = random(50);
            img.ellipse(random(img.width), random(img.height), r, r);
        }
        img.noFill();
        img.stroke(0);
        img.rect(0,0,img.width-1, img.height-1);
        img.endDraw();
        img.loadPixels();

        dbg_img = createGraphics(640, 480);
        dbg_img.beginDraw();
        dbg_img.image(img, 0, 0);
        dbg_img.endDraw();

        edge_exist_id_map = createGraphics(640, 480);
        edge_exist_id_map.beginDraw();
        edge_exist_id_map.background(0);
        edge_exist_id_map.endDraw();
        edge_exist_id_map.loadPixels();


//        int x1 = 1;
//        int y1 = 1;
//        int x2 = width-2;
//        int y2 = height-2;
        int x1 = 0;
        int y1 = 0;
        int x2 = width;
        int y2 = height;

        int y_inc = 5;
        //int[] contour_id_map = new int[img.width * img.height];
        int scan_id = color(255,0,0);


        ThresholdChecker thresholdChecker = color -> {
            return ((color >> 8) & 0xFF) > 128; // checking green with a threshold of 128
        };


        ContourData contour_data = new ContourData();
        contour_data.edge_indexes = new int[img.width*img.height/2];  // todo, calculate worst case length for blob (which would be a space filling curve)
        contour_data.is_corner = new boolean[img.width*img.height/2];


        ContourDataProcessor contour_data_processor = contourData -> {

            beginShape();
            for (int i = 0; i < contour_data.length; i++) {
                if (contour_data.is_corner[i]) {
                    int index = contour_data.edge_indexes[i];
                    int x = index % img.width;
                    int y = (index - x) / img.width;
                    vertex(x, y);
                }
            }
            noFill();
            stroke(255,0,0);
            endShape();
            return false; // for now stop after first one
        };




        ContourDataProcessor contour_data_processor_2 = contourData -> {
            int color = color(0,255,0);
            scan_results++;
            println("contour_data.length: "+contour_data.length);
            for (int i = 0; i < contour_data.length; i++) {
                dbg_img.pixels[contour_data.edge_indexes[i]] = color;
            }
            return true;
        };


        BlobScanner.scan(
                img.pixels,
                img.width,
                img.height,
                x1,
                y1,
                x2,
                y2,
                y_inc,
                thresholdChecker,
                edge_exist_id_map.pixels,
                scan_id,
                contour_data,
                contour_data_processor_2);



        surface.setTitle("scan_results: "+scan_results);

    }

    @Override
    public void draw() {
        background(0);


        if (display_mode == IMG) {
            image(img, 0, 0, img.width * scale, img.height * scale);
        }
        else if (display_mode == RESULT) {
            image(dbg_img, 0, 0, img.width*scale, img.height*scale);
        }
        else if (display_mode == EDGE_MAP) {
            image(edge_exist_id_map, 0, 0, img.width * scale, img.height * scale);
        }


        noLoop();
    }

    public void keyPressed() {
        if (key == '1') scale = 1;
        else if (key == '2') scale = 2;
        else if (key == '3') scale = 3;
        else if (key == '4') scale = 4;
        else if (key == '5') scale = 5;
        else if (key == '6') scale = 6;
        else if (key == '7') scale = 7;
        else if (key == '8') scale = 8;
        else if (key == '9') scale = 9;
        else if (key == '0') scale = 10;
        else {
            display_mode++;
            if (display_mode == 3) display_mode = 0;
        }

        redraw();
    }



}
