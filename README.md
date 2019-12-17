AirDroid项目说明
==================
Android设备间远程控制：
程序分为两部分，分别运行在受控端和控制端，时序图请参照“启动流程.png”。

- 受控端：调用初始化后会启动RemoteService（用作会话处理、信令接收）、ProjectionServcice（用作录屏传输）
监听连接，当连接建立后，初始化编码器、开始录屏传输。
- 控制端：设置好分辨率帧率参数，输入受控端ip地址，点击连接后方可看到受控端屏幕以及操作控制

运行环境和条件
-----------------
- 受控端Android系统版本 >= 5.0
- 受控端与控制端需要在同一个网段下，或相互均有独立IP地址
- 受控端SDK所在的应用需要具备Android系统进程权限（用于事件注入）
- 控制端解码器默认采用M1设备的SPS、PPS参数，可自行扩展

项目结构
-----------------
- airdroid_sdk：包含核心功能实现
├─java
│  ├─com
│  │  └─luoj
│  │      └─airdroid
│  │          │  AirDroid.java 初始化服务
│  │          │  CodecParam.java 编解码器参数
│  │          │  EventInput.java 事件注入
│  │          │  RTPParam.java
│  │          │  SocketParam.java 会话传输参数
│  │          │
│  │          ├─activity
│  │          │      InitActivity.java 初始化MediaProjection
│  │          │
│  │          ├─adapter
│  │          ├─decoder
│  │          │      VideoDecoder.java 已弃用
│  │          │      VideoDecoder2.java 视频解码器
│  │          │
│  │          ├─eventinjection 已弃用
│  │          │
│  │          ├─service
│  │          │      ProjectionService.java 录屏服务
│  │          │      RemoteServer.java 已弃用
│  │          │      RemoteServerLauncher.java 已弃用
│  │          │      RemoteService.java 会话管理服务
│  │          │      RTPProjectionService.java 录屏编码传输服务
│  │          │
│  │          └─view 悬浮窗显示丢包、抖动
│  │
│  └─jlibrtp 第三方RTP库

- app：Demo程序，包含组件初始化、受控端、控制端等范例代码
├─java
│  └─com
│      └─luoj
│          └─airdroid
│              │  AppContext.java
│              │  Util.java
│              │
│              ├─activity
│              │      AutoConnectActivity.java 连接和控制界面
│              │      InputActivity.java IP输入界面
│              │      MainActivity.java 功能列表界面
│              │      RTPPlayActivity.java 已弃用
│              │      RTPProjectionActivity.java 已弃用
│              │      RTPShareActivity.java 已弃用
│              │      TestActivityPreview.java 已弃用
│              │      TestEncodeLoopback.java 已弃用
│              │      TestNoEncodeLoopback.java 已弃用
│              │
│              ├─adapter
│              │      BaseRecyclerViewAdapter.java
│              │      IDataFilter.java
│              │
│              └─view
│                      IPEditText.java

会话协议
-----------------
基于分隔符的单行文本协议，格式：action+;+参数1+;+参数2...

第三方依赖库
-----------------
jlibrtp0.2.2(Java源码版本)<br>
androidasync Socket服务端和客户端<br>
XLog 日志<br>
windowutil_1.0 自定义悬浮窗<br>


受控端SDK集成方法
-----------------
在Application中加入下面的代码，即完成组件初始化和连接服务监听。
```java
//设置横竖屏，会影响受控方录屏分辨率、编码器分辨率
CodecParam.setOritationLandscape();
CodecParam.framerate = 30;
CodecParam.bitrate = 1024 * 1024 * 1;
CodecParam.i_frame_interval = 1;
//初始化组件
AirDroid.init(this);
```