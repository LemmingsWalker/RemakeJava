import com.github.lemmingswalker.BlobScanner;
import com.github.lemmingswalker.ContourData;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Created by doekewartena on 5/6/16.
 */
public class DrawTest_inv extends PApplet {


    public static void main(String[] args) {
        PApplet.main("DrawTest_inv", args);
    }

    int[] contour_map;
    ContourData cd;
    int scan_id = 1;

    PGraphics canvas;
    PGraphics overlay;


    @Override
    public void settings() {
        size(600, 600);
    }

    @Override
    public void setup() {

        contour_map = new int[width*height];

        cd = new ContourData();
        cd.corner_indexes = new int[width*height];
        cd.contour_indexes = new int[width*height];

        canvas = createGraphics(width, height);
        overlay = createGraphics(width, height);

        canvas.beginDraw();
        canvas.background(255);
        canvas.endDraw();


    }

    @Override
    public void draw() {
        //background(0);

        canvas.beginDraw();
        if (mousePressed) {
            canvas.noStroke();
            canvas.fill(0);
            canvas.ellipse(mouseX, mouseY, 30, 30);
        }

        canvas.noFill();
        canvas.stroke(255);
        canvas.rect(0, 0, width-1, height-1);
        canvas.endDraw();

        canvas.loadPixels();

        overlay.beginDraw();
        overlay.clear();


        int y_increment = 10;
        scan_id += BlobScanner.scan(
                canvas.pixels, width, height,
                0, 0, width, height,
                y_increment,
                (pixels, index) -> ((pixels[index] >> 8) & 0xFF) < 128,
                contour_map,
                scan_id,
                cd,
                contourData -> {


                    overlay.beginShape();
                    overlay.noFill();
                    overlay.stroke(0,255,0);

                    for (int i = 0; i < contourData.n_of_corners; i++) {
                        int index = contourData.corner_indexes[i];
                        float x = index % width;
                        float y = (index - x) / width;
                        overlay.vertex(x, y);
                    }

                    overlay.endShape();
                    return true;
                });

        println();

        overlay.endDraw();

        image(canvas, 0, 0);
        image(overlay, 0, 0);
    }
}

