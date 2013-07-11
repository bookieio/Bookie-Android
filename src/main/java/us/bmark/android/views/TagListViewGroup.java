package us.bmark.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Set;

import us.bmark.android.R;

public class TagListViewGroup extends ViewGroup {

    private static final String TAG = TagListViewGroup.class.getCanonicalName();
    private Set<String> tags;

    public TagListViewGroup(Context context) {
        super(context);
    }

    public TagListViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void recreateTagViews() {
        removeAllViews();
        for (final String tag : tags) {
            final TextView child =
                    (TextView)
                    LayoutInflater.from(getContext()).inflate(R.layout.bookmark_tag,null);
            child.setText(tag);
            child.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    deleteTag(((TextView)view).getText().toString());
                    return true;
                }
            });

            LayoutParams params = child.getLayoutParams();
            if (params == null) {
                params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            } else {
                params.width = LayoutParams.WRAP_CONTENT;
                params.height = LayoutParams.WRAP_CONTENT;
            }
            child.setLayoutParams(params);
            addView(child);
        }
    }

    private void deleteTag( String s) {
        tags.remove(s);

    }

    @Override
    protected void onSizeChanged(int horiz, int vert, int horizOld, int verOld) {
        super.onSizeChanged(horiz, vert, horizOld, verOld);
        Log.v(TAG, String.format("onSizeChanged(%d,%d,%d,%d)", horiz, vert, horizOld, verOld));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.v(TAG, "onDraw");

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.v(TAG, String.format("onLayout(%s,%d,%d,%d,%d)", Boolean.toString(changed), l, t, r, b));


        final int width = r - l;
        final int height = b - t;
        final int childCount = getChildCount();
        boolean[] done = new boolean[childCount];

        Log.v(TAG,"Childcount is "+childCount);

        int currentRow = 0;
        int currentRowWidthUsed = l;
        int currentRowStart = t;
        int currentRowHeight = 0;
        boolean checked=true;
        while (!all(done)) {
            for (int i = 0; i < childCount; i++) {
                if (!done[i]) {
                    final View child = getChildAt(i);

                    if (!(child instanceof TextView)) {
                        done[i] = true;
                    } else {
                        TextView tView = (TextView) getChildAt(i);
                        final int childHeight = tView.getMeasuredHeight();

                        final int tagWidth = tView.getMeasuredWidth();
                        if (currentRowWidthUsed == 0 && tagWidth > width) {
                            currentRowHeight = childHeight > currentRowHeight ? childHeight : currentRowHeight;
                            tView.layout(currentRowWidthUsed, currentRowStart, currentRowWidthUsed+tagWidth,currentRowStart+currentRowHeight);
                            applyCheckStyle(tView, checked);
                            currentRowWidthUsed = tagWidth;
                            done[i] = true;
                        } else if (tagWidth + currentRowWidthUsed < width) {
                            currentRowHeight = childHeight > currentRowHeight ? childHeight : currentRowHeight;
                            child.layout(currentRowWidthUsed, currentRowStart, currentRowWidthUsed+tagWidth,currentRowStart+currentRowHeight);
                            applyCheckStyle(tView, checked);
                            currentRowWidthUsed+=tagWidth;
                            done[i] = true;
                            checked=!checked;
                        }
                    }
                }

            }
            currentRow++;
            currentRowWidthUsed=l;
            currentRowStart+=currentRowHeight;
            currentRowHeight=0;
            checked=currentRow%2==0;

        }

    }

    private void applyCheckStyle(TextView view, boolean checked) {
        if(checked) {
            view.setBackgroundColor(getResources().getColor(R.color.check_background));
            view.setTextColor(getResources().getColor(R.color.check_text));
        }
    }

    private boolean all(boolean[] bits) {
        for (boolean bit : bits) {
            if (!bit) return false;
        }
        return true;
    }


    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Log.v(TAG, "onMeasure -- width: " + MeasureSpec.toString(widthMeasureSpec) + " height: " + MeasureSpec.toString(heightMeasureSpec));

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int calculatedWidth = widthMode == MeasureSpec.UNSPECIFIED ? 800 : width;
        int calculatedHeight = heightMode == MeasureSpec.UNSPECIFIED ? 800 : height;

        setMeasuredDimension(calculatedWidth, calculatedHeight);


        View[] children = new View[getChildCount()];
        boolean[] done = new boolean[children.length];

        for (int i = 0; i < children.length; i++) {
            children[i] = getChildAt(i);
        }

        for (View child : children) {
            if (child != null) {
                int childMeasureWidth = MeasureSpec.makeMeasureSpec(calculatedWidth, MeasureSpec.AT_MOST);
                int childMeasureHeight = MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.AT_MOST);
                child.measure(childMeasureWidth, childMeasureHeight);
            }
        }


    }
}
