package com.abwbw.screenshot;

import android.graphics.Bitmap;

import java.io.File;

/**
 * @autor wangbinwei
 * @since 2017/9/6 下午12:49
 */

public class ShotPicData {
    private Bitmap bitmap;
    private File file;

    public ShotPicData(Bitmap bitmap, File file){
        this.bitmap = bitmap;
        this.file =file;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public File getFile() {
        return file;
    }
}
