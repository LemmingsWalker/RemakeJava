import com.github.lemmingswalker.ThresholdChecker;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Created by doekewartena on 14/07/16.
 */
public class PBlobScannerTestRGB extends PApplet {



    public static void main(String[] args) {
        PApplet.main("PBlobScannerTestRGB", args);
    }


    PBlobScanner blobScanner;

    PImage img;

    ThresholdChecker t_r, t_g, t_b, t_rgb;

    @Override
    public void settings() {
        size(900, 900);
    }

    @Override
    public void setup() {

        blobScanner = new PBlobScanner();
        blobScanner.threshold = 128;
        //blobScanner.set_ROI(0, 0, 0.5f, 0.5f);
        blobScanner.set_ROI(0, 0, 1f, 1f);

        //blobScanner.border_color = color(255,0,0);


        img = loadImage("/Users/doekewartena/Desktop/exp_raster_doeke/data/apple3.jpg");
        img.loadPixels();

        // todo, for processing just use red green blue methods
        t_r = new ThresholdChecker() {
            @Override
            public boolean result_of(int[] pixels, int index) {
                return red(pixels[index]) > blobScanner.threshold;
            }
        };

        t_g = new ThresholdChecker() {
            @Override
            public boolean result_of(int[] pixels, int index) {
                return green(pixels[index]) > blobScanner.threshold;
            }
        };

        t_b = new ThresholdChecker() {
            @Override
            public boolean result_of(int[] pixels, int index) {
                return blue(pixels[index]) > blobScanner.threshold;
            }
        };

        t_rgb = new ThresholdChecker() {
            @Override
            public boolean result_of(int[] pixels, int index) {
                return brightness(pixels[index]) > blobScanner.threshold;
            }
        };

        background(0);
    }

    @Override
    public void draw() {

        fill(0, 2);
        blendMode(DARKEST);
        rect(0, 0, width, height);
        blendMode(BLEND);

        //background(0);

        //image(img, 0, 0, img.width, img.height);

        //blobScanner.threshold = (int)map(mouseX, 0, width, 0, 255);
        //blobScanner.threshold = (int) map(sin(frameCount * 0.02f), -1, 1, 0, 255);
        blobScanner.threshold = (int) map(frameCount%512, 0, 512, 0, 255);

        for (int i = 0; i < 4; i++) {

            translate(1, 1);

            ThresholdChecker thresholdChecker;
            int color;
            int alpha = 255 / 4;

            if (i == 0) {
                thresholdChecker = t_r;
                color = color(255,0,0, alpha);
            }
            else if (i == 1) {
                thresholdChecker = t_g;
                color = color(0,255,0, alpha);
            }
            else if (i == 2) {
                thresholdChecker = t_b;
                color = color(0,0,255, alpha);
            }
            else {
                thresholdChecker = t_rgb;
                color = color(255,255,255, alpha);
            }
            blobScanner.thresholdChecker = thresholdChecker;

            blobScanner.y_increment = 10;

            blobScanner.scan(
                    img,
                    contourData -> {


                        beginShape();
                        noFill();
                        stroke(color);

                        for (int j = 0; j < contourData.n_of_corners; j++) {
                            int index = contourData.corner_indexes[j];
                            float x = index % width;
                            float y = (index - x) / width;
                            vertex(x, y);
                        }

                        endShape();

                        return true;
                    });

        }
        surface.setTitle(""+(int)frameRate);
    }


}
