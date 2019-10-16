package com.luoj.airdroid.view.draggableview;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * @author LuoJ
 * @date 2013-8-14
 * @package j.android.library.view - MoveButton.java
 * @Description 自定义可拖拽悬浮控件
 */
public class DraggableSuspendLayout extends FrameLayout implements DragMotionProxy.OnDragListener {

	private WindowManager wm;// 悬浮窗口相关
	private WindowManager.LayoutParams wmParams;// 窗口参数

	private boolean isCanDrag=true;
	private boolean isAutoAttachEdge;//自动贴到边缘
	private boolean isShowing = false;// 是否显示中
	
	private float showX = 0;// 默认显示的X坐标
	private float showY = 120;// 默认显示的Y坐标


	/**
	 * 拖拽相关
	 */
	private DragMotionProxy mDragMotionProxy;
	private float endX;// 抬起后X坐标
	private float endY;// 抬起后Y坐标
	private float offsetX;// 点击处距控件的X轴间距
	private float offsetY;// 点击处距控件的Y轴间距

	/**
	 * 构造函数
	 * @param context
	 */
	public DraggableSuspendLayout(final Context context) {
		super(context);
		init();
		initWindowParam();
	}

	/**
	 * 构造函数
	 * @param context
	 * @param attrs
	 */
	public DraggableSuspendLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		initWindowParam();
	}

	private void init(){
		mDragMotionProxy=new DragMotionProxy();
		mDragMotionProxy.set(this, this);
	}
	
	private void initWindowParam() {
//		setOrientation(LinearLayout.VERTICAL);
		// 获取WindowManager
		wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		// 设置LayoutParams(全局变量）相关参数
		wmParams = new WindowManager.LayoutParams();
		wmParams.type = WindowManager.LayoutParams.TYPE_PHONE; // 设置window type
		wmParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
		// 不设置这个弹出框的透明遮罩显示为黑色
        //wmParams.format = PixelFormat.TRANSLUCENT;
		/* 
		 * http://www.cnblogs.com/mengdd/p/3824782.html
		 * 设置Window flag
		 * 如果设置了FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
		 * FLAG_NOT_TOUCH_MODAL 不阻塞事件传递到后面的窗口
		 */
//		wmParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
//		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
//				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
//				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//				| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
//		wmParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		// 以屏幕左上角为原点，设置x、y初始值
		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.gravity = Gravity.TOP | Gravity.LEFT;
		// 设置悬浮窗口长宽数据
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
	}

    public void setFlag(int flag){
        wmParams.flags = flag;
    }

	public void setWidth(int width){
		wmParams.width = width;
	}
	
	public void setHeight(int height){
		wmParams.height = height;
	}
	
	public void setFullScreen(){
		setWidth(-1);
		setHeight(-1);
	}
	
	public void setGravity(int gravity){
		wmParams.gravity = gravity;
	}
	
	/**
	 * 显示悬浮控件
	 * @param x
	 * @param y
	 */
	private void showWindowView(float x, float y) {
		if (null != this) {
			WindowManager.LayoutParams params = (android.view.WindowManager.LayoutParams) this.getLayoutParams();
			params.y = (int) y - (this.getHeight() / 2);
			params.x = (int) x;
			this.setLayoutParams(params);
			if (null != wm) {
				wm.updateViewLayout(this, wmParams);
			}
		}
	}

	/**
	 * 初始化相对于屏幕的位置
	 * @param x
	 * @param y
	 */
	public void initLocationOfScreen(int x,int y){
		this.showX=x;
		this.showY=y;
	}
	
	/**
	 * 显示窗口.
	 */
	public synchronized void show() {
		if (!isShowing) {
			wm.addView(this, wmParams);
			showWindowView(showX, showY);
			isShowing = true;
		}else{
			Log.e("","悬浮布局已经显示，不必再次调用show()");
		}
	}

	public void showAtScreenCenter(){
//		DisplayMetrics outMetrics=new DisplayMetrics();
//		wm.getDefaultDisplay().getMetrics(outMetrics);
//		int screenWidth = outMetrics.widthPixels;
//		int screenHeight=outMetrics.heightPixels;
//		//
//		int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED); 
//		int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED); 
//		measure(w, h); 
//		int layoutHeight = getMeasuredHeight(); 
//		int layoutWidth = getMeasuredWidth();
//		showX=(screenWidth/2)-(layoutWidth/2);
//		showY=(screenHeight/2)-(layoutHeight/2);
		show();
	}
	
	/**
	 * 显示窗口在指定位置.
	 * 强制调整位置显示
	 * @param x
	 * @param y
	 */
	public void show(int x,int y){
		dismiss();
		if (!isShowing) {
			wm.addView(this, wmParams);
			showWindowView(x, y);
			isShowing = true;
		}
	}
	
	/**
	 * 隐藏悬浮布局.
	 */
	public void dismiss() {
		if (isShowing) {
			wm.removeView(this);
			isShowing=false;
			if(null!=mOnDragLayoutDismissListener)mOnDragLayoutDismissListener.onDismiss();
		}
	}

	/**
	 * 判断悬浮布局是否显示
	 * @return
	 */
	public boolean isShow() {
		return isShowing;
	}

	/**
	 * 是否可以拖拽
	 * @return
	 */
	public boolean isCanDrag() {
		return isCanDrag;
	}

	/**
	 * 设置是否可拖拽
	 * @param
	 */
	public void setCanDrag(boolean isCanDrag) {
		this.isCanDrag = isCanDrag;
        if (isCanDrag){
            if(null==mDragMotionProxy){
                mDragMotionProxy=new DragMotionProxy();
                mDragMotionProxy.set(this, this);
            }
        }else{
            if(null!=mDragMotionProxy){
                mDragMotionProxy.unInitializ();
                mDragMotionProxy=null;
            }
        }
	}

	/**
	 * 是否在拖拽完松开后，依附到屏幕边缘.
	 * @return
	 */
	public boolean isAutoAttachEdge() {
		return isAutoAttachEdge;
	}

	/**
	 * 设置是否在拖拽完松开后，依附到屏幕边缘.
	 * @param isAutoAttachEdge
	 */
	public void setAutoAttachEdge(boolean isAutoAttachEdge) {
		this.isAutoAttachEdge = isAutoAttachEdge;
	}

	/**
	 * 移动悬浮布局
	 * @param x
	 * @param y
	 */
    public void moveWindowView(float x, float y) {
		if (null != this) {
			WindowManager.LayoutParams params = (android.view.WindowManager.LayoutParams) this.getLayoutParams();
			params.x = (int) (x - offsetX);
			params.y = (int) (y - offsetY);
			this.setLayoutParams(params);
			if (null != wm) {
				wm.updateViewLayout(this, wmParams);
			}
		}
	}

	//事件拦截
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return isCanDrag?mDragMotionProxy.handleInterceptTouchEvent(ev):super.onInterceptTouchEvent(ev);
	}

