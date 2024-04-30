package at.aau.anti_mon.client;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class CustomView_Gamefield extends ConstraintLayout {
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float FACTOR = 1.0f;
    private float previousX, previousY;

    public CustomView_Gamefield(@NonNull Context context) {
        super(context);
        initView();
    }

    public CustomView_Gamefield(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        Log.d("Detection", "onTouch event was detected! custom View gamefield");

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                previousX = event.getX();
                previousY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - previousX;
                float deltaY = event.getY() - previousY;
                scrollBy((int) -deltaX, (int) -deltaY);

                previousX = event.getX();
                previousY = event.getY();
                break;
        }

        return true;
    }

    public void initView() {
        inflate(getContext(), R.layout.custom_view_gamefield, this);
        scaleGestureDetector = new ScaleGestureDetector(this.getContext(), new ScaleListener());
        gestureDetector = new GestureDetector(this.getContext(), new GestureListener());
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            FACTOR *= detector.getScaleFactor();
            FACTOR = Math.max(1.0f, Math.min(FACTOR, 10.f));
            setScaleX(FACTOR);
            setScaleY(FACTOR);
            Log.d("Detection", "Scaling detected" + FACTOR);
            return true;
        }

    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

}


