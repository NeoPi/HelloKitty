package com.byg.android.hellokitty;

import java.io.Serializable;

public class DestinationBean implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int type;         // 类型 1、集合地点  2、目的地
    private String name;      // 名字
    private String street ;   // 街道
    private double longitude; // 经度
    private double latitude;  // 维度
    
    @Override
    public String toString() {
        return "DestinationBean [type=" + type + ", name=" + name + ", street=" + street + ", longitude=" + longitude
                + ", latitude=" + latitude + ", isSelect=" + isSelect + "]";
    }
    private boolean isSelect = false ;
    
    public boolean isSelect() {
        return isSelect;
    }
    public void setSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public DestinationBean() {
        super();
    }
    public DestinationBean(int type, String name, double longitude, double latitude) {
        super();
        this.type = type;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }
    
    
}
