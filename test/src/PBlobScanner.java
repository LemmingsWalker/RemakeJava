import com.github.lemmingswalker.BlobScanner;
import com.github.lemmingswalker.ContourData;
import com.github.lemmingswalker.ContourDataProcessor;
import com.github.lemmingswalker.ThresholdChecker;
import processing.core.PImage;

/**
 * Created by doekewartena on 13/07/16.
 */
public class PBlobScanner {

    public ContourData contourData;
    public ThresholdChecker thresholdChecker;

    public int threshold;

    public int[] contour_exist_map;

    public int scan_id = 1;

    // ROI
    public float x1 = 0, y1 = 0, x2 = 1, y2 = 1;

    public int y_increment = 10;

    public int[] backup_boundary;

    public int border_color;

    public PBlobScanner() {
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

    public void set_ROI(float x1, float y1, float x2, float y2) {
        // todo Math.min(x1, x2) etc?
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

    public void backup_ROI(int[] pixels, int img_width, int img_height) {

        int x1 = (int) (this.x1 * img_width);
        int x2 = (int) (this.x2 * img_width);
        int y1 = (int) (this.y1 * img_height);
        int y2 = (int) (this.y2 * img_height);

        int i, max;
        int index = 0;

        // top
        i = y1 * img_width + x1;
        max = i + x2 - x1;
        for (; i < max; i++) {
            backup_boundary[index++] = pixels[i];
            pixels[i] = border_color;
        }

        // left and right
        for (int y = y1+1; y < y2-1; y++) {
            i = y * img_width + x1;
            backup_boundary[index++] = pixels[i];
            pixels[i] = border_color;
            i = y * img_width + x2 - 1;
            backup_boundary[index++] = pixels[i];
            pixels[i] = border_color;
        }

        // bottom
        i = (y2-1) * img_width + x1;
        max = i + x2 - x1;
        for (; i < max; i++) {
            backup_boundary[index++] = pixels[i];
            pixels[i] = border_color;
        }

    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

    public void restore_ROI(int[] pixels, int img_width, int img_height) {

        int x1 = (int) (this.x1 * img_width);
        int x2 = (int) (this.x2 * img_width);
        int y1 = (int) (this.y1 * img_height);
        int y2 = (int) (this.y2 * img_height);

        int i, max;
        int index = 0;

        // top
        i = y1 * img_width + x1;
        max = i + x2 - x1;
        for (; i < max; i++) {
            pixels[i] = backup_boundary[index++];
        }

        // left and right
        for (int y = y1+1; y < y2-1; y++) {
            i = y * img_width + x1;
            pixels[i] = backup_boundary[index++];
            i = y * img_width + x2 - 1;
            pixels[i] = backup_boundary[index++];
        }

        // bottom
        i = (y2-1) * img_width + x1;
        max = i + x2 - x1;
        for (; i < max; i++) {
            pixels[i] = backup_boundary[index++];
        }
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .



    public void check_init(int w, int h) {

        final int n = w * h;

        if (contourData == null ||
            contourData.contour_indexes.length < n/2 ) {

            contourData = new ContourData();
            contourData.corner_indexes = new int[n/2];
            contourData.contour_indexes = new int[n/2];

        }

        if (contour_exist_map == null ||
            contour_exist_map.length < n) {

            contour_exist_map = new int[n];
        }


        if (thresholdChecker == null) {

            thresholdChecker = new ThresholdChecker() {
                @Override
                public boolean result_of(int[] pixels, int index) {
                    return ((pixels[index] >> 8) & 0xFF) > threshold; // checking green
                }
            };
        }

        if (backup_boundary == null ||
            backup_boundary.length < w * 2 + h * 2) {
            backup_boundary = new int[w * 2 + h * 2];
        }

    }


    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


    public void scan(PImage img, ContourDataProcessor cdp) {
        check_init(img.width, img.height);
        backup_ROI(img.pixels, img.width, img.height);
        scan(img.pixels, img.width, img.height, cdp);
        restore_ROI(img.pixels, img.width, img.height);
    }


    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

    public void scan(int[] pixels,
                     int img_width,
                     int img_height,
                     ContourDataProcessor cdp) {

        // we do check boundary now as well which is not needed in our case...
        // we also check double now if we call the other method first
        check_init(img_width, img_height);

        int x1 = (int) (this.x1 * img_width);
        int x2 = (int) (this.x2 * img_width);
        int y1 = (int) (this.y1 * img_height);
        int y2 = (int) (this.y2 * img_height);

        scan_id += BlobScanner.scan(
                pixels,
                img_width, img_height,
                x1, y1, x2, y2,
                y_increment,
                thresholdChecker,
                contour_exist_map,
                scan_id,
                contourData,
                cdp);

    }
}
