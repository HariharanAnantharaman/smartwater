package com.techolution.mauritius.smartwater.notification.controller;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.techolution.mauritius.smartwater.notification.domain.NotificationDetails;
import com.techolution.mauritius.smartwater.notification.domain.NotificationStatusStatistics;
import com.techolution.mauritius.smartwater.notification.service.NotificationService;


@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping(value="/notifications")

public class NotificationController {

private Log log = LogFactory.getLog(NotificationController.class);
	
	@Autowired
	private NotificationService notificationService;
	
	@RequestMapping(method=RequestMethod.GET,value="/open")
	public @ResponseBody List<NotificationDetails> getAllOpenNotification()
	{
		log.info("Entering NotificationController.getAllConnections");
		List<NotificationDetails> resultList=notificationService.getAllOpenNotifcations();
		
		log.info("Exiting NotificationController.getAllConnections");
		return resultList;
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/priroity/count")
	public @ResponseBody NotificationStatusStatistics getCountByPriority() throws UnknownHostException
	{
		log.info("Entering NotificationController.getCountByPriority");
		NotificationStatusStatistics stats=notificationService.getCountBasedOnStatus();
		//Object stats=notificationService.getCountBasedOnStatus();
		
		log.info("Exiting NotificationController.getCountByPriority");
		return stats;
	}
	
}

