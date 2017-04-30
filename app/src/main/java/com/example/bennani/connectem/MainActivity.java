package com.example.bennani.connectem;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    private SurfaceViewActivity mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // recuperation de la vue une voie cree à partir de son id
        mView = (SurfaceViewActivity) findViewById(R.id.mainView);
        // rend visible la vue
        mView.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mView.pause();
        //mView.stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.resume();
    }

}
