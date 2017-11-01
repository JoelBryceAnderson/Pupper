package joelbryceanderson.com.pupperkeyboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.daprlabs.cardstack.SwipeDeck;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import joelbryceanderson.com.pupperkeyboard.adapters.SwipeDeckAdapter;
import joelbryceanderson.com.pupperkeyboard.models.Pupper;
import joelbryceanderson.com.pupperkeyboard.services.PupperService;
import joelbryceanderson.com.pupperkeyboard.services.ServiceFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    PupperService mPupperService;
    SwipeDeck mPupperCards;
    SwipeDeckAdapter mSwipeDeckAdapter;
    FloatingActionButton mSharePupper;
    TextView mGoodBoy;
    Target mTarget;
    TextView mDownBoy;
    int mCurrentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-1140621218981804~2710138775");

        mSharePupper = (FloatingActionButton) findViewById(R.id.share_pupper);
        mGoodBoy = (TextView) findViewById(R.id.good_boy_text);
        mDownBoy = (TextView) findViewById(R.id.down_boy_text);
        mPupperCards = (SwipeDeck) findViewById(R.id.swipe_deck);

        mSharePupper.setOnClickListener(view -> sharePupper());

        mPupperService = ServiceFactory.createRetrofitService(
                PupperService.class, PupperService.PUPPER_ENDPOINT);

        mSwipeDeckAdapter = new SwipeDeckAdapter(new ArrayList<>(), this);

        mPupperCards.setAdapter(mSwipeDeckAdapter);
        mPupperCards.setEventCallback(onSwipe());

        AdView mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        loadPupper();
        loadPupper();
        loadPupper();
    }

    private SwipeDeck.SwipeEventCallback onSwipe() {
        return new SwipeDeck.SwipeEventCallback() {
            @Override
            public void cardSwipedLeft(int position) {
                animatePopup(mDownBoy);
                loadPupper();
                mCurrentPosition = position + 1;
            }

            @Override
            public void cardSwipedRight(int position) {
                animatePopup(mGoodBoy);
                loadPupper();
                mCurrentPosition = position + 1;
            }

            @Override
            public void cardsDepleted() {
                loadPupper();
            }

            @Override
            public void cardActionDown() {

            }

            @Override
            public void cardActionUp() {

            }
        };
    }


    private void sharePupper() {
        Log.e("SHARED",mSwipeDeckAdapter.getItem(mCurrentPosition) );
        mTarget = onPupperBitmapped();

        Picasso.with(MainActivity.this)
                .load(mSwipeDeckAdapter.getItem(mCurrentPosition))
                .resize(500,500)
                .onlyScaleDown()
                .centerInside()
                .into(mTarget);
    }

    private Observable<Uri> getLocalUri(Bitmap bitmap) {
        return Observable.fromCallable(() -> getLocalBitmapUri(bitmap));
    }

    private Target onPupperBitmapped(){
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                createShareDialog(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                showErrorToast();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
    }

    private void createShareDialog(Bitmap bitmap) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        mSharePupper.hide();

        getLocalUri(bitmap)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri->{
                    showShareDialog(uri, bitmap, intent);
                }, throwable -> {
                    Log.e(TAG, throwable.getLocalizedMessage());
                    showErrorToast();
                });
    }

    private void showShareDialog(Uri uri, Bitmap bitmap, Intent intent) {
        mSharePupper.show();
        intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
        startActivity(Intent.createChooser(intent, getString(R.string.share_pupper)));
    }

    private Uri getLocalBitmapUri(Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    "pupper" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    /**
     * Fetches pupper from the backend, displays on completion
     */
    private void loadPupper() {
        mPupperService.getRandomPupper()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showPupper, throwable -> {
                    Log.e(TAG, throwable.getLocalizedMessage());
                    showErrorToast();
                });
    }

    private void showPupper(List<Pupper> puppers) {
        String pupperUrl = puppers.get(0).getData().getChildren().get(0).getData().getUrl();
        if (!pupperUrl.endsWith(".gif") || !pupperUrl.endsWith(".gifv")) {
            mSwipeDeckAdapter.addPupper(pupperUrl);
        } else {
            loadPupper();
        }
    }

    private void showErrorToast() {
        Toast.makeText(MainActivity.this, R.string.error_loading_text, Toast.LENGTH_LONG).show();
    }

    private void animatePopup(TextView popUp) {
        popUp.bringToFront();
        popUp.setAlpha(1);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(300);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(300);
        fadeOut.setDuration(300);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                popUp.setAlpha(0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        popUp.startAnimation(animation);
    }

    public void goodBoy(View v) {
        mPupperCards.swipeTopCardRight(300);
        animatePopup(mGoodBoy);
    }

    public void downBoy(View v) {
        mPupperCards.swipeTopCardLeft(300);
        animatePopup(mDownBoy);
    }
}
