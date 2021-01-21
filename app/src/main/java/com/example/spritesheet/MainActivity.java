package com.example.spritesheet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        gameView = new GameView(this);
        setContentView(gameView);
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    static class GameView extends SurfaceView implements Runnable{
        Thread gameThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playing;

        Canvas canvas;
        Paint paint;

        long fps;
        private long timeThisFrame;
        Bitmap bitmapDoraemon;
        boolean isMoving = false;
        boolean forward = false;
        float walkSpeedPerSecond = 250;
        float doraemonXPosition = 20;
        private int frameWidth = 70;
        private int frameHeight = 200;
        // How many frames are there on the sprite sheet?
        private int frameCount = 6;
        // Start at the first frame - where else?
        private int currentFrame = 0;
        // What time was it when we last changed frames
        private long lastFrameChangeTime = 0;
        // How long should each frame last
        private int frameLengthInMilliseconds = 100;
        // A rectangle to define an area of the sprite sheet that represents 1 frame
        private Rect frameToDraw = new Rect(
                0,
                0,
                frameWidth,
                frameHeight);
        // A rect that defines an area of the screen  on which to draw
        RectF whereToDraw = new RectF(doraemonXPosition, 0, doraemonXPosition + frameWidth, frameHeight);

        private long lastFrameChangeTIme = 0;
        private int frameLengthInMiliSeconds = 100;

        public GameView(Context context) {

            super(context);
            ourHolder = getHolder();
            paint = new Paint();

            bitmapDoraemon = BitmapFactory.decodeResource(this.getResources(), R.drawable.doraemon);
            bitmapDoraemon = Bitmap.createScaledBitmap(bitmapDoraemon, frameWidth * frameCount, frameHeight, false);

        }

        @Override
        public void run() {
            while(playing)
            {
                long startFrameTime = System.currentTimeMillis();
                update();
                draw(startFrameTime);

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1)
                {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void getCurrentFrame() {
            long time = System.currentTimeMillis();
            if (isMoving) {
                if (time > lastFrameChangeTime + frameLengthInMilliseconds) {
                    lastFrameChangeTime = time;
                    currentFrame++;
                    if (currentFrame >= frameCount) {
                        currentFrame = 0;
                    }
                    frameToDraw.left = currentFrame * frameWidth;
                    frameToDraw.right = frameToDraw.left + frameWidth;
                }
            }
        }

        public void back(){
            Matrix matrix = new Matrix();
            matrix.preScale(-1.0f, 1.0f);
            Bitmap bInput = bitmapDoraemon;
            bitmapDoraemon = Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), matrix, true);
        }

        public void update() {
            if (isMoving)
            {
                if(doraemonXPosition > (getScreenWidth()-100) || doraemonXPosition < 0)
                {
                    forward = true;
                    if(forward){
                        back();
                        forward = false;
                    }
                    walkSpeedPerSecond = -walkSpeedPerSecond;
                }
                if(forward){
                    back();
                    forward = false;
                }
                doraemonXPosition = doraemonXPosition + (walkSpeedPerSecond/fps);


            }
        }

        public void draw(long startFrameTime)
        {
            if(ourHolder.getSurface().isValid())
            {
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255,170,184,211));

                paint.setColor(Color.argb(255,73,73,73));

                paint.setTextSize(45);

                canvas.drawText("FPS : " + fps, 20,200,paint) ;
                canvas.drawText("Height : " + frameHeight + "  Width : " + frameWidth, 20,240,paint) ;
                canvas.drawText("Doraemon Height : " + bitmapDoraemon.getHeight() + "   Doraemon Width : " + bitmapDoraemon.getHeight(), 20,280,paint) ;
                canvas.drawText("Doraemon X Position : " + startFrameTime, 20,320,paint) ;

                whereToDraw.set((int)doraemonXPosition, 20,(int)doraemonXPosition + frameWidth, frameHeight);
                getCurrentFrame();
                canvas.drawBitmap(bitmapDoraemon, frameToDraw, whereToDraw, paint);
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e){
                Log.e("Error : ", "joining thread");
            }
        }

        public void resume() {
            playing = true;
            gameThread= new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent)
        {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK)
            {
                case MotionEvent.ACTION_DOWN:
                    isMoving = true;
                    break;

                case MotionEvent.ACTION_UP:
                    isMoving = false;
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

}