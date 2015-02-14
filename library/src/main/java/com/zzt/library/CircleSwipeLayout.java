package com.zzt.library;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by zzt on 2015/2/8.
 */
public class CircleSwipeLayout  extends ViewGroup{

    private static CircleSwipeLayout mCircleSwipeLayout;
    protected VelocityTracker mVelocityTracker;
    protected BuildLayerFrameLayout mContentContainer;
    public static final int STATE_CLOSED = 0;
    public static final int STATE_CLOSING = 1;
    public static final int STATE_DRAGGING = 2;
    public static final int STATE_OPENING = 4;
    public static final int STATE_OPEN = 8;
    protected static final int PEEK_DURATION = 800;
    private static final int DEFAULT_ANIMATION_DURATION = 800;
    protected int mMaxAnimationDuration = DEFAULT_ANIMATION_DURATION;
    protected int mLayoutState = STATE_CLOSED;
    protected int mMenuSize;
    protected int mTouchSize;
    protected int mTouchSlop;
    protected int mMaxVelocity;
    protected float mLastMotionX = -1;
    protected float mLastMotionY = -1;
    protected float mInitialMotionX;
    protected float mInitialMotionY;
    protected float mOffsetPixels;
    protected long mPeekDelay;
    protected boolean mIsDragging;
    protected boolean mBottomViewVisible = false;
    protected boolean mLayerTypeHardware;
    protected boolean mHardwareLayersEnabled = true;
    private boolean IsPeeking = false;
    public static final boolean USE_TRANSLATIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    private Scroller mScroller;
    private Scroller mPeekScroller;
    private Runnable mPeekStartRunnable;
    //protected OnInterceptMoveEventListener mOnInterceptMoveEventListener;
    private static final Interpolator SMOOTH_INTERPOLATOR = new DecelerateInterpolator();
    private final Runnable mDragRunnable = new Runnable() {
        @Override
        public void run() {
            postAnimationInvalidate();
        }
    };
    protected final Runnable mPeekRunnable = new Runnable(){
        @Override
        public void run() {
            peekDrawerInvalidate();
        }
    };

    private OnLayoutOpenListener mLayoutOpenListener;

    public interface OnLayoutOpenListener{
        public void onMenuOpen();
    }


    public CircleSwipeLayout(Context context) {
        this(context, null);
    }

    public CircleSwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        setWillNotDraw(false);
        setFocusable(false);

        this.mContentContainer = new NoClickThroughFrameLayout(context);
        this.mContentContainer.setId(R.id.md__content);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();

        mScroller = new Scroller(context, SMOOTH_INTERPOLATOR);
        mPeekScroller = new Scroller(context, SMOOTH_INTERPOLATOR);

        super.addView(mContentContainer, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private static CircleSwipeLayout createCircleSwipeLayout(Activity activity){
        CircleSwipeLayout circleSwipeLayout = new CircleSwipeLayout(activity);
        return circleSwipeLayout;
    }

    public static CircleSwipeLayout attach(Activity activity){
        mCircleSwipeLayout = createCircleSwipeLayout(activity);
        mCircleSwipeLayout.setId(R.id.md__drawer);//给菜单设置一个id,即平时xml里面指定的@+id,因为在这里使用java生成的,所以没有id
        attachToContent(activity, mCircleSwipeLayout);
        return mCircleSwipeLayout;
    }

    private static void attachToContent(Activity activity, CircleSwipeLayout circleSwipeLayout) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);

        decorView.removeAllViews();
        mCircleSwipeLayout.mContentContainer.addView(decorChild, decorChild.getLayoutParams());
        //mCircleSwipeLayout.addView(decorChild, decorChild.getLayoutParams());
        decorView.addView(mCircleSwipeLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override//这里width,height就是上面的onMeasure提供的
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r-l;
        final int height= b-t;
        if(USE_TRANSLATIONS){
            this.mContentContainer.layout(0, 0, width, height);
        }else{
            final int offsetPixels = (int)mOffsetPixels;
            this.mContentContainer.layout(offsetPixels, 0, width+offsetPixels, height);
        }
    }

