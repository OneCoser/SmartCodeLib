package ch.smart.code.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.jakewharton.rxbinding3.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import ch.smart.code.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class ClearEditText extends AppCompatEditText {
    
    private static final int DRAWABLE_LEFT = 0;
    private static final int DRAWABLE_TOP = 1;
    private static final int DRAWABLE_RIGHT = 2;
    private static final int DRAWABLE_BOTTOM = 3;
    
    private Drawable mClearDrawable;
    
    private Disposable disposable;
    
    public ClearEditText(Context context) {
        super(context);
        init();
    }
    
    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mClearDrawable = getCompoundDrawables()[DRAWABLE_RIGHT];
                if (mClearDrawable == null) {
                    mClearDrawable = ContextCompat.getDrawable(getContext(), R.drawable.public_edit_clear);
                }
                disposable = RxTextView.textChanges(ClearEditText.this)
                    .skipInitialValue()
                    .throttleLatest(100, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(text -> setClearIconVisible(hasFocus() && text.length() > 0), Timber::e);
                setOnFocusChangeListener((v, hasFocus) -> setClearIconVisible(hasFocus && length() > 0));
            }
        }, 500);
    }
    
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Drawable drawable = getCompoundDrawables()[DRAWABLE_RIGHT];
            if (drawable != null && event.getX() <= (getWidth() - getPaddingRight())
                && event.getX() >= (getWidth() - getPaddingRight() - drawable.getBounds().width())) {
                setText("");
            }
        }
        return super.onTouchEvent(event);
    }
    
    private void setClearIconVisible(boolean visible) {
        if (isInEditMode()) {
            return;
        }
        setCompoundDrawablesWithIntrinsicBounds(getCompoundDrawables()[DRAWABLE_LEFT], getCompoundDrawables()[DRAWABLE_TOP],
            visible ? mClearDrawable : null, getCompoundDrawables()[DRAWABLE_BOTTOM]);
    }

}
