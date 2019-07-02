package com.invengo.rfidpad.base;


/**
 * 管理内部调试开关
 */
public class DebugManager {

	private DebugManager(){
		//
	}

	private static DebugManager instance = null;

	public static synchronized DebugManager getInstance(){
		if(null == instance){
			instance  = new DebugManager();
		}
		return instance;
	}

	/**
	 * 退出
	 */
	public static synchronized void clearSettings(){
		if(null != instance){
			instance  = null;
		}
	}

	private boolean debug = false;//保存内部调试入口状态

	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}


}
