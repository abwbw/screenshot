package com.abwbw.screenshot;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements ScreenShotListenHelper.OnScreenShotListener, View.OnClickListener{
    private FrameLayout mContentFl;
    private Disposable mDisposable;
    private WebView mWebview;
    private Button mGoBtn;
    private EditText mUrlTv;
    private PopupWindow mPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableSlowWholeDocumentDraw();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentFl = (FrameLayout) findViewById(R.id.content_fl);
        mGoBtn = (Button)findViewById(R.id.go_btn);
        mUrlTv = (EditText)findViewById(R.id.url_tv);
        mGoBtn.setOnClickListener(this);
        attachWebView();

        startScreenShotListener();
    }

    private void startScreenShotListener(){
        ScreenShotListenHelper manager = ScreenShotListenHelper.newInstance(this);
        manager.setListener(this);
        manager.startListen();
    }

    private void attachWebView(){
        mWebview = new WebView(this);
        mWebview.setWebChromeClient(new WebChromeClient());
        mWebview.setWebViewClient(new WebViewClient());
        mContentFl.addView(mWebview, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mWebview.loadUrl("https://www.baidu.com");
    }

    private void enableSlowWholeDocumentDraw(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }
    }

    private void showPopupBitmap(ShotPicData data){
        if(mPopup != null){
            mPopup.dismiss();
        }
        PopupWindow window = new PopupWindow(MainActivity.this);
        window.setHeight(dp2px(192));
        window.setWidth(dp2px(108));
//        window.setOutsideTouchable(true);
//        window.setBackgroundDrawable(new BitmapDrawable());
        window.setAnimationStyle(R.style.PopupAnim);

        ImageView img = new ImageView(MainActivity.this);

        img.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        img.setImageBitmap(data.getBitmap());
        img.setScaleType(ImageView.ScaleType.FIT_XY);
        img.setBackgroundResource(R.drawable.rectangle);
        img.setPadding(dp2px(2), dp2px(2), dp2px(2), dp2px(2));
        window.setContentView(img);
        img.setId(R.id.show_img_iv);
        img.setOnClickListener(this);
        img.setTag(data.getFile().getAbsolutePath());

        if(!isFinishing()) {
            window.showAtLocation(mContentFl, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0);
            mPopup = window;
        }
    }

    @Override
    public void onBackPressed() {
        if(mPopup != null && mPopup.isShowing()){
            mPopup.dismiss();
        } if(mWebview.canGoBack()){
            mWebview.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, MainActivity.this.getResources().getDisplayMetrics());
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mDisposable != null && !mDisposable.isDisposed()){
            mDisposable.dispose();
        }
    }

    @Override
    public void onShot(String imagePath) {
        if(isFinishing()){
            return;
        }
        mDisposable = ScreenShotUtil.execute(MainActivity.this, mWebview,  new Consumer<ShotPicData>() {
            @Override
            public void accept(ShotPicData data) throws Exception {
                showPopupBitmap(data);
            }
        }, null);
    }

    @Override
    public void onClick(View v) {
        if(R.id.go_btn == v.getId()){
            goUrl();
        } else if(R.id.show_img_iv == v.getId()){
            String path = (String) v.getTag();
            mPopup.dismiss();
            Intent intent = new Intent(MainActivity.this, BitmapShowActivity.class);

            intent.putExtra(BitmapShowActivity.EXTRA_BITMAP_PATH, path);
            startActivity(intent);
        }
    }

    private void goUrl(){
        if(mWebview != null){
            String url = mUrlTv.getText().toString();

            if(TextUtils.isEmpty(url)){
                Toast.makeText(this, "请填写跳转url", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!url.toLowerCase().startsWith("https") && !url.toLowerCase().startsWith("http")){
                url = "http://" + url;
            }

            mWebview.loadUrl(url);
            mUrlTv.setText("http://");
        }
    }
}
