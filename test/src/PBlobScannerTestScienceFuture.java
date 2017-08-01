import com.github.lemmingswalker.ContourData;
import com.github.lemmingswalker.ContourDataProcessor;
import com.github.lemmingswalker.ThresholdChecker;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.*;

/**
 * Created by doekewartena on 14/07/16.
 */
public class PBlobScannerTestScienceFuture extends PApplet {



    public static void main(String[] args) {
        PApplet.main("PBlobScannerTestScienceFuture", args);
    }

    String movie_path = "/Users/doekewartena/Downloads/vids_future_proj";
    String[] movies = {"VID_20160523_180100", "VID_20160525_113640", "VID_20160525_115918", "VID_20160530_144454", "VID_20160601_110607", "VID_20160601_122621", "VID_20160609_115837"};
    Movie movie;

    PGraphics scan_img;
    PGraphics background;
    PGraphics strokes;


    PBlobScanner blobScanner;

    PGraphics[] buffer = new PGraphics[100];

    boolean play_buffer;

    @Override
    public void settings() {
        size(720, 1280);
    }

    @Override
    public void setup() {

        frameRate(23);

        scan_img = createGraphics(width, height);
        background = createGraphics(width, height);
        strokes = createGraphics(width, height);

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = createGraphics(width, height);
        }

        blobScanner = new PBlobScanner();

