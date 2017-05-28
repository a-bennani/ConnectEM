
package com.example.bennani.connectem;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.MainThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static java.lang.Math.abs;
import static java.lang.Math.copySign;

/**
 * Created by Abdellatif BENNANI on 16/02/2017.
 */

public class SurfaceViewActivity extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private boolean paused;
    private boolean finished;
    private Thread cv_thread;
    SurfaceHolder holder;

    Paint paint;

    private Bitmap win;
    private Bitmap aquarium;
    private Bitmap poulpe;
    private Bitmap poulpeR;
    private Bitmap poulpeB;
    private Bitmap poulpeG;

    private Bitmap tentaculeR;
    private Bitmap tentaculeR_90;
    private Bitmap tentaculeR_180;
    private Bitmap tentaculeR_270;

    private Bitmap tentaculeB;
    private Bitmap tentaculeB_90;
    private Bitmap tentaculeB_180;
    private Bitmap tentaculeB_270;

    private Bitmap tentaculeG;
    private Bitmap tentaculeG_90;
    private Bitmap tentaculeG_180;
    private Bitmap tentaculeG_270;

    private boolean isWon = false;

    // Declaration des objets Ressources et Context permettant d'acc�der aux ressources de notre application et de les charger
    private Resources mRes;
    private Context mContext;

    // tableau modelisant la _octopus du jeu
    static octopus[][] _octopus;

    // taille de la _octopus
    static final int carteWidth = 4;
    static final int carteHeight = 5;
    int imgSize;
    int widthTileSize;
    int heightTileSize;
    Rect mainRect;

    boolean evtTranslate = false;
    int evtDownX;
    int evtDownY;
    int evtOffsetX;
    int evtOffsetY;
    static boolean notInit = true;

    long time = 0;
    /**
     * The constructor called from the main JetBoy activity
     *
     * @param context
     * @param attrs
     */
    public SurfaceViewActivity(Context context, AttributeSet attrs) {
        super(context, attrs);

        // permet d'ecouter les surfaceChanged, surfaceCreated, surfaceDestroyed
        holder = getHolder();
        holder.addCallback(this);
        // chargement des images
        mContext = context;
        mRes = mContext.getResources();
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;
        win = BitmapFactory.decodeResource(mRes, R.drawable.bravo);
        aquarium = BitmapFactory.decodeResource(mRes, R.drawable.aquarium);
        poulpe =  BitmapFactory.decodeResource(mRes, R.drawable.poulpe, opt);
        poulpeR =  BitmapFactory.decodeResource(mRes, R.drawable.poulpe_red, opt);
        poulpeB =  BitmapFactory.decodeResource(mRes, R.drawable.poulpe_blue, opt);
        poulpeG =  BitmapFactory.decodeResource(mRes, R.drawable.poulpe_green, opt);

        opt.inSampleSize = 3;

        tentaculeR =  BitmapFactory.decodeResource(mRes, R.drawable.tentacule_red, opt);
        tentaculeR_90 = rotateBitmap(tentaculeR, 90);
        tentaculeR_180 = rotateBitmap(tentaculeR, 180);
        tentaculeR_270 = rotateBitmap(tentaculeR, 270);

        tentaculeB =  BitmapFactory.decodeResource(mRes, R.drawable.tentacule_blue, opt);
        tentaculeB_90 = rotateBitmap(tentaculeB, 90);
        tentaculeB_180 = rotateBitmap(tentaculeB, 180);
        tentaculeB_270 = rotateBitmap(tentaculeB, 270);

        tentaculeG =  BitmapFactory.decodeResource(mRes, R.drawable.tentacule_green, opt);
        tentaculeG_90 = rotateBitmap(tentaculeG, 90);
        tentaculeG_180 = rotateBitmap(tentaculeG, 180);
        tentaculeG_270 = rotateBitmap(tentaculeG, 270);


        // initialisation des parmametres du jeu
        initparameters();

        // creation du thread
/*
        cv_thread = new Thread(this);
        if ((cv_thread != null) && (!cv_thread.isAlive())) {
            cv_thread.start();
            Log.i("-FCT-", "cv_thread.start()");
        }
*/


        // prise de focus pour gestion des touches
        setFocusable(true);


    }

    public void reshape() {
        mainRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
        widthTileSize = getWidth() /carteWidth;
        heightTileSize = getHeight() /carteHeight;
        imgSize = heightTileSize / 6;

        Log.i("-> FCT <-", "reshape " + imgSize);
    }


    private int update_neibourghs(){
        int k, cnt = 0;
        for (int i = 0; i < carteHeight; i++)
            for (int j = 0; j < carteWidth; j++) {
                k = 0;
                if(i > 0 && _octopus[i-1][j].exist()) k++;
                if(i < carteHeight - 1 && _octopus[i+1][j].exist()) k++;
                if(j > 0 && _octopus[i][j-1].exist()) k++;
                if(j < carteWidth - 1 && _octopus[i][j+1].exist()) k++;
                if(_octopus[i][j].connections > k) {
                    _octopus[i][j].connections = k;
                    _octopus[i][j]._max = k;
                    _octopus[i][j].update();
                }
                cnt += _octopus[i][j].connections;
            }
        if((k = (cnt % 4)) != 0){
            for (int i = 0; i < carteHeight; i++)
                for (int j = 0; j < carteWidth; j++) {
                    if(_octopus[i][j].connections > 0){
                        _octopus[i][j].connections--;
                        _octopus[i][j].update();
                        k--;
                        if(k == 0)return 0;
                    }
                }
        }
        return 0;
    }

    private void initOctopus() {
        _octopus = new octopus[carteHeight][carteWidth];
        int k;
        for (int i = 0; i < carteHeight; i++)
            for (int j = 0; j < carteWidth; j++) {
                k = 4;
                if (i == 1 || (i == carteHeight - 2) || j == 1 || (j == carteWidth - 2))
                    k = 3;
                if ((i == 0 || i == carteHeight - 1) && (j == 0 || j == carteWidth - 1))
                    k = 2;

                Rect centralRect = new Rect(mainRect.left + j * widthTileSize ,//left
                        mainRect.top + i * heightTileSize ,// top
                        mainRect.left + (j + 1) * widthTileSize , //right
                        mainRect.top + (i + 1) * heightTileSize );//bottom

                int for_margin = (int) (widthTileSize / 3.6), three_margin = (int)(widthTileSize / 3.),
                        Xcenter = centralRect.left + centralRect.width() / 2,
                        Ycenter = centralRect.top + centralRect.height() / 2,
                        horizantal_margin = (widthTileSize - centralRect.width()) / 2 + imgSize,
                        vertical_margin = (heightTileSize - centralRect.height()) / 2 + imgSize / 3;

                Rect leftRect = new Rect(centralRect.left - horizantal_margin,//left
                        centralRect.top + three_margin,// top
                        Xcenter - imgSize, //right
                        centralRect.bottom - three_margin );//bottom

                Rect topRect = new Rect(centralRect.left + for_margin,//left
                        centralRect.top - for_margin,// top
                        centralRect.right - for_margin, //right
                        Ycenter);//bottom

                Rect rightRect = new Rect(Xcenter + imgSize,//left
                        centralRect.top + three_margin,// top
                        centralRect.right + horizantal_margin, //right
                        centralRect.bottom - three_margin);//bottom

                Rect bottomRect = new Rect(centralRect.left + for_margin,//left
                        Ycenter,// top
                        centralRect.right - for_margin, //right
                        centralRect.bottom + for_margin);//bottom

                Rect[] _rect = {centralRect, leftRect, topRect, rightRect, bottomRect};

                _octopus[i][j] = new octopus(k, _rect);

            }
        update_neibourghs();
    }

    // initialisation du jeu
    public void initparameters() {
        paused = false;
        finished = false;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paint.setTextSize(65);
        paint.setTextScaleX(1.f);
        paint.setAlpha(0);
        paint.setColor(Color.BLACK);
        //recalibrage des dimentions
        //reshape();
        //initOctopus();
    }

    private void loadlevel(){
        initOctopus();
    }

    // dessin des fleches
    private void paintaqua(Canvas canvas) {
        canvas.drawBitmap(aquarium, null, mainRect, null);
    }

    // dessin du gagne si gagne
    private void paintwin(Canvas canvas) {
        canvas.drawBitmap(win, null, new Rect(getLeft(),
                getTop() + getHeight()/3,
                getRight(),
                getBottom() - getHeight()/3), null);
    }

    private static Bitmap rotateBitmap(Bitmap bm, int angle){
        Matrix  mtx = new Matrix();
        mtx.postRotate(angle);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mtx, true);
    }

    // dessin de la _octopus du jeu
    private void paintOctopus(Canvas canvas) {
        for (int i = 0; i < carteHeight; i++) {
            for (int j = 0; j < carteWidth; j++) {
                if(_octopus[i][j].exist()) {
                    switch (_octopus[i][j].getcolor()){
                        case Color.RED:
                            if(_octopus[i][j].left)
                                canvas.drawBitmap( tentaculeR_270, null, _octopus[i][j].left_rect, null);
                            if(_octopus[i][j].top)
                                canvas.drawBitmap(tentaculeR, null, _octopus[i][j].top_rect, null);
                            if(_octopus[i][j].right)
                                canvas.drawBitmap( tentaculeR_90, null, _octopus[i][j].right_rect, null);
                            if(_octopus[i][j].bottom)
                                canvas.drawBitmap( tentaculeR_180, null, _octopus[i][j].bottom_rect, null);
                            canvas.drawBitmap(poulpeR, null, _octopus[i][j].rect, null);
                            break;

                        case Color.GREEN:
                            if(_octopus[i][j].left)
                                canvas.drawBitmap( tentaculeG_270, null, _octopus[i][j].left_rect, null);
                            if(_octopus[i][j].top)
                                canvas.drawBitmap(tentaculeG, null, _octopus[i][j].top_rect, null);
                            if(_octopus[i][j].right)
                                canvas.drawBitmap( tentaculeG_90, null, _octopus[i][j].right_rect, null);
                            if(_octopus[i][j].bottom)
                                canvas.drawBitmap( tentaculeG_180, null, _octopus[i][j].bottom_rect, null);
                            canvas.drawBitmap(poulpeG, null, _octopus[i][j].rect, null);
                            break;

                        case Color.BLUE:
                            if(_octopus[i][j].left)
                                canvas.drawBitmap( tentaculeB_270, null, _octopus[i][j].left_rect, null);
                            if(_octopus[i][j].top)
                                canvas.drawBitmap(tentaculeB, null, _octopus[i][j].top_rect, null);
                            if(_octopus[i][j].right)
                                canvas.drawBitmap( tentaculeB_90, null, _octopus[i][j].right_rect, null);
                            if(_octopus[i][j].bottom)
                                canvas.drawBitmap( tentaculeB_180, null, _octopus[i][j].bottom_rect, null);
                            canvas.drawBitmap(poulpeB, null, _octopus[i][j].rect, null);
                            break;
                    }


                    int x = (_octopus[i][j].rect.left + (int)(_octopus[i][j].rect.width()/2.7));
                    int y = (_octopus[i][j].rect.top + _octopus[i][j].rect.height()/4);
                    canvas.drawText(String.valueOf(_octopus[i][j].connections), x, y, paint);

                    if(time != 0 && !evtTranslate
                            && _octopus[i][j].rect.contains(evtDownX, evtDownY)
                            && _octopus[i][j].rect.contains(evtDownX + evtOffsetX, evtDownY + evtOffsetY)
                            && (System.currentTimeMillis() - time) > 1000
                            && _octopus[i][j].exist()
                            && _octopus[i][j].isNotConnected()) {
                        evtTranslate = true;
                    }
                    if(evtTranslate && _octopus[i][j].rect.contains(evtDownX, evtDownY)) {
                        Rect rct = new Rect(_octopus[i][j].rect);
                        canvas.drawBitmap(poulpe, null, rct, null);
                        rct.offset(evtOffsetX, evtOffsetY);
                        canvas.drawBitmap(poulpe, null, rct, null);
                        continue;
                    }

                }
            }
        }
    }


    // permet d'identifier si la partie est gagnee (tous les diamants à leur place)
    private boolean isWon() {
        for (int i = 0; i < carteHeight; i++) {
            for (int j = 0; j < carteWidth; j++) {
                if (_octopus[i][j].Isntgreen())
                    return false;
            }
        }
        return true;
    }

    // dessin du jeu (fond uni, en fonction du jeu gagne ou pas dessin du plateau et du joueur des diamants et des fleches)
    private void nDraw(Canvas canvas) {
        canvas.drawRGB(255, 255, 255);
        //paintaqua(canvas);
        paintOctopus(canvas);
        if (isWon) {
            paintwin(canvas);
        }
        //Log.i("-> FCT <-", "draw");
    }

    // callback sur le cycle de vie de la surfaceview
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("-> FCT <-", "surfaceChanged " + width + " - " + height);
        reshape();
        if (notInit) initOctopus();
        notInit = false;
        //cv_thread.start();
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        Log.i("-> FCT <-", "surfaceCreated");
    }


    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.i("-> FCT <-", "surfaceDestroyed");
    }


    public void pause()
    {
        Log.i("-> FCT <-", "pause");
        paused = true;
        synchronized (cv_thread) {
            while (paused) {
                try {
                    cv_thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            cv_thread = null;
        }
    }

    public void resume() {
        Log.i("-> FCT <-", "resume");
        if (cv_thread == null) {
            cv_thread = new Thread(this);
            Log.i("-> FCT <-", "thread create and start");
        }
        cv_thread.start();

        paused = false;
        synchronized (cv_thread) {
            cv_thread.notifyAll();
        }
   }

    /**
     * run (run du thread créé)
     * on endort le thread, on modifie le compteur d'animation, on prend la main pour dessiner et on dessine puis on libère le canvas
     */
    public void run() {
        Log.i("-> FCT <-", "run");
        while (!paused)
        {
            synchronized (cv_thread){

            }
            if (!holder.getSurface().isValid())
                continue;
            Canvas c = holder.lockCanvas(null);
            nDraw(c);
            holder.unlockCanvasAndPost(c);
            try {
                cv_thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e("-> RUN <-", "PB DANS RUN");
            }
        }
    }

    // fonction permettant de recuperer les evenements tactiles
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction(), actionX = (int) event.getX(), actionY = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if(isWon){
                    loadlevel();
                    isWon = false;
                }
                time = System.currentTimeMillis();
                evtDownX = actionX; evtDownY = actionY;
                //Log.i("-> FCT <-", "onTouchEvent down: x = " + evtDownX + " y = " + evtDownY );
                return true;
            case MotionEvent.ACTION_MOVE:

                evtOffsetX = actionX - evtDownX;
                evtOffsetY = actionY - evtDownY;
                //Log.i("-> FCT <-", "onTouchEvent move: x = " + evtOffsetX + " y = " + evtOffsetY);
                return true;
            case MotionEvent.ACTION_UP:
                int deltaX = (actionX - evtDownX), deltaY = (actionY - evtDownY);
                //Log.i("-> FCT <-", "onTouchEvent up: dx = " + deltaX + " dy = " + deltaY);
                int i = (evtDownY - mainRect.top) / heightTileSize;
                int j = (evtDownX - mainRect.left) / widthTileSize;
                int ii = (actionY - mainRect.top) / heightTileSize;
                int jj = (actionX - mainRect.left) / widthTileSize;
                if(!evtTranslate && _octopus[i][j].exist() && ((abs(deltaX) > widthTileSize/2) || (abs(deltaY ) > heightTileSize/2))){
                    if(abs(deltaX) > abs(deltaY)){ // swipe horizantally
                        if(deltaX < 0){//to left
                            Log.i("-> FCT <-", "swip to left ");
                            if(j > 0 && _octopus[i][j - 1].exist()) {
                                _octopus[i][j].left = !_octopus[i][j].left;
                                _octopus[i][j - 1].right = !_octopus[i][j - 1].right;
                                _octopus[i][j - 1].update();
                            }
                        } else {// to right
                            Log.i("-> FCT <-", "swip to right ");
                            if(j < carteWidth - 1 && _octopus[i][j + 1].exist()) {
                                _octopus[i][j].right = !_octopus[i][j].right;
                                _octopus[i][j + 1].left = !_octopus[i][j + 1].left;
                                _octopus[i][j + 1].update();
                            }
                        }
                    }else if (abs(deltaX) < abs(deltaY)){ //swipe vertically
                        if(deltaY < 0){ // to top
                            Log.i("-> FCT <-", "swip to top ");
                            if(i > 0 && _octopus[i - 1][j].exist()) {
                                _octopus[i][j].top = !_octopus[i][j].top;
                                _octopus[i - 1][j].bottom = !_octopus[i - 1][j].bottom;
                                _octopus[i - 1][j].update();
                            }
                        }else { // to bottom
                            Log.i("-> FCT <-", "swip to bottom " + i);
                            if(i < carteHeight - 1 && _octopus[i + 1][j].exist()) {
                                _octopus[i][j].bottom = !_octopus[i][j].bottom;
                                _octopus[i + 1][j].top = !_octopus[i + 1][j].top;
                                _octopus[i + 1][j].update();
                            }
                        }
                    }
                    _octopus[i][j].update();
                }
                if(evtTranslate
                        && _octopus[i][j].connections <= _octopus[ii][jj]._max
                        && _octopus[ii][jj].connections <= _octopus[i][j]._max
                        && _octopus[i][j].connected == 0
                        && _octopus[ii][jj].connected == 0){
                    int tmp = _octopus[i][j].connections;
                    _octopus[i][j].connections = _octopus[ii][jj].connections;
                    _octopus[ii][jj].connections = tmp;
                    _octopus[i][j].update();
                    _octopus[ii][jj].update();
                }
                isWon = isWon();
                evtTranslate = false;
                evtDownX = 0;
                evtDownY = 0;
                evtOffsetX = 0;
                evtOffsetY = 0;
                time = 0;
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }
    //return super.onTouchEvent(event);
}
