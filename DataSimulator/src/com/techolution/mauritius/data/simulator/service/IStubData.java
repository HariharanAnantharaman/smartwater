package com.techolution.mauritius.data.simulator.service;

import java.text.ParseException;

public interface IStubData {
	
	public void startProcess(int meterId,String startTime,String endTime,long sleepTime,int incrementtime);

}
