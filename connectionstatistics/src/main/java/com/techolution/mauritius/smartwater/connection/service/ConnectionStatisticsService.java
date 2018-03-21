package com.techolution.mauritius.smartwater.connection.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.techolution.mauritius.smartwater.connection.domain.Data;
import com.techolution.mauritius.smartwater.connection.domain.RequestData;
import com.techolution.mauritius.smartwater.connection.domain.Telemetry;


@Component
public class ConnectionStatisticsService {
	
	private Log log = LogFactory.getLog(ConnectionStatisticsService.class);
	
	public List<Data> getData(RequestData data) throws ParseException{
		
		
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		
		
		/*String startTime = myFormat.format(data.getStart_Time().getTime());
		String endTime = myFormat.format(data.getEnd_Time().getTime());*/
		String startTime = data.getStart_Time();
		//String startTime = "2018-03-01";
		
		String endTime = data.getEnd_Time();
		//String endTime = "2018-03-15";
		
		int distanceValue=data.getSample_Distance_value();
		//int distanceValue=30;
		String disVal=String.valueOf(distanceValue);
		
		String code="d";
		String groupVal=null;
		log.debug("Sample Distance:"+data.getSample_Distance());
		log.debug("distanceValue:"+distanceValue);
		//String groupVal="1d";
		if(data.getSample_Distance().equalsIgnoreCase("Day")){
			code="d";
			groupVal=disVal+code;
		}
		else if(data.getSample_Distance().equalsIgnoreCase("Hour")){
			code="h";
			groupVal=disVal+code;
		}else if(data.getSample_Distance().equalsIgnoreCase("Month")){
			int monthgroupval=distanceValue*30;
			groupVal=String.valueOf(monthgroupval)+"d";
			
		}else{
			code="d";
			groupVal=disVal+code;
		}
		
		
		int deviceId=data.getHouse_ID();
		//int deviceId=123;
		String query = "select sum(value)  from flow where time >='"+startTime+"' and time<'"+endTime+"' and meter_id='"+deviceId+"' group by time("+groupVal+") fill(0)";// now() - 10d and meter_id = '124' group by time(1d) fill(0)
		log.debug("Query is:"+query);
		
		
		InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:32768", "root", "root");
		String dbName = "mauritius_smartwater";
		QueryResult queryResult = influxDB.query(new Query(query, dbName));
		String locationName= "TEST";
		
		List<Result> resultlist=queryResult.getResults();
		int recordSize=0;
		List<Data> retlist=new ArrayList<Data>();
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		//Date date1=new SimpleDateFormat("yyyy-MM-DDTHH:mm:ssz").parse(sDate1);
		Data resultData=null;
		for(Result result:resultlist){
			List<Series> serieslist=result.getSeries();
			if(serieslist == null){
				break;
			}
			for(Series series:serieslist){
				List<List<Object>> valuelist=series.getValues();
				for(List<Object> results:valuelist){
					String endTimeReturned=(String)results.get(0);
					log.debug("Date is:"+(endTimeReturned.split("T"))[0]);
					Date date=dateFormat.parse(endTimeReturned);
					
					resultData=new Data();
					resultData.setDevid(deviceId);
					resultData.setEndtime(date);	
					resultData.setWdata(((Double)results.get(1)).doubleValue());
					resultData.setSensor_locationname(locationName);
					retlist.add(resultData);
				}
				
				
			}
			
		}
		influxDB.close();
		return retlist;
	}
	
