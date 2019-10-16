package com.luoj.airdroid.view.draggableview;

import android.util.AndroidException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author LuoJ
 * @date 2014-7-25
 * @package j.android.library.view - DragMotionProxy.java
 * @Description 拖拽动作处理的代理，当本身布局区域交互和拖拽冲突时，必须把布局的拦截交给拖拽代理处理（默认即可，不调用notNeedIntercept）。
 */
public class DragMotionProxy {

	private ViewGroup byAgentContainer;//被代理的容器(需要添加拖拽手势的容器组件)
	
	private AtomicBoolean initialized=new AtomicBoolean(false);
	
	private AtomicBoolean initInterceptTouchEvent=new AtomicBoolean(false);
	
	private OnDragListener mOnDragListener;
	
	private float startX;// 按下的X坐标
	private float startY;// 按下的Y坐标
	private boolean isMoveing = false;// 是否在移动状态
	private float canMoveOffset = 20f;//可移动的偏移量
	
	public void set(ViewGroup byAgentContainer,OnDragListener onDragListener){
		if (null==byAgentContainer) {
			try {
				throw new AndroidException("给组件容器设置拖拽手势时发生错误，传入的容器组件对象为空！");
			} catch (AndroidException e) {
				e.printStackTrace();
			}
		}
		if (initialized.compareAndSet(false, true)) {
			this.byAgentContainer=byAgentContainer;
			this.mOnDragListener=onDragListener;
			this.byAgentContainer.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent ev) {
					if (!initInterceptTouchEvent.get()) {
						throw new RuntimeException("被代理组件必须重写onInterceptTouchEvent()，并调用代理对象处理！");
					}
					int action = ev.getAction();
					switch (action) {
                    case MotionEvent.ACTION_DOWN:
                         if (isNotNeedIntercept){
//                             LogUtil.d("drag_motion_proxy onTouch down");
                             startX = ev.getRawX();
                             startY = ev.getRawY();
                             if(null!=DragMotionProxy.this.mOnDragListener)DragMotionProxy.this.mOnDragListener.onActionDown(ev);
                             return true;
                         }
                         break;
					case MotionEvent.ACTION_MOVE:// 移动
//						LogUtil.d("drag_motion_proxy onTouch move isMoveing:"+isMoveing);
						if (isMoveing) {
							if(null!=DragMotionProxy.this.mOnDragListener)DragMotionProxy.this.mOnDragListener.onActionMove(ev);
						}
						else{
							if (Math.abs(startX - ev.getRawX()) > canMoveOffset || Math.abs(startY - ev.getRawY()) > canMoveOffset) {
								isMoveing = true;
//								LogUtil.d("开始拖拽，事件由拖拽布局处理，不下放。");
								return true;
							}
						}
						break;
					case MotionEvent.ACTION_UP:// 弹起
//						LogUtil.d("drag_motion_proxy onTouch up");
						if(null!=DragMotionProxy.this.mOnDragListener)DragMotionProxy.this.mOnDragListener.onActionUp(ev);
						isMoveing = false;
						break;
					}
					return false;
				}
			});
		}
	}
	
	/**
	 * 拦截事件.
	 * 当发生拖拽行为时，拦截事件，不在下传，自己处理
	 * @param ev
	 * @return
	 */
	public boolean handleInterceptTouchEvent(MotionEvent ev) {
		if (null!=ev) {//确定在视图中的onInterceptTouchEvent调用代理来处理
			initInterceptTouchEvent.compareAndSet(false, true);
		}
		isMoveing = false;
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d("","drag_motion_proxy handleInterceptTouchEvent down");
			startX = ev.getRawX();
			startY = ev.getRawY();
			if(null!=DragMotionProxy.this.mOnDragListener)DragMotionProxy.this.mOnDragListener.onActionDown(ev);
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d("","drag_motion_proxy handleInterceptTouchEvent move");
			if (Math.abs(startX - ev.getRawX()) > canMoveOffset || Math.abs(startY - ev.getRawY()) > canMoveOffset) {
				isMoveing = true;
				Log.d("","开始拖拽，事件由拖拽布局处理，不下放。");
				return true;
			}
		}
		return false;  
	}

    public void unInitializ(){
        if(null!=byAgentContainer)byAgentContainer.setOnTouchListener(null);

    }

    /**
     * 设置不需要拦截事件.
     */
    private boolean isNotNeedIntercept=false;
    public void notNeedIntercept(){
        isNotNeedIntercept=true;
        initInterceptTouchEvent.compareAndSet(false, true);
    }

	/**
	 * @author LuoJ
	 * @date 2014-7-25
	 * @package com.test.androidtest -- DragMotionProxy.java
	 * @Description 拖拽监听
	 */
	public interface OnDragListener{
		void onActionDown(MotionEvent ev);
		void onActionMove(MotionEvent ev);
		void onActionUp(MotionEvent ev);
	}
	
}