        movie = new Movie(this, movie_path+"/"+movies[0]+".mp4");
        movie.play();
        movie.jump(0);
        movie.pause();
    }

    void setFrame(int n) {
        movie.play();

        float movFrameRate = 23.525908f;


        // The duration of a single frame:
        float frameDuration = 1.0f / movFrameRate;

        // We move to the middle of the frame by adding 0.5:
        float where = (n + 0.5f) * frameDuration;

        // Taking into account border effects:
        float diff = movie.duration() - where;
        if (diff < 0) {
            where += diff - 0.25f * frameDuration;
        }

        movie.jump(where);
        movie.pause();
    }


    @Override
    public void draw() {

        if (frameCount == 105) {
            for (int i = 0 ; i < buffer.length; i++) {
                buffer[i].loadPixels();
                buffer[i].save("/Users/doekewartena/Desktop/export/05/"+nf(i, 3)+".png");
            }
        }

        if (play_buffer) {
            image(buffer[frameCount % buffer.length], 0, 0);
            return;
        }

        setFrame(frameCount);



        {
            PGraphics pg = scan_img;

            pg.beginDraw();
            pg.background(0);
            pg.translate(0, height);
            pg.rotate(radians(-90));
            pg.image(movie, 0, 0);
            pg.endDraw();
            pg.loadPixels();

            pg.filter(GRAY);

            fastblur(pg, 10);
        }

       // image(pg, 0, 0);

        {

            PGraphics pg = background;

            int levels = 24;

            for (int i = 0; i < levels; i++) {

                pg.beginDraw();

                final int ii = i;

                blobScanner.y_increment = 20;
                blobScanner.threshold = (int) map(i, 0, levels - 1, 0, 256);
                blobScanner.thresholdChecker = (pixels, index) -> brightness(pixels[index]) > blobScanner.threshold;

                blobScanner.scan(scan_img, new ContourDataProcessor() {
                    @Override
                    public boolean process(ContourData contourData) {

                        pg.beginShape();
                        pg.noStroke();
                        //fill((int)map(ii, 0, levels-1, 0, 256));

                        float t = norm(ii, 0, levels - 1);
                        //println(t);
                        pg.fill(pg.lerpColor(pg.color(0, 0, 5), pg.color(0, 0, 255), t));
                        for (int j = 0; j < contourData.n_of_corners; j++) {
                            int index = contourData.corner_indexes[j];
                            float x = index % width;
                            float y = (index - x) / width;
                            pg.vertex(x, y);
                        }
                        pg.endShape();


//                        float last_x = -1;
//                        float last_y = -1;
//
//                        pg.fill(255);
//                        pg.noStroke();
//
//                        float max_dist_sq = sq(8);
//
//                        for (int j = 0; j < contourData.n_of_indexes; j++) {
//                            int index = contourData.contour_indexes[j];
//                            float x = index % width;
//                            float y = (index - x) / width;
//
//                            if (dist_sq(x, y, last_x, last_y) > max_dist_sq) {
//                                float d = random(3.f, 3);
//                                pg.ellipse(x, y, d, d);
//                                last_x = x;
//                                last_y = y;
//                            }
//
//                        }


                        return true;
                    }


                });

                pg.loadPixels();
                int blur_rad = (int)map(ii, 0, levels-1, 0, 3);
                //fastblur(pg, blur_rad);
                pg.updatePixels();

                pg.endDraw();


            }
        }

        image(background, 0, 0);



        if (true) {
            PGraphics pg = strokes;

            pg.beginDraw();
            pg.background(255, 0);
            pg.endDraw();

            int levels = 12;

            randomSeed(1);

            for (int i = 0; i < levels; i++) {

                final int ii = i;

                pg.beginDraw();

                blobScanner.y_increment = 20;
                blobScanner.threshold = (int) map(i, 0, levels - 1, 0, 256);
                blobScanner.thresholdChecker = (pixels, index) -> brightness(pixels[index]) > blobScanner.threshold;
                blobScanner.scan(scan_img, new ContourDataProcessor() {
                    @Override
                    public boolean process(ContourData contourData) {


                        float last_x = -1;
                        float last_y = -1;

                        pg.fill(255);
                        pg.noStroke();

                        float t = norm(ii, 0, levels-1);

                        float max_dist_sq = sq(8);

                        max_dist_sq = map(t, 0, 1, sq(19), sq(3));

                        pg.stroke(200,200,255);

                        for (int j = 0; j < contourData.n_of_indexes; j++) {
                            int index = contourData.contour_indexes[j];
                            float x = index % width;
                            float y = (index - x) / width;

                            if (dist_sq(x, y, last_x, last_y) > max_dist_sq) {
                                //float d = random(1.5f, 1.5f);

                                //float d = map(noise(0.1f*x, 0.1f*y), 0, 1, 0.0001f, 7);

                                //float d = map(dist(x, y, width/2, height/2), 0, dist(0, 0, width/2, height/2), 0.3f, 6);

                                float d = 2.5f;


                                //float d = map(t, 0, 1, 5f, 1);

                                pg.stroke(lerpColor(color(0,0,255), color(255,255,255), t));

                                pg.strokeWeight(d);


                                float offset_x = 0;//((noise(x * 0.05f, y * 0.05f)*2)-1) * 20;
                                float offset_y = 0;//((noise(y * 0.05f, x * 0.05f)*2)-1) * 20;


                                pg.point(x + offset_x, y + offset_y);
                                //pg.ellipse(x + offset_x, y + offset_y, d, d);
                                //pg.fill(lerpColor(color(0,0,255), color(255,255,255), t));

                                //pg.rect(x, y, 10, 10);

                                last_x = x;
                                last_y = y;
                            }

                        }

                        return true;
                    }
                });

                pg.endDraw();

//                pg.loadPixels();
//                int blur_rad = (int)map(ii, 0, levels-1, 0, 20);
//                fastblur(pg, blur_rad);
//                pg.updatePixels();

            }
            image(strokes, 0, 0);
        }


        PGraphics buffer_img =  buffer[frameCount % buffer.length];
        buffer_img.beginDraw();
        buffer_img.image(background, 0, 0);
        buffer_img.image(strokes, 0, 0);
        buffer_img.endDraw();


//        loadPixels();
//        fastblur(g, 30);
//        updatePixels();


        //image(pg, 0, 0);



//        fill(0, 2);
//        blendMode(DARKEST);
//        //rect(0, 0, width, height);
//        blendMode(BLEND);
//
//        //background(0);
//
//
//
//        //blobScanner.threshold = (int)map(mouseX, 0, width, 0, 255);
//        //blobScanner.threshold = (int) map(sin(frameCount * 0.02f), -1, 1, 0, 255);
//        blobScanner.threshold = (int) map(frameCount%32, 0, 32, 0, 255);
//
//        for (int i = 3; i < 4; i++) {
//
//            translate(1, 1);
//
//            ThresholdChecker thresholdChecker;
//            int color;
//            int alpha = 255 / 4;
//
//            if (i == 0) {
//                thresholdChecker = t_r;
//                color = color(255,0,0, alpha);
//            }
//            else if (i == 1) {
//                thresholdChecker = t_g;
//                color = color(0,255,0, alpha);
//            }
//            else if (i == 2) {
//                thresholdChecker = t_b;
//                color = color(0,0,255, alpha);
//            }
//            else {
//                thresholdChecker = t_rgb;
//                color = color(255,255,255, alpha);
//            }
//            blobScanner.thresholdChecker = thresholdChecker;
//
//            blobScanner.y_increment = 10;
//
//
//
//
//            blobScanner.scan(
//                    img,
//                    contourData -> {
//
//
//                        //beginShape();
//                        noFill();
//                        stroke(color);
//
//                        for (int j = 0; j < contourData.n_of_corners; j++) {
//                            int index = contourData.corner_indexes[j];
//                            float x = index % width;
//                            float y = (index - x) / width;
//                            //vertex(x, y);
//
//                            //ellipse(x, y, 2, 2);
//                            float a = atan2(pre_x - x, pre_y - y);
//                            float l = 10;
//                            stroke(img.get((int)x, (int)y));
//                            line(x + cos(a) * -l, y + sin(a) * -l, x + cos(a) * l, y + sin(a) * l);
//
//
//
//
//                            pre_x = x;
//                            pre_y = y;
//
//                        }
//
//                        //endShape();
//
//                        return true;
//                    });
//
//        }
        surface.setTitle(""+(int)frameRate+"   "+frameCount);
    }

    public float dist_sq(float x1, float y1, float x2, float y2) {
        return sq(x2-x1) + sq(y2-y1);
    }


    public void movieEvent(Movie m) {
        m.read();
    }

    void fastblur(PImage img, int radius)
    {
        if (radius<1){
            return;
        }
        int w=img.width;
        int h=img.height;
        int wm=w-1;
        int hm=h-1;
        int wh=w*h;
        int div=radius+radius+1;
        int r[]=new int[wh];
        int g[]=new int[wh];
        int b[]=new int[wh];
        int rsum,gsum,bsum,x,y,i,p,p1,p2,yp,yi,yw;
        int vmin[] = new int[max(w,h)];
        int vmax[] = new int[max(w,h)];
        int[] pix=img.pixels;
        int dv[]=new int[256*div];
        for (i=0;i<256*div;i++){
            dv[i]=(i/div);
        }

        yw=yi=0;

        for (y=0;y<h;y++){
            rsum=gsum=bsum=0;
            for(i=-radius;i<=radius;i++){
                p=pix[yi+min(wm,max(i,0))];
                rsum+=(p & 0xff0000)>>16;
                gsum+=(p & 0x00ff00)>>8;
                bsum+= p & 0x0000ff;
            }
            for (x=0;x<w;x++){

                r[yi]=dv[rsum];
                g[yi]=dv[gsum];
                b[yi]=dv[bsum];

                if(y==0){
                    vmin[x]=min(x+radius+1,wm);
                    vmax[x]=max(x-radius,0);
                }
                p1=pix[yw+vmin[x]];
                p2=pix[yw+vmax[x]];

                rsum+=((p1 & 0xff0000)-(p2 & 0xff0000))>>16;
                gsum+=((p1 & 0x00ff00)-(p2 & 0x00ff00))>>8;
                bsum+= (p1 & 0x0000ff)-(p2 & 0x0000ff);
                yi++;
            }
            yw+=w;
        }

        for (x=0;x<w;x++){
            rsum=gsum=bsum=0;
            yp=-radius*w;
            for(i=-radius;i<=radius;i++){
                yi=max(0,yp)+x;
                rsum+=r[yi];
                gsum+=g[yi];
                bsum+=b[yi];
                yp+=w;
            }
            yi=x;
            for (y=0;y<h;y++){
                pix[yi]=0xff000000 | (dv[rsum]<<16) | (dv[gsum]<<8) | dv[bsum];
                if(x==0){
                    vmin[y]=min(y+radius+1,hm)*w;
                    vmax[y]=max(y-radius,0)*w;
                }
                p1=x+vmin[y];
                p2=x+vmax[y];

                rsum+=r[p1]-r[p2];
                gsum+=g[p1]-g[p2];
                bsum+=b[p1]-b[p2];

                yi+=w;
            }
        }

    }


    @Override
    public void keyPressed() {
       // image(img, 0, 0, img.width, img.height);
        play_buffer = !play_buffer;
    }
}
