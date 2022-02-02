package com.example.midnight_chevves.Customer.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.midnight_chevves.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class AboutUsFragment extends Fragment {

    private ImageView mImage;
    private View mPresentation;
    private int mImageHeight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about_us, container, false);

        mImage = (ImageView) v.findViewById(R.id.image);
        mImageHeight = mImage.getLayoutParams().height;

        ((TrackingScrollView) v.findViewById(R.id.scroller)).setOnScrollChangedListener(
                new TrackingScrollView.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged(TrackingScrollView source, int l, int t, int oldl, int oldt) {
                        handleScroll(source, t);
                    }
                }
        );

        mPresentation = v.findViewById(R.id.presentation);
       centerViewVertically(mPresentation);
        return v;
    }

    private void handleScroll(TrackingScrollView source, int top) {
        int scrolledImageHeight = Math.min(mImageHeight, Math.max(0, top));

        ViewGroup.MarginLayoutParams imageParams = (ViewGroup.MarginLayoutParams) mImage.getLayoutParams();
        int newImageHeight = mImageHeight - scrolledImageHeight;
        if (imageParams.height != newImageHeight) {
            // Transfer image height to margin top
            imageParams.height = newImageHeight;
            imageParams.topMargin = scrolledImageHeight;

            // Invalidate view
            mImage.setLayoutParams(imageParams);
        }
    }

    private static void centerViewVertically(View view) {
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.setTranslationY(-v.getHeight() / 2);
                v.removeOnLayoutChangeListener(this);
            }
        });
    }

}