	/**
	 * 
	 * @param telemetry
	 */
	@Async
	public void insertData(Telemetry telemetry){
		
		log.info("Entering ConnectionStatisticsService.insertData");
		if(telemetry.getDate()==null){
			log.info("Date is null.Setting defaule date");
			Calendar date=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			telemetry.setDate(date.getTime());
		}else{
			log.info("Time to set is:"+telemetry.getDate().getTime());
		}
		InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
		String dbName = "mauritius_smartwater";
		influxDB.setDatabase(dbName);
		influxDB.enableBatch(BatchOptions.DEFAULTS);
		String rpName = "aRetentionPolicy";
		//influxDB.createRetentionPolicy(rpName, dbName, "365d", "30m", 2, true);

		
		BatchPoints batchPoints = BatchPoints
				.database(dbName)
//				.tag("async", "true")
				.retentionPolicy(rpName)
				.consistency(ConsistencyLevel.ALL)
				.build();
		
		
		if(telemetry.getFlow()!=null){
			insertFlow(telemetry,influxDB,batchPoints);
		}else{
			log.info("No flow data. Not inserting");
		}
		
		
		if(telemetry.getBattery()!=null){
			insertBattery(telemetry,batchPoints);
		}else{
			log.info("No battery data. Not inserting");
		}
		
		if(telemetry.getFlowrate()!=null){
			insertFlowrate(telemetry,batchPoints);
		}else{
			log.info("No flowrate data. Not inserting");
		}
		
		if(telemetry.getReading()!=null){
			insertMeterReading(telemetry,batchPoints);
		}else{
			log.info("No meterreading data. Not inserting");
		}
		
		//influxDB.flush();
		influxDB.write(batchPoints);

		influxDB.close();
		
		log.info("Exiting ConnectionStatisticsService.insertData");
		
	}
	
	private void insertFlow(Telemetry telemetry,InfluxDB influxDB ,BatchPoints  batchPoints){
		
		log.info("Entering ConnectionStatisticsService.insertFlow");
		
		/*
		influxDB.write(Point.measurement("flow")
				.time(telemetry.getDate().getTime(), TimeUnit.MILLISECONDS)
				.addField("meter_id", telemetry.getMeter_id())
				.addField("value", telemetry.getFlow())
				.build());
*/
		
		Point point1 = Point.measurement("flow")
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.addField("meter_id", telemetry.getMeter_id())
				.addField("value", telemetry.getFlow())
				.build();

		batchPoints.point(point1);
		
	
		log.debug("Inserted flow into db");
		log.info("Exiting ConnectionStatisticsService.insertFlow");
		
	}
	private void insertBattery(Telemetry telemetry,BatchPoints  batchPoints){
		
		log.info("Entering ConnectionStatisticsService.insertBattery");
		
		/*influxDB.write(Point.measurement("batterylevel")
				.time(telemetry.getDate().getTime(), TimeUnit.MILLISECONDS)
				.addField("meter_id", telemetry.getMeter_id())
				.addField("value", telemetry.getBattery())
				.build());
		*/
		
		Point point1 = Point.measurement("batterylevel")
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.addField("meter_id", telemetry.getMeter_id())
				.addField("value", telemetry.getBattery())
				.build();

		batchPoints.point(point1);
	
		log.debug("Inserted battery into db");
		
		log.info("Exiting ConnectionStatisticsService.insertBattery");
		
	}
	
	private void insertFlowrate(Telemetry telemetry,BatchPoints  batchPoints){
		
		log.info("Entering ConnectionStatisticsService.insertFlowrate");
	
		/*influxDB.write(Point.measurement("flowrate")
				.time(telemetry.getDate().getTime(), TimeUnit.MILLISECONDS)
				.addField("meter_id", telemetry.getMeter_id())
				.addField("value", telemetry.getFlowrate())
				.build());*/
	
		Point point1 = Point.measurement("flowrate")
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.addField("meter_id", telemetry.getMeter_id())
				.addField("value", telemetry.getFlowrate())
				.build();
		batchPoints.point(point1);
		log.debug("Inserted Flowrate into db");
		
		log.info("Exiting ConnectionStatisticsService.insertFlowrate");
		
	}
	
 private void insertMeterReading(Telemetry telemetry,BatchPoints  batchPoints){
		
		log.info("Entering ConnectionStatisticsService.insertFlowrate");
	
		/*influxDB.write(Point.measurement("flowrate")
				.time(telemetry.getDate().getTime(), TimeUnit.MILLISECONDS)
				.addField("meter_id", telemetry.getMeter_id())
				.addField("value", telemetry.getFlowrate())
				.build());*/
	
		Point point1 = Point.measurement("meterreading")
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.addField("meter_id", telemetry.getMeter_id())
				.addField("value", telemetry.getReading())
				.build();
		batchPoints.point(point1);
		log.debug("Inserted meterreading into db");
		
		log.info("Exiting ConnectionStatisticsService.insertFlowrate");
		
	}


}
