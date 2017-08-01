import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Created by doekewartena on 14/07/16.
 */
public class PBlobScannerTest extends PApplet {



    public static void main(String[] args) {
        PApplet.main("PBlobScannerTest", args);
    }


    PBlobScanner blobScanner;

    PGraphics canvas;

    @Override
    public void settings() {
        size(600, 600);
    }

    @Override
    public void setup() {

        blobScanner = new PBlobScanner();
        blobScanner.threshold = 128;
        //blobScanner.set_ROI(0, 0, 0.5f, 0.5f);
        blobScanner.set_ROI(0, 0, 1f, 1f);

        //blobScanner.border_color = color(255,0,0);

        canvas = createGraphics(width, height);
        canvas.beginDraw();
        canvas.background(0);
        canvas.endDraw();

    }

    @Override
    public void draw() {

        if (mousePressed) {
            canvas.beginDraw();
            canvas.noStroke();
            canvas.fill(255);
            canvas.ellipse(mouseX, mouseY, 30, 30);

            canvas.noFill();
            canvas.stroke(0);
            canvas.rect(0, 0, width-1, height-1);

            canvas.endDraw();

            canvas.loadPixels();
        }


        image(canvas, 0, 0);

        blobScanner.y_increment = 10;

        blobScanner.scan(
                canvas,
                contourData -> {


                    beginShape();
                    noFill();
                    stroke(0,255,0);

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
