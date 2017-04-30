package com.example.bennani.connectem;

import android.graphics.Color;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by Abdellatif BENNANI on 16/02/2017.
 */

public class octopus {

    protected int color;
    protected int connections;
    protected int connected;
    protected int _max;
    protected boolean isgreen;
    protected Rect rect;
    protected Rect left_rect;
    protected Rect top_rect;
    protected Rect right_rect;
    protected Rect bottom_rect;
    protected boolean left;
    protected boolean top;
    protected boolean right;
    protected boolean bottom;

    protected octopus(int nb_max, Rect[] rct){
        Random rn = new Random();
        _max = nb_max;
        connections = rn.nextInt(nb_max)+1;
        connected = 0;
        left = false;
        top = false;
        right = false;
        bottom = false;
        color = Color.BLUE;
        isgreen = false;
        if (connections == 0) isgreen = true;
        rect = rct[0];
        left_rect = rct[1];
        top_rect = rct[2];
        right_rect = rct[3];
        bottom_rect = rct[4];
    }

    protected void update(){
        connected = 0;
        if(left) connected++;
        if(top) connected++;
        if(right) connected++;
        if(bottom) connected++;
        if(connected == connections){
            color = Color.GREEN;
            isgreen = true;
        }
        if(connected > connections) {
            color = Color.RED;
            isgreen = false;
        }
        if(connected < connections) {
            color = Color.BLUE;
            isgreen = false;
        }
    }

    protected boolean exist(){
        if(connections != 0) return true;
        return false;
    }


    protected boolean isNotConnected(){
        if(connected == 0) return true;
        return false;
    }

    protected boolean Isntgreen(){
        return !isgreen;
    }

    protected int getcolor(){
        return color;
    }
}
