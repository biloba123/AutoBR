package com.lvqingyang.autobr;

import java.io.Serializable;

/**
 * Author：LvQingYang
 * Date：2017/8/30
 * Email：biloba12345@gamil.com
 * Github：https://github.com/biloba123
 * Info：
 */
public class Code implements Serializable{


    /**
     * Id : 1000108
     * Time : 1504072753
     * CheckValue : 0f4e2
     */

    private int Id;
    private String Time;
    private String CheckValue;

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String Time) {
        this.Time = Time;
    }

    public String getCheckValue() {
        return CheckValue;
    }

    public void setCheckValue(String CheckValue) {
        this.CheckValue = CheckValue;
    }
}
