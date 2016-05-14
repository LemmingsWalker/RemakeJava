import com.github.lemmingswalker.BlobScanner;
import com.github.lemmingswalker.ContourData;
import com.github.lemmingswalker.ContourDataProcessor;
import com.github.lemmingswalker.ThresholdChecker;
import processing.core.PApplet;

/**
 * Created by doekewartena on 5/6/16.
 */
public class DrawTest extends PApplet {


    public static void main(String[] args) {
        PApplet.main("DrawTest", args);
    }

    int[] contour_map;
    ContourData cd;


    @Override
    public void settings() {
        size(600, 600);
    }

    @Override
    public void setup() {

        contour_map = new int[width*height];

        cd = new ContourData();
        cd.corner_indexes = new int[width*height];
        cd.edge_indexes   = new int[width*height];

        background(0);
    }

    @Override
    public void draw() {
        //background(0);

        if (mousePressed) {
            noStroke();
            fill(255);
            ellipse(mouseX, mouseY, 30, 30);
        }

        noFill();
        stroke(0);
        rect(0, 0, width-1, height-1);

        loadPixels();

        int scan_id = frameCount;
        int y_increment = 10;
        BlobScanner.scan(
                pixels, width, height,
                0, 0, width, height,
                y_increment,
                color -> ((color >> 8) & 0xFF) > 128,
                contour_map,
                scan_id,
                cd,
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

        println();
    }
}

