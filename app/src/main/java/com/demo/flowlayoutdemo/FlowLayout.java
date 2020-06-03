package com.demo.flowlayoutdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hao
 *  自定义流式布局
 */

public class FlowLayout extends ViewGroup {

    private Line mLine = null;
    public static final int DEFAULT_SPACING = 20;
    //所有的子控件
    private SparseArray<View> mViews;
    /**
     * 横向间隔
     */
    private int mHorizontalSpacing = DEFAULT_SPACING;
    /**
     * 纵向间隔
     */
    private int mVerticalSpacing = DEFAULT_SPACING;

    /**
     * 当前行已用的宽度，由子View宽度加上横向间隔
     */
    private int mUsedWidth = 0;
    /**
     * 代表每一行的集合
     */
    private final List<Line> mLines = new ArrayList<Line>();
    //子View的对齐方式
    private int isAlignByCenter = 1;

    /**
     * 最大的行数
     */
    private int mMaxLinesCount = Integer.MAX_VALUE;
    /**
     * 是否需要布局，只用于第一次
     */
    boolean mNeedLayout = true;



    public FlowLayout(Context context) {
        this(context,null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 计算子View摆放的位置
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */

//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        if (!mNeedLayout && changed){
//            mNeedLayout = false;
//            int left = getPaddingLeft();//获取最初的左上点
//            int top = getPaddingTop();
//            int count = mLines.size();
//            for (int i = 0; i < count; i++) {
//                Line line = mLines.get(i);
//                line.LayoutView(left,top);//摆放每一行中子View的位置
//                top +=line.mHeight+ mVerticalSpacing;//为下一行的top赋值
//            }
//        }
//    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int left = getPaddingLeft();//获取最初的左上点
            int top = getPaddingTop();
            int count = mLines.size();
            for (int i = 0; i < count; i++) {
                Line line = mLines.get(i);
                line.LayoutView(left, top);//摆放每一行中子View的位置
                top += line.mHeight + mVerticalSpacing;//为下一行的top赋值
            }
        }
    }


    /**
     * 计算自身显示在页面上的大小
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingRight() - getPaddingLeft();
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        restoreLine();// 还原数据，以便重新记录
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                break;
            }
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(sizeWidth, modeWidth == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : modeWidth);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(sizeHeight, modeHeight == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : modeHeight);
            // 测量child
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            if (mLine == null) {
                mLine = new Line();
            }
            int measuredWidth = child.getMeasuredWidth();
            mUsedWidth += measuredWidth;// 增加使用的宽度
            if (mUsedWidth < sizeWidth) { //当本行的使用宽度小于行总宽度的时候直接加进line里面
                mLine.addView(child);
                mUsedWidth += mHorizontalSpacing;// 加上间隔
                if (mUsedWidth >= sizeWidth){
                    if (!newLine()){
                        break;
                    }
                }
            }else {// 使用宽度大于总宽度。需要换行
                if (mLine.getViewCount() == 0){//如果这行一个View也没有超过也得加进去,保证一行最少有一个View
                    mLine.addView(child);
                    if (!newLine()) {// 换行
                        break;
                    }
                }else {
                    if (!newLine()) {// 换行
                        break;
                    }
                    mLine.addView(child);
                    mUsedWidth += measuredWidth + mHorizontalSpacing;
                }
            }
        }
        if (mLine !=null && mLine.getViewCount() > 0 && !mLines.contains(mLine)){
            mLines.add(mLine);
        }
        int totalHeight = 0;
        final int linesCount = mLines.size();
        for (int i = 0; i < linesCount; i++) {// 加上所有行的高度
            totalHeight += mLines.get(i).mHeight;
        }
        totalHeight += mVerticalSpacing * (linesCount - 1);// 加上所有间隔的高度
        totalHeight += getPaddingTop() + getPaddingBottom();// 加上padding
        // 设置布局的宽高，宽度直接采用父view传递过来的最大宽度，而不用考虑子view是否填满宽度，因为该布局的特性就是填满一行后，再换行
        // 高度根据设置的模式来决定采用所有子View的高度之和还是采用父view传递过来的高度
        setMeasuredDimension(totalWidth, resolveSize(totalHeight, heightMeasureSpec));
    }


    private void restoreLine() {
        mLines.clear();
        mLine = new Line();
        mUsedWidth = 0;
    }


    /**
     * 新增加一行
     */
    private boolean newLine() {
        mLines.add(mLine);
        if (mLines.size() < mMaxLinesCount) {
            mLine = new Line();
            mUsedWidth = 0;
            return true;
        }
        return false;
    }


    class Line{
        int mWidth = 0;// 该行中所有的子View累加的宽度
        int mHeight = 0;// 该行中所有的子View中高度的那个子View的高度
        List<View> views = new ArrayList<>();

        public int getViewCount() {
            return views.size();
        }

        public void addView(View view) {// 往该行中添加一个
            views.add(view);
            mWidth += view.getMeasuredWidth();
            int childHeight = view.getMeasuredHeight();
            mHeight = mHeight < childHeight ? childHeight : mHeight;//高度等于一行中最高的View
        }
        //摆放行中子View的位置
        public void LayoutView(int l, int t) {
            int left = l;
            int top = t;
            int count = getViewCount();
            int layoutWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();//行的总宽度
            //剩余的宽度，是除了View和间隙的剩余空间
            int surplusWidth = layoutWidth - mWidth - mHorizontalSpacing * (count - 1);
            if (surplusWidth >= 0) {
                for (int i = 0; i < count; i++) {
                    final View view = views.get(i);
                    int childWidth = view.getMeasuredWidth();
                    int childHeight = view.getMeasuredHeight();
                    //计算出每个View的顶点，是由最高的View和该View高度的差值除以2
                    int topOffset = (int) ((mHeight - childHeight) / 2.0 + 0.5);
                    if (topOffset < 0) {
                        topOffset = 0;
                    }
                    view.layout(left,top+topOffset,left+childWidth,top + topOffset + childHeight);
                    left += childWidth + mVerticalSpacing;//为下一个View的left赋值
                }
            }
        }

    }


}
