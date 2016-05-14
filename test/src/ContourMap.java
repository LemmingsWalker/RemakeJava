import com.github.lemmingswalker.BlobScanner;
import com.github.lemmingswalker.ContourData;
import com.github.lemmingswalker.ContourDataProcessor;
import com.github.lemmingswalker.ThresholdChecker;
import processing.core.PApplet;
import peasy.*;
import processing.core.PGraphics;

/**
 * Created by doekewartena on 5/5/16.
 */
public class ContourMap extends PApplet {

    public static void main(String[] args) {
        PApplet.main("ContourMap", args);
    }

    PeasyCam cam;
    PGraphics noise = new PGraphics();

    ThresholdChecker threshold_checker;
    int threshold;

    ContourData cd;
    ContourDataProcessor cdp;

    int[] contour_exist_map;

    int scan_id = 1;


    @Override
    public void settings() {
        size(1024, 768, P3D);
    }

    @Override
    public void setup() {

        cam = new PeasyCam(this, 200);
        noise = createGraphics(256, 256);

        threshold_checker = new ThresholdChecker() {
            @Override
            public boolean result_of(int color) {
                return ((color >> 8) & 0xFF) > threshold; // checking green
            }
        };

        cd = new ContourData();
        cd.corner_indexes = new int[noise.width * noise.height];
        cd.edge_indexes   = new int[noise.width * noise.height];

        cdp = new ContourDataProcessor() {
            @Override
            public boolean process(ContourData contour_data) {
                beginShape();
                noFill();
                for (int i = 0; i < contour_data.n_of_corners; i++) {
                    int index = contour_data.corner_indexes[i];
                    float x = index % noise.width;
                    float y = (index - x) / noise.width;
                    vertex(x, y);
                    //curveVertex(x, y);
                }
                endShape();
            return true;
            }
        };

        contour_exist_map = new int[noise.width * noise.height];


    }


    @Override
    public void draw() {

        float noise_scale = 0.1f;
        float shift = frameCount*0.05f;
        float scale_2d = sin(frameCount*0.01f); // % TWO_PI) + 1;
        scale_2d = map(scale_2d, -1, 1, 0.01f, 0.4f);


        noise.beginDraw();

        for (int y = 0; y < noise.height; y++) {
            for (int x = 0; x < noise.width; x++) {
                int _x = x - noise.width/2;
                int _y = y - noise.height/2;
                _x += 1000;
                _y += 1000;
                float n = noise(_x*noise_scale*scale_2d, _y*noise_scale*scale_2d, shift);
                noise.set(x, y, color(n*256));
            }
        }
        noise.fill(0);
        noise.textSize(40);
        noise.textAlign(LEFT, TOP);
        noise.text("LEMMINGS\nWALKER", 10, 10);
        noise.noFill();
        noise.stroke(0);
        noise.strokeWeight(1);
        noise.line(0, 0, noise.width, noise.height);

        noise.stroke(0);
        noise.strokeWeight(1);
        noise.rect(0,0,noise.width-1,noise.height-1);

        noise.endDraw();

        noise.loadPixels();

        background(0);

        translate(-noise.width/2, -noise.height/2);

        pushStyle();
        colorMode(HSB, 256, 256, 256);

        // wtf?  cause there is no pure white?
        // too check
        //for (int i = 0; i <= 257; i ++) {
        for (int i = 1; i < 255; i ++) {
            translate(0, 0, 0.25f);
            threshold = i;


            stroke(i, i, i);
            strokeWeight(1);

            BlobScanner.scan(
                    noise.pixels,
                    noise.width,
                    noise.height,
                    0, 0, noise.width, noise.height,
                    5,
                    threshold_checker,
                    contour_exist_map,
                    scan_id++,
                    cd,
                    cdp);

        }
        popStyle();


        image(noise, 256, 0);

        surface.setTitle("fps: "+frameRate);

    }
}
