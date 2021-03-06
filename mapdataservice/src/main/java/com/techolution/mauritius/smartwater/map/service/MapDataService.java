package com.techolution.mauritius.smartwater.map.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.techolution.mauritius.smartwater.domain.MeterConnection;
import com.techolution.mauritius.smartwater.domain.MeterConsumption;
import com.techolution.mauritius.smartwater.domain.RequestData;
import com.techolution.mauritius.smartwater.map.InfluxProperties;

@Component
public class MapDataService {
	
	
	private Log log = LogFactory.getLog(MapDataService.class);
	
	 //RestTemplate restTemplate;
	
	 public RestTemplate restTemplate() {
		    return new RestTemplate();
		}
		
	 

		@Autowired
	    InfluxProperties influxProperties;
	
	public List<MeterConsumption> getDataForAllConnections(RequestData data) throws ParseException{
		
		
		log.info("Entering MapDataService.getDataForAllConnections ");
		
		
		ResponseEntity<MeterConnection[]> responseEntity = restTemplate().getForEntity("http://localhost:8082/consolidateddata/connections",MeterConnection[].class);
//		List<MeterConnection> availableConnections=restTemplate().getForObject("http://localhost:8082/consolidateddata/connections", ArrayList.class);

		MeterConnection[] returnedobjects = (MeterConnection[])responseEntity.getBody();
		List<MeterConnection> availableConnections =Arrays.asList(returnedobjects);

		//log.debug(arg0);
	//	List<MeterConnection> availableConnections =new ArrayList<MeterConnection>();


		//availableConnections.

		
		log.debug("List size returned is:"+availableConnections.size());
		//For each of the returned value call influx and resturn
		
		String endTime = getNextDay(data.getEnd_Time());
		//String endTime = "2018-03-15";
		
		String startTime = data.getStart_Time();
		
		int deviceId=data.getHouse_ID();
		//int deviceId=123;
				
		
		//InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:32768", "root", "root");
		InfluxDB influxDB = InfluxDBFactory.connect(influxProperties.getUrl(),influxProperties.getUsername(),influxProperties.getPassword());
		
		
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		myFormat.setTimeZone(TimeZone.getTimeZone(influxProperties.getDatatimezone()));
		String locationName= "TEST";
		
		List<MeterConsumption> resultList=new ArrayList<MeterConsumption>();
				
		availableConnections.parallelStream().forEach(meterConnection -> {
			
			String query = "select sum(value)  from flowvalues where time >='"+startTime+"' and time<'"+endTime+"' and meter_id='"+meterConnection.getHouse_id()+"'";// now() - 10d and meter_id = '124' group by time(1d) fill(0)
			log.debug("Query is:"+query);
			QueryResult queryResult = influxDB.query(new Query(query, influxProperties.getDbname()));
			List<Result> resultlist=queryResult.getResults();
			double consumption=0.0;
			String endDate=null;
			if(resultlist != null && resultlist.size()>0 ){
				Result result = resultlist.get(0);
				if(result .getSeries() != null && result.getSeries().size()>0){
					Series series=result.getSeries().get(0);
					endDate=(String)series.getValues().get(0).get(0);
					 consumption=((Double)series.getValues().get(0).get(1)).doubleValue();
					
				}
				
			}
			MeterConsumption meterconsumption=new MeterConsumption(meterConnection);
			if(endDate!=null){
				//try {
					log.debug("End date is:"+endDate);
					Instant instant=Instant.parse(endDate);
				//	Date date=myFormat.parse((endDate.split("Z"))[0]);
				//	meterconsumption.setEndTime(date);
					meterconsumption.setEndTime(java.util.Date.from(instant));
				/*} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
			meterconsumption.setWdata(consumption);
			resultList.add(meterconsumption);
		});
		
		influxDB.close();
		log.info("Exiting MapDataService.getDataForAllConnections ");
		return resultList;
	}
	
private String getNextDay(String endTime) throws ParseException {
		
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date=myFormat.parse(endTime);
		
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, 1);
		endTime = myFormat.format(calendar.getTime());
		return endTime;
	}

}