    @Override//先于onLayout被调用,在这个函数里面要设置本类(ViewGroup的子类)里面的孩子的大小
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {//sdk说明:必须调用setMeasuredDimension,来保存width, heigh

        final int width = MeasureSpec.getSize(widthMeasureSpec);//返回屏幕的宽度  用getMeasuredWidth()也可以得到
        final int height = MeasureSpec.getSize(heightMeasureSpec);//返回屏幕的高度,不包括状态栏

        //这里设置主界面的大小
        final int contentWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, height);
        this.mContentContainer.measure(contentWidthMeasureSpec, contentHeightMeasureSpec);//设置mContentContainer应该占的尺寸

        //下面设置menu的大小
        int menuWidthMeasureSpec;
        int menuHeightMeasureSpec;

        menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, mMenuSize);
        menuHeightMeasureSpec= getChildMeasureSpec(widthMeasureSpec, 0, height);

        //mMenuContainer.measure(menuWidthMeasureSpec, menuHeightMeasureSpec);
        setMeasuredDimension(width, height);//在onMeasure里面必须要被调用的函数,用来保存尺寸
        updateTouchAreaSize();//在这里确定可以drag菜单的范围
    }

    protected int mActivePointerId = INVALID_POINTER;
    protected static final int INVALID_POINTER = -1;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        System.out.println("onInterceptTouchEvent");
        final int action = ev.getAction()&MotionEvent.ACTION_MASK;

        switch(action){
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                final boolean allowDrag = onDownAllowDrag((int) mLastMotionX, (int) mLastMotionY);
                mActivePointerId = ev.getPointerId(0);
                if (allowDrag) {
                    setLayoutState(mBottomViewVisible ? STATE_OPEN : STATE_CLOSED);
                    stopAnimation();
                    endPeek();
                    setPeekingState(false);
                    mIsDragging = false;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerId = mActivePointerId;
                if(activePointerId==INVALID_POINTER){
                    break;
                }
                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if(pointerIndex == -1){
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                    closeMenu(true);
                    return false;
                }
                final float x = ev.getX(pointerIndex);
                final float dx= x - mLastMotionX;
                final float y = ev.getY(pointerIndex);
                final float dy= y - mLastMotionY;
                if(checkTouchSlop(dx, dy)){
/*                    if(mOnInterceptMoveEventListener!=null&&(mTouchMode==TOUCH_MODE_FULLSCREEN||mBottomViewVisible)
                            &&canChildrenScroll((int)dx, (int)dy, (int)x, (int)y)){
                        //endDrag();
                        requestDisallowInterceptTouchEvent(true);
                        return false;
                    }*/
                    final boolean allowDrag = onMoveAllowDrag((int)x, (int)y, dx, dy);
                    if(allowDrag){
                        setLayoutState(STATE_DRAGGING);
                        System.out.println(" mIsDragging = true;");
                        mIsDragging = true;//允许拖动之后,touch事件就被送到onTouchEvent
                        mLastMotionX = x;
                        mLastMotionY = y;
                    }
                }
                break;
        }
        if(mVelocityTracker==null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);
        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        final int action = ev.getAction()&MotionEvent.ACTION_MASK;
        if(mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                System.out.println("ACTION_MOVE");
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if(pointerIndex == -1){
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    //有东西未填
                    return false;
                }
                if(!mIsDragging){
                    final float x = ev.getX(pointerIndex);
                    final float dx= x - mLastMotionX;
                    final float y = ev.getY(pointerIndex);
                    final float dy= y - mLastMotionY;
                    if(checkTouchSlop(dx, dy)){
                        final boolean allowDrawg = onMoveAllowDrag((int)x, (int)y, dx, dy);
                        if(allowDrawg){
                            setLayoutState(STATE_DRAGGING);
                            mIsDragging = true;
                            mLastMotionX = x;
                            mLastMotionY = y;
                        }else{
                            mInitialMotionX = x;
                            mInitialMotionY = y;
                        }
                    }
                }
                if(mIsDragging){
                    startLayoutAnimation();
                    final float x = ev.getX(pointerIndex);
                    float dx= x - mLastMotionX;
                    final float y = ev.getY(pointerIndex);
                    final float dy= y - mLastMotionY;
                    mLastMotionX = x;
                    mLastMotionY = y;
                    onMoveEvent(dx, dy);
                }
                break;
            case MotionEvent.ACTION_UP: {
                //Log.i("onTouchEvent", "ACTION_UP ACTION_CANCEL");
                int index = ev.findPointerIndex(mActivePointerId);
                index = index == -1?0:index;
                final int x = (int)ev.getX(index);
                final int y = (int)ev.getY(index);
                onUpEvent(x, y);
                mActivePointerId = INVALID_POINTER;
                mIsDragging = false;
                break;
            }
        }
        return true;
    }

    protected boolean onDownAllowDrag(int x, int y) {
        return (!mBottomViewVisible && mInitialMotionX<=mTouchSize
                ||mBottomViewVisible && mInitialMotionX >= mOffsetPixels);
    }

    protected boolean onMoveAllowDrag(int x, int y, float dx, float dy) {
        return (!mBottomViewVisible && mInitialMotionX <= mTouchSize && (dx > 0))
                || (mBottomViewVisible && x >= mOffsetPixels);
    }

    public void setPeekingState(boolean newState){
        IsPeeking = newState;
    }

    protected void initPeekScroller() {
        final int dx = 500 / 3;
        mPeekScroller.startScroll(0, 0, dx, 0, PEEK_DURATION);
    }

    public void stopAnimation() {
        removeCallbacks(mDragRunnable);
        mScroller.abortAnimation();
        stopLayerTranslation();
    }

    protected void startPeek() {
        initPeekScroller();
        startLayerTranslation();
        peekDrawerInvalidate();
    }

    public void endPeek() {
        removeCallbacks(mPeekStartRunnable);
        removeCallbacks(mPeekRunnable);
        stopLayerTranslation();
        IsPeeking = false;
    }

    private void postAnimationInvalidate(){
        if(this.mScroller.computeScrollOffset()){
            final int oldX = (int)mOffsetPixels;
            final int x = this.mScroller.getCurrX();

            if(x!=oldX)setOffsetPixels(x);
            if(x!=this.mScroller.getFinalX()){//还没完成,继续滚动
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    postOnAnimation(mDragRunnable);//这个runnable就是为了重复调用这个函数
                else
                    post(mDragRunnable);
                return ;
            }else if(x==this.mScroller.getFinalX()
                    &&mOffsetPixels!=0&&mLayoutOpenListener!=null){//菜单完全打开时候可以在这里触发相关业务
                mLayoutOpenListener.onMenuOpen();
            }
        }
        completeAnimation();
    }

    private void peekDrawerInvalidate() {
        if (mPeekScroller.computeScrollOffset()) {
            //Log.i(tag, "computeScrollOffset");
            final int oldX = (int) mOffsetPixels;
            final int x = mPeekScroller.getCurrX();
            if (x != oldX) setOffsetPixels(x);

            if (!mPeekScroller.isFinished()) {//还没完成滚动,就继续,所以可以反复调用这个函数
                //postOnAnimation(mPeekRunnable);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    postOnAnimation(mPeekRunnable);//这个runnable就是为了重复调用这个函数
                else
                    post(mPeekRunnable);
                return;
            } else if (mPeekDelay > 0) {
                mPeekStartRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startPeek();
                    }
                };
                postDelayed(mPeekStartRunnable, mPeekDelay);
            }
        }
        completePeek();
    }

    private void completePeek() {
        mPeekScroller.abortAnimation();
        setOffsetPixels(0);
        setLayoutState(STATE_CLOSED);
        stopLayerTranslation();
    }

    private void completeAnimation(){
        this.mScroller.abortAnimation();
        final int finalX = this.mScroller.getFinalX();
        setOffsetPixels(finalX);
        stopLayerTranslation();
    }

    protected void startLayerTranslation(){
        if(USE_TRANSLATIONS&&mHardwareLayersEnabled&&!mLayerTypeHardware){
            mLayerTypeHardware = true;
            mContentContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    protected void stopLayerTranslation(){
        if(mLayerTypeHardware){
            mLayerTypeHardware = false;
            mContentContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    protected void onUpEvent(int x, int y) {
        final int offsetPixels = (int) mOffsetPixels;

        if (mIsDragging) {
            mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
            final int initialVelocity = (int) getXVelocity(mVelocityTracker);
            mLastMotionX = x;
            animateOffsetTo(initialVelocity > 0 ? mMenuSize : 0, initialVelocity, true);
        } else if (mBottomViewVisible && x > offsetPixels) {
            closeMenu();
        }
    }

    protected float getXVelocity(VelocityTracker velocityTracker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return velocityTracker.getXVelocity(mActivePointerId);
        }
        return velocityTracker.getXVelocity();
    }

    public void animateOffsetTo(int position, int velocity, boolean animate){
		/*final int startX = (int)mOffsetPixels;
		final int dx = position - startX;

		if(dx==0||!animate){
			setOffsetPixels(position);
			return ;
		}
		int duration;
		animateOffsetTo(position, 1400);*/
        endDrag();
        endPeek();

        final int startX = (int) mOffsetPixels;
        final int dx = position - startX;
        if (dx == 0 || !animate) {
            setOffsetPixels(position);
            setLayoutState(position == 0 ? STATE_CLOSED : STATE_OPEN);
            stopLayerTranslation();
            return;
        }
        int duration;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000.f * Math.abs((float) dx / velocity));
        } else {
            duration = (int) (600.f * Math.abs((float) dx / mMenuSize));
        }

        duration = Math.min(duration, mMaxAnimationDuration);
        animateOffsetTo(position, duration);
    }

    protected void animateOffsetTo(int position, int duration){
        final int startX = (int)mOffsetPixels;
        final int dx = position - startX;
        if(dx>0){//opening
            setLayoutState(STATE_OPENING);
            this.mScroller.startScroll(startX, 0,  dx,  0, duration);
        }else{//closing
            setLayoutState(STATE_CLOSING);
            this.mScroller.startScroll(startX, 0, dx, 0, duration);
        }
        startLayerTranslation();
        postAnimationInvalidate();//第一次投放
    }

    protected void endDrag() {
        mIsDragging = false;

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void closeMenu() {
        closeMenu(true);
    }

    public void closeMenu(boolean animate) {
        animateOffsetTo(0, 0, animate);
    }

    protected boolean checkTouchSlop(float dx, float dy) {
        return Math.abs(dx) > mTouchSlop && Math.abs(dx) > Math.abs(dy);
    }

    protected void updateTouchAreaSize() {
        mTouchSize = getMeasuredWidth();
        mMenuSize = mTouchSize/2;
    }

    protected void onMoveEvent(float dx, float dy){
        setOffsetPixels(Math.min(Math.max(mOffsetPixels + dx, 0), mMenuSize));
    }

    protected void setOffsetPixels(float offsetPixels) {
        final int oldOffset = (int) mOffsetPixels;
        final int newOffset = (int) offsetPixels;

        mOffsetPixels = offsetPixels;
        //System.out.println("newOffset:"+newOffset+" oldOffset:"+oldOffset);
        if (newOffset != oldOffset) {
            onOffsetPixelsChanged(newOffset);
            mBottomViewVisible = newOffset != 0;
        }
    }

    protected void onOffsetPixelsChanged(int offsetPixels) {
        //System.out.println("onOffsetPixelsChanged:"+offsetPixels);
        if (USE_TRANSLATIONS) {
            mContentContainer.setTranslationX(offsetPixels);
        } else {
            mContentContainer.offsetLeftAndRight(offsetPixels - getLeft());
        }
        invalidate();
    }

    protected void setLayoutState(int state){
        if(state != mLayoutState){
            final int oldState = mLayoutState;
            mLayoutState = state;
        }
    }

}
