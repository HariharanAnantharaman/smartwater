package com.techolution.mauritius.smartwater.connection.domain;

import java.io.Serializable;
import java.util.Date;

public class Data implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int getDevid() {
		return devid;
	}
	public void setDevid(int devid) {
		this.devid = devid;
	}
	public String getSensor_locationname() {
		return sensor_locationname;
	}
	public void setSensor_locationname(String sensor_locationname) {
		this.sensor_locationname = sensor_locationname;
	}
	public Date getEndtime() {
		return endtime;
	}
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
	public double getWdata() {
		return wdata;
	}
	public void setWdata(double wdata) {
		this.wdata = wdata;
	}
	private int devid;
	private String sensor_locationname;
	private Date endtime;
	private double wdata;

}
