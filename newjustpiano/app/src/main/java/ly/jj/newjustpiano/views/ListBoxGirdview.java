package ly.jj.newjustpiano.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class ListBoxGirdview extends GridView {
    public ListBoxGirdview(Context context) {
        super(context);
    }

    public ListBoxGirdview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListBoxGirdview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListBoxGirdview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //int x = (int) ev.getX();
        //int y = (int) ev.getY();
        //System.out.println("action:"+ev.getAction()+ ",x:" + x + ",y:" + y);
        return false;
        //return super.onInterceptTouchEvent(ev);
    }
}
