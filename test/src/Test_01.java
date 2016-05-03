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

    int w = 1024;
    int h = 768;

    PGraphics img;
    PGraphics dbg_img;
    PGraphics edge_exist_id_map;
    PGraphics hitpoints;


    int scan_results = 0;

    int IMG = 0;
    int RESULT = 1;
    int EDGE_MAP = 2;
    int HIT_POINTS = 3;

    int display_mode = RESULT;

    int scale = 1;


    ThresholdChecker thresholdChecker;
    ContourData contour_data;
    ContourDataProcessor contour_data_processor;


    @Override
    public void settings() {
        size(w, h);
        noSmooth();
    }

    @Override
    public void setup() {

        randomSeed(1);

        img = createGraphics(w, h);
        img.noSmooth();

        dbg_img = createGraphics(w, h);
        dbg_img.beginDraw();
        dbg_img.image(img, 0, 0);
        dbg_img.endDraw();

        edge_exist_id_map = createGraphics(w, h);
        edge_exist_id_map.beginDraw();
        edge_exist_id_map.background(0);
        edge_exist_id_map.endDraw();
        edge_exist_id_map.loadPixels();

        hitpoints = createGraphics(w, h);
        hitpoints.beginDraw();
        hitpoints.background(0);
        hitpoints.endDraw();
        hitpoints.loadPixels();





        thresholdChecker = color -> {
            return ((color >> 8) & 0xFF) > 128; // checking green with a threshold of 128
        };


        contour_data = new ContourData();
        contour_data.edge_indexes = new int[img.width*img.height/2];  // todo, calculate worst case length for blob (which would be a space filling curve)
        contour_data.is_corner = new boolean[img.width*img.height/2];


        contour_data_processor = contourData -> {
            int color = color(0,255,0);
            scan_results++;
            //  println("contour_data.length: "+contour_data.length);
            for (int i = 0; i < contour_data.length; i++) {
                dbg_img.pixels[contour_data.edge_indexes[i]] = color;
            }
            return true;
        };

        BlobScanner.debug_hitpoints = hitpoints.pixels;


    }


    float noise_scale = 0.001f;
    @Override
    public void draw() {

        if (display_mode == IMG) {
            img.beginDraw();
            img.background(0);
            img.endDraw();
        }
        else if (display_mode == RESULT) {
            dbg_img.beginDraw();
            dbg_img.background(0);
            dbg_img.endDraw();
        }
        else if (display_mode == EDGE_MAP) {
//            edge_exist_id_map.beginDraw();
//            edge_exist_id_map.background(0);
//            edge_exist_id_map.endDraw();
        }
        else if (display_mode == HIT_POINTS) {
            hitpoints.beginDraw();
            hitpoints.background(0);
            hitpoints.endDraw();
        }



        if (true) {

            //println("!");
            img.beginDraw();
            img.background(0);
            if (false) {
                img.noStroke();
                img.fill(255);
                noiseSeed(1);
                noise_scale += 0.00001f;
                for (int i=0;i<5000;i++) {
                    float r = noise(i*noise_scale) * 15;
                    img.ellipse(noise((i+256)*noise_scale)*img.width, noise((i+1024)*noise_scale)*img.height, r, r);
                    //img.rect(random(img.width), random(img.height), r, r);
                }
            }
            if (true) {
                img.stroke(255);
                img.noFill();
                img.line(50,50, 50,600);
                img.line(50,100, 250,100);

            }
            img.noFill();
            img.stroke(0);
            img.rect(0,0,img.width-1, img.height-1);
            img.endDraw();
            img.loadPixels();
        }

        background(255);


//        int x1 = 1;
//        int y1 = 1;
//        int x2 = width-2;
//        int y2 = height-2;
        int x1 = 0;
        int y1 = 100;
        int x2 = width;
        int y2 = height;

        int y_inc = 5;
        //int[] contour_id_map = new int[img.width * img.height];
        int scan_id = -65536 + frameCount ;

        pushStyle();
        colorMode(HSB, 360, 100, 1);
        scan_id = color(frameCount * 10 % 360, 100, 1);
        popStyle();

        //println(frameCount / (360 * 360));

        int start = millis();

        scan_results = 0;

        BlobScanner.scan(
                img.pixels,
                img.width,
                x1,
                y1,
                x2,
                y2,
                y_inc,
                thresholdChecker,
                edge_exist_id_map.pixels,
                scan_id,
                contour_data,
                contour_data_processor);


        int time = millis()-start;


        if (display_mode == IMG) {
            img.updatePixels();
            image(img, 0, 0, img.width * scale, img.height * scale);
        }
        else if (display_mode == RESULT) {
            dbg_img.updatePixels();
            image(dbg_img, 0, 0, img.width*scale, img.height*scale);
        }
        else if (display_mode == EDGE_MAP) {
            edge_exist_id_map.updatePixels();
            image(edge_exist_id_map, 0, 0, img.width * scale, img.height * scale);
        }
        else if (display_mode == HIT_POINTS) {
            hitpoints.updatePixels();
            image(hitpoints, 0, 0, img.width * scale, img.height * scale);
        }

        //noLoop();
        surface.setTitle("scan_results: "+scan_results+" time(ms): "+time);

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
            if (display_mode == 4) display_mode = 0;



        }

        //redraw();
    }



}
