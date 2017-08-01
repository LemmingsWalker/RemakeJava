import com.github.lemmingswalker.ThresholdChecker;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.awt.*;

/**
 * Created by doekewartena on 14/07/16.
 */
public class PBlobScannerTestMask extends PApplet {



    public static void main(String[] args) {
        PApplet.main("PBlobScannerTestMask", args);
    }

    PBlobScanner blobScanner;

    PGraphics mask;
    PGraphics canvas;


    @Override
    public void settings() {
        size(512, 512);
    }

    @Override
    public void setup() {

        blobScanner = new PBlobScanner();
        blobScanner.threshold = 128;
        //blobScanner.set_ROI(0, 0, 0.5f, 0.5f);
        blobScanner.set_ROI(0, 0, 1f, 1f);

        //blobScanner.border_color = color(255,0,0);

        mask = createGraphics(width, height);
        mask.beginDraw();
        mask.background(0);
        mask.fill(255);
        //mask.ellipse(width/2, height/2, width/3, height/3);
        mask.textAlign(CENTER, CENTER);
        mask.textSize(200);
        mask.textLeading((mask.textAscent()+mask.textDescent())*0.8f);
        mask.text("MA\nSK", width/2, height/2);
        mask.endDraw();
        mask.loadPixels();

        canvas = createGraphics(width, height);
        canvas.beginDraw();
        canvas.background(0);
        canvas.endDraw();
        canvas.loadPixels();

        blobScanner.thresholdChecker = new ThresholdChecker() {
            @Override
            public boolean result_of(int[] pixels, int index) {

                if (mask.pixels[index] == color(0)) {
                    return false;
                }
                return green(canvas.pixels[index]) > blobScanner.threshold;
            }
        };
    }

    @Override
    public void draw() {

        background(0);

        fill(255);
        ellipse(mouseX, mouseY, 50, 50);

        if (mousePressed) {
            canvas.beginDraw();
            canvas.fill(255);
            canvas.noStroke();
            canvas.ellipse(mouseX, mouseY, 50, 50);
            canvas.loadPixels();
            canvas.endDraw();
        }

        //image(canvas, 0, 0);


        blobScanner.scan(
                canvas,
                contourData -> {

                    beginShape();
                    //noFill();
                    fill(255,0,0, 50);
                    stroke(255,0,0);

                    for (int i = 0; i < contourData.n_of_corners; i++) {
                        int index = contourData.corner_indexes[i];
                        float x = index % width;
                        float y = (index - x) / width;
                        vertex(x, y);
                    }

                    endShape();

                    return true;
                });

    }
}
