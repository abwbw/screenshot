package com.abwbw.screenshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.view.View;
import android.webkit.WebView;

import java.io.File;
import java.io.FileOutputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;


/**
 * @autor wangbinwei
 * @since 2017/9/6 上午8:47
 */

public class ScreenShotUtil {

    public static Disposable execute(Context context, View shotView, Consumer<ShotPicData> callback,
                                     Consumer<Throwable> errCb){
        if(shotView == null){
            throw new NullPointerException("shotView must not be null");
        }

        if(context == null){
            throw new NullPointerException("context must not be null");
        }

        return shotBitmap(shotView).observeOn(Schedulers.io())
                .map(saveBitmap(context)).observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback == null?Functions.<ShotPicData>emptyConsumer():callback, errCb == null?Functions.ERROR_CONSUMER:errCb);
    }


    private static Observable<Bitmap> shotBitmap(final View view){
        final Observable<Bitmap> shotObs = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                measureAndLayoutView(view);
                e.onNext(true);
                e.onComplete();
            }

        }).subscribeOn(AndroidSchedulers.mainThread()).observeOn(Schedulers.io()).map(new Function<Boolean, Bitmap>() {
            @Override
            public Bitmap apply(Boolean aBoolean) throws Exception {
                return createBitmap(view);
            }
        });

        return shotObs;
    }

    private static Function<Bitmap, ShotPicData> saveBitmap(final Context context){
        final Function<Bitmap, ShotPicData>  saveObs = new Function<Bitmap, ShotPicData> () {
            @Override
            public ShotPicData apply(Bitmap bitmap) throws Exception {
                if(bitmap == null){
                    throw new NullPointerException("the bitmap which need save is null");
                }

                File shotFile = new File(context.getExternalCacheDir(), getShotBimapName());

                if(shotFile.exists() && !shotFile.delete()){
                    throw new NullPointerException("delete exist shot picture error");
                }

                FileOutputStream outWriter = new FileOutputStream(shotFile);

                boolean comprssResult = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outWriter);

                if(!comprssResult){
                    throw new NullPointerException("compress a bitmpa to jepg fail");
                }

                outWriter.flush();
                outWriter.close();

                return new ShotPicData(bitmap, shotFile);
            }
        };

        return saveObs;
    }

    private static String getShotBimapName(){
        return "ShotImage.jpg";
    }

    private static boolean measureAndLayoutView(View view){
        if(view == null){
            return false;
        }

        view.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        return true;
    }

    private static Bitmap createBitmap(View view){
        if(view == null){
            return null;
        }
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap targetBmp = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBmp);
//        Bitmap bitmap = view.getDrawingCache();
//        if(view instanceof WebView){
//            Picture picture = ((WebView) view).capturePicture();
//            picture.draw(canvas);
//        } else {
//
//
//        }
        canvas.drawBitmap(targetBmp, 0, targetBmp.getHeight(), new Paint());
        view.draw(canvas);
        view.destroyDrawingCache();

        if(targetBmp == null){
            return null;
        }
        return targetBmp;
    }


}
