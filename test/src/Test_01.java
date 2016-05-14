import com.github.lemmingswalker.BlobScanner;
import com.github.lemmingswalker.ContourData;
import com.github.lemmingswalker.ContourDataProcessor;
import com.github.lemmingswalker.ThresholdChecker;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Created by doekewartena on 4/28/16.
 */




/*
BLOB CLOSED
- we always meet the start...
-can we calculate based on boundingbox and length?

 */



// what if we start on a corner?


/*

allow_back_travel

 */


public class Test_01 extends PApplet {

    public static void main(String[] args) {
        PApplet.main("Test_01", args);
    }

    int w = 1024;
    int h = 768;

    PGraphics pg;
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

    int scan_id = 1;//MIN_INT;



    @Override
    public void settings() {
        size(w, h);
        noSmooth();
    }

    @Override
    public void setup() {

        BlobScanner.debug = true;

        randomSeed(1);

        pg = createGraphics(w, h);
        pg.noSmooth();

        dbg_img = createGraphics(w, h);
        dbg_img.beginDraw();
        dbg_img.image(pg, 0, 0);
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
        contour_data.edge_indexes = new int[pg.width* pg.height/2];  // todo, calculate worst case length for blob (which would be a space filling curve)
        contour_data.corner_indexes = new int[pg.width* pg.height/2]; // todo, way to big
        //contour_data.is_corner = new boolean[pg.width*pg.height/2];


        contour_data_processor = contourData -> {
            int color = color(0,255,0);
            scan_results++;
            //  println("contour_data.length: "+contour_data.length);

            if (false) {
                for (int i = 0; i < contour_data.length; i++) {
                    dbg_img.pixels[contour_data.edge_indexes[i]] = color;
                }
            }
            else {
                println(contour_data.n_of_corners);
                for (int i = 0; i < contour_data.n_of_corners; i++) {
                    dbg_img.pixels[contour_data.corner_indexes[i]] = color;
                }
            }
            return true;
        };

        BlobScanner.debug_hitpoints = hitpoints.pixels;


    }


    float noise_scale = 0.001f;
    @Override
    public void draw() {

        if (display_mode == IMG) {
            pg.beginDraw();
            pg.background(0);
            pg.endDraw();
        }
        else if (display_mode == RESULT) {
            dbg_img.beginDraw();
            dbg_img.background(0);
            dbg_img.endDraw();
        }
        else if (display_mode == EDGE_MAP) {
            // don't clear!
        }
        else if (display_mode == HIT_POINTS) {
            hitpoints.beginDraw();
            hitpoints.background(0);
            hitpoints.endDraw();
        }

        int draw_method = 1;

        pg.beginDraw();

        pg.pushStyle();
        if (draw_method == 0) {
            pg.background(0);
            pg.noStroke();
            pg.fill(255);
            noiseSeed(1);
            noise_scale += 0.00001f;
            for (int i = 0; i < 5000; i++) {
                float r = noise(i * noise_scale) * 15;
                pg.ellipse(noise((i + 256) * noise_scale) * pg.width, noise((i + 1024) * noise_scale) * pg.height, r, r);
                //r *= 3;
                //pg.rect(noise((i+256)*noise_scale)*pg.width, noise((i+1024)*noise_scale)*pg.height, r, r);
                //pg.rect(random(pg.width), random(pg.height), r, r);
            }
        }
        else if (draw_method == 1) {
            pg.background(0);
            pg.stroke(255);
            pg.noFill();
//            pg.line(50,50, 50,600);
//            pg.line(50,99, 250,99);
//            //pg.line(50,100, 250,100);
//            pg.line(50,101, 250,101);
            pg.noStroke();
            pg.fill(255);
            pg.rect(50, 100, 100, 100);

        }
        else if (draw_method == 2) {
            pg.background(120);
            pg.colorMode(HSB, 360, 1 ,1);
            pg.ellipseMode(CENTER);
            pg.noStroke();
            for (int i = 0; i < 100; i++) {
                float x = random(pg.width);
                float y = random(pg.height);
                float r = random(10, 30);
                pg.fill(random(360), 1, 1);
                pg.ellipse(x, y, r, r);
            }
        }
        pg.popStyle();


        pg.stroke(0);
        pg.strokeWeight(1);
        pg.noFill();
        pg.rect(0,0,pg.width-1, pg.height-1);
        pg.endDraw();
        pg.loadPixels();





//        if (true) {
//            image(pg, 0, 0);
//            return;
//        }


        background(255);


//        int x1 = 1;
//        int y1 = 1;
//        int x2 = width-2;
//        int y2 = height-2;
        int x1 = 0;
        int y1 = 199;
        int x2 = width;
        int y2 = height;

        int y_inc = 5;
        //int[] contour_id_map = new int[pg.width * pg.height];
        //int scan_id = -65536 + frameCount ;

        pushStyle();
        colorMode(HSB, 360, 100, 1);
        //scan_id = color(-65536 + frameCount);// * 10 % 360, 100, 1);
        scan_id++;
        popStyle();

        //println(frameCount / (360 * 360));

        int start = millis();

        scan_results = 0;

         BlobScanner.scan(
                pg.pixels,
                pg.width,
                pg.height,
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

        //println(scan_id);


        int time = millis()-start;

        imageMode(CENTER);

        if (display_mode == IMG) {
            pg.updatePixels();
            image(pg, width/2, height/2, pg.width * scale, pg.height * scale);
        }
        else if (display_mode == RESULT) {
            dbg_img.updatePixels();
            image(dbg_img, width/2, height/2, pg.width*scale, pg.height*scale);
        }
        else if (display_mode == EDGE_MAP) {
            edge_exist_id_map.updatePixels();
            image(edge_exist_id_map, width/2, height/2, pg.width * scale, pg.height * scale);
        }
        else if (display_mode == HIT_POINTS) {
            hitpoints.updatePixels();
            image(hitpoints, width/2, height/2, pg.width * scale, pg.height * scale);
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
            println("display_mode: "+display_mode);
        }

        //redraw();
    }



}
