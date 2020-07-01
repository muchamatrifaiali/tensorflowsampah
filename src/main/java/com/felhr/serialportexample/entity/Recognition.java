package com.felhr.serialportexample.entity;

import android.graphics.Bitmap;

import java.util.List;

public interface Recognition {

    List<Classifier> recognize(Bitmap bitmap);

    void close();

}