//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev) {
//		return isCanDrag?mDragMotionProxy.handleInterceptTouchEvent(ev):super.dispatchTouchEvent(ev);
//	}
	
	@Override
	public void onActionDown(MotionEvent ev) {
		// 计算控件左上角和点击控件的间距
		int[] location = new int[2];
		this.getLocationOnScreen(location);//获取拖拽组件在屏幕的坐标点
		offsetX = ev.getRawX() - location[0];
		offsetY = ev.getRawY() - location[1];
	}
	
	@Override
	public void onActionMove(MotionEvent ev) {
		// 更新控件的位置
		moveWindowView(ev.getRawX(), ev.getRawY());
	}
	
	@Override
	public void onActionUp(MotionEvent ev) {
		endX = ev.getRawX();
		endY = ev.getRawY();
		@SuppressWarnings("deprecation")
		float windowWidth = wm.getDefaultDisplay().getWidth();
		if (isAutoAttachEdge) {
			if (endX < (windowWidth / 2)) {// 如果在屏幕左侧，自动贴到左侧，反之右侧
				showWindowView(0, endY);
				showX = 0;
			} else {
				showWindowView(windowWidth - this.getWidth(), endY);
				showX = windowWidth - this.getWidth();
			}
		}
		showY = endY;
	}

	private OnDragLayoutDismissListener mOnDragLayoutDismissListener;
	public void setOnDragLayoutDismissListener(OnDragLayoutDismissListener onDragLayoutDismissListener){
		mOnDragLayoutDismissListener=onDragLayoutDismissListener;
	}

}
