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


    int scan_results = 0;


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
        img.stroke(255,0,0);
        img.rect(0,0,img.width-1, img.height-1);
        img.endDraw();

        img.loadPixels();

        dbg_img = createGraphics(640, 480);
        dbg_img.beginDraw();
        dbg_img.image(img, 0, 0);
        dbg_img.endDraw();


        int x1 = 1;
        int y1 = 1;
        int x2 = width-2;
        int y2 = height-2;
        int y_inc = 10;
        int border_color = color(255,0,0);
        int[] contour_id_map = new int[img.width * img.height];
        int scan_id = 1;


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
            boolean bugged = contourData.length == 4;

            int color = bugged ? color(0,0,255) : color(0,255,0);


            scan_results++;

            println("contour_data.length: "+contour_data.length);
            for (int i = 0; i < contour_data.length; i++) {
                dbg_img.pixels[contour_data.edge_indexes[i]] = color;
            }
            if (bugged) {
                dbg_img.pixels[contour_data.edge_indexes[0]] = color(255,255,0);

            }
            if (scan_results < 50) return true;
            return false; // for now stop after the first one
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
                border_color,
                contour_id_map,
                scan_id,
                contour_data,
                contour_data_processor_2);



    }

    @Override
    public void draw() {
        int scale = 16;
        image(dbg_img, 0, 0, img.width*scale, img.height*scale);
        noLoop();
    }




}
