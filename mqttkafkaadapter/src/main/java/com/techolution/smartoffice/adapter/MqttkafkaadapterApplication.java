package com.techolution.smartoffice.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.techolution.smartoffice.adapter.callback.SmartOfficeMqttCallBack;



@SpringBootApplication
public class MqttkafkaadapterApplication implements CommandLineRunner{
	
	 public static Logger logger = LoggerFactory.getLogger(MqttkafkaadapterApplication.class);
	
	 
/*	 @Autowired
	 private CustomProperties customProperties;*/
	 
	 @Autowired
	 SmartOfficeMqttCallBack smartOfficeMqttCallBack;

	public static void main(String[] args) {
		SpringApplication.run(MqttkafkaadapterApplication.class, args);
		
	}



	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		smartOfficeMqttCallBack.connect();
		//smartOfficeMqttCallBack.subscribe();
	}
	
	
	
	
	
}