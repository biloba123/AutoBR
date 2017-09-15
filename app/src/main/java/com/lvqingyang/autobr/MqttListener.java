package com.lvqingyang.autobr;

/**
 * Author：LvQingYang
 * Date：2017/8/29
 * Email：biloba12345@gamil.com
 * Github：https://github.com/biloba123
 * Info：
 */
interface MqttListener {
    void onConnected();
    void onFail();
    void onLost();
    void onRecieive(String message);
}
