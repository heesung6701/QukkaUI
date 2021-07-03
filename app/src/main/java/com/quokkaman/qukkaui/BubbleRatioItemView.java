package com.quokkaman.qukkaui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class BubbleRatioItemView extends View {

    @Nullable
    private String value;
    @Nullable
    private final ColorStateList valueTextColorStateList;
    @NonNull
    private final Paint valuePaint;

    @Nullable
    private String label;
    @Nullable
    private final ColorStateList labelTextColorStateList;
    @NonNull
    private final Paint labelPaint;

    private float labelPadding;

    @Nullable
    private final ColorStateList bubbleColorStateList;
    @NonNull
    private final Paint bubblePaint;

    @NonNull
    private final Rect touchRect = new Rect();

    @NonNull
    private final Rect valueTextRect = new Rect();

    @NonNull
    private final Rect labelTextRect = new Rect();

    public BubbleRatioItemView(Context context) {
        this(context, null);
    }

    public BubbleRatioItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleRatioItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BubbleRatioItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BubbleRatioItemView);

        try {
            value = typedArray.getString(R.styleable.BubbleRatioItemView_value);
            valueTextColorStateList = typedArray.getColorStateList(R.styleable.BubbleRatioItemView_valueTextColor);

            label = typedArray.getString(R.styleable.BubbleRatioItemView_label);
            labelTextColorStateList = typedArray.getColorStateList(R.styleable.BubbleRatioItemView_labelTextColor);
            labelPadding = typedArray.getDimension(R.styleable.BubbleRatioItemView_labelPadding, 0.0f);
            bubbleColorStateList = typedArray.getColorStateList(R.styleable.BubbleRatioItemView_bubbleColor);
        } finally {
            typedArray.recycle();
        }

        valuePaint = new Paint();
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(valueTextColorStateList != null ? valueTextColorStateList.getDefaultColor() : Color.WHITE);
        valuePaint.setTextSize(48);

        bubblePaint = new Paint();
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setColor(bubbleColorStateList != null ? bubbleColorStateList.getDefaultColor() : ContextCompat.getColor(getContext(), android.R.color.primary_text_light));

        labelPaint = new Paint();
        labelPaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(labelTextColorStateList != null ? labelTextColorStateList.getDefaultColor() : Color.BLACK);
        labelPaint.setTextSize(48);

        if (label != null) {
            labelPaint.getTextBounds(label, 0, label.length(), labelTextRect);
        }

        if (value != null) {
            valuePaint.getTextBounds(value, 0, value.length(), valueTextRect);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // TODO This is Not Support Vertical Orientation with Weight on LinearLayout
        //      only Calculate height by suggestion width
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.AT_MOST:
                widthSize = Math.min(getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth(), widthSize);
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                widthSize = widthMeasureSpec;
                break;
        }

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.AT_MOST:
                heightSize = Math.min(getPaddingTop() + getPaddingBottom() + getSuggestedMinimumHeight() + widthSize + labelTextRect.height() + (int)labelPadding, heightSize);
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                heightSize = heightMeasureSpec;
                break;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();

        int bubbleCenterX = (width) / 2 + getPaddingLeft();
        int bubbleCenterY = bubbleCenterX + getPaddingTop();
        int bubbleRadius = width / 2;

        canvas.drawCircle(bubbleCenterX, bubbleCenterY, bubbleRadius, bubblePaint);
        if (value != null) {
            int shiftTextHalfHeight = valueTextRect.height() / 2;
            canvas.drawText(value, bubbleCenterX, bubbleCenterY + shiftTextHalfHeight, valuePaint);
        }
        if (label != null) {
            canvas.drawText(label, bubbleCenterX, bubbleCenterY + bubbleRadius + labelTextRect.height() + labelPadding, labelPaint);
        }
        touchRect.set(bubbleCenterX - bubbleRadius, bubbleCenterY - bubbleRadius, bubbleCenterX + bubbleRadius, bubbleCenterY + bubbleRadius);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(touchRect.contains(x,y)) {
                    final int[] pressedStateSet = new int[]{android.R.attr.state_pressed};
                    updatePaintColor(valuePaint, valueTextColorStateList, pressedStateSet);
                    updatePaintColor(labelPaint, labelTextColorStateList, pressedStateSet);
                    updatePaintColor(bubblePaint, bubbleColorStateList, pressedStateSet);
                    invalidate();
                    return true;
                }
                return false;
            case MotionEvent.ACTION_UP:
                if(touchRect.contains(x,y)) {
                    performClick();
                }
            case MotionEvent.ACTION_CANCEL:
                final int[] emptyStateSet = new int[]{};
                updatePaintColor(valuePaint, valueTextColorStateList, emptyStateSet);
                updatePaintColor(labelPaint, labelTextColorStateList, emptyStateSet);
                updatePaintColor(bubblePaint, bubbleColorStateList, emptyStateSet);
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        Toast.makeText(getContext(), "Clicked", Toast.LENGTH_SHORT).show();
        return super.performClick();
    }

    public void setLabelPadding(float labelPadding) {
        this.labelPadding = labelPadding;
    }

    public void setLabel(@Nullable String label) {
        this.label = label;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    private void updatePaintColor(@NonNull Paint paint, @Nullable ColorStateList colorStateList, @NonNull int[] stateSet) {
        if(colorStateList == null) return;
        paint.setColor(colorStateList.getColorForState(stateSet, colorStateList.getDefaultColor()));
    }
}
