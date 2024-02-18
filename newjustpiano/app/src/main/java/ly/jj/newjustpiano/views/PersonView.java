package ly.jj.newjustpiano.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.Arrays;

public class PersonView extends ViewGroup {
    public PersonView(Context context) {
        super(context);
    }

    public PersonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PersonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PersonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }


    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return super.checkLayoutParams(p);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int layoutWidth = Math.min(widthSize, 333);
        int layoutHeight = Math.min(heightSize, 500);
        layoutHeight = Math.min(layoutWidth * 3 / 2, layoutHeight);
        layoutWidth = Math.min(layoutWidth, layoutHeight * 2 / 3);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(resolveSize(layoutWidth, widthMeasureSpec), resolveSize(layoutHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            MarginLayoutParams childParams = (MarginLayoutParams) child.getLayoutParams();
            int cl = childParams.leftMargin;
            int ct = childParams.topMargin;
            int cr = childParams.rightMargin;
            int cb = childParams.bottomMargin;
            int cw = child.getMeasuredWidth();
            int ch = child.getMeasuredHeight();
            if ((cl + ct + cr + cb) != 0 & (((cl + cr) == Math.max(cl, cr)) && ((ct + cb) == Math.max(ct, cb)))) {
                System.out.println(i+Arrays.toString(new int[]{cl, ct, cr, cb}));
                int pw = (r - l - cw) / 2;
                int ph = (b - t - ch) / 2;
                int rl = 0, rt = 0, rr = 0, rb = 0;
                if (cl > 0) {
                    rl = l + cl;
                    rr = l + cl + cw;
                }
                if (cr > 0) {
                    rl = r - cr - cw;
                    rr = r - cr;
                }
                if (ct > 0) {
                    rt = t + ct;
                    rb = t + ct + ch;
                }
                if (cb > 0) {
                    rt = b - cb - ch;
                    rb = b - cb;
                }
                if (rl == 0 | rr == 0) {
                    rl = l + pw;
                    rr = r - pw;
                }
                if (rt == 0 | rb == 0) {
                    rt = t + ph;
                    rb = b - ph;
                }
                child.layout(rl, rt, rr, rb);
            } else {
                child.layout(l + cl, t + ct, r - cr, b - cb);
            }
        }
    }
}
