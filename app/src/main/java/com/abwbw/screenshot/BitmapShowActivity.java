package com.abwbw.screenshot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @autor wangbinwei
 * @since 2017/9/6 上午11:40
 */

public class BitmapShowActivity extends AppCompatActivity {
    public static final String EXTRA_BITMAP_PATH= "bitmap_path";
    private ImageView mShowIv;
    private String mPath;
    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_show);
        mShowIv = (ImageView) findViewById(R.id.show_img_iv);
        mShowIv.setVisibility(View.INVISIBLE);

        mPath = getIntent().getStringExtra(EXTRA_BITMAP_PATH);

    }

    @Override
    protected void onStart() {
        super.onStart();

        showBitmap(mPath);
    }

    private void showBitmap(final String bitmapPath){
        mDisposable = Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> e) throws Exception {
                Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath);
                if(bitmap == null){
                    e.onError(new NullPointerException("not find bitmap:" + bitmapPath));
                    return;
                }

                e.onNext(bitmap);
                e.onComplete();

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Bitmap>() {
            @Override
            public void accept(Bitmap bitmap) throws Exception {
                mShowIv.setImageBitmap(bitmap);
                mShowIv.startAnimation(new AlphaAnimation(0.f, 1.f));
                mShowIv.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(mDisposable != null && !mDisposable.isDisposed()){
            mDisposable.dispose();
        }
    }
}
