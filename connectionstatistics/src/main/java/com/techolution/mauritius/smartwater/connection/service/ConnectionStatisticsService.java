package com.techolution.mauritius.smartwater.connection.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.techolution.mauritius.smartwater.connection.domain.ConnectionKpiData;
import com.techolution.mauritius.smartwater.connection.domain.Data;
import com.techolution.mauritius.smartwater.connection.domain.Kpi;
import com.techolution.mauritius.smartwater.connection.domain.RequestData;
import com.techolution.mauritius.smartwater.connection.domain.Telemetry;
import com.techolution.mauritius.smartwater.connection.domain.TelemetryRequestData;


@Component
public class ConnectionStatisticsService {
	
	private Log log = LogFactory.getLog(ConnectionStatisticsService.class);
	
	private static String INFLUX_CONNECTION_STRING="http://52.170.92.62:8086";
	private static String INFLUX_USERNAME="root";
	private static String INFLUX_PWD="root";
	
	private static String dbName = "mauritius_smartwater";
	/**
	 * 
	 * @param data
	 * @return
	 * @throws ParseException
	 */
	
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
		String query = "select sum(value)  from flowvalues where time >='"+startTime+"' and time<='"+endTime+"' and meter_id='"+deviceId+"' group by time("+groupVal+") fill(0)";// now() - 10d and meter_id = '124' group by time(1d) fill(0)
		log.debug("Query is:"+query);
		
		
		//InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:32770", "root", "root");
		InfluxDB influxDB = InfluxDBFactory.connect(INFLUX_CONNECTION_STRING, INFLUX_USERNAME, INFLUX_PWD);
		long startStarttime=System.currentTimeMillis();
		QueryResult queryResult = influxDB.query(new Query(query, dbName));
		long endtime=System.currentTimeMillis();
		log.debug("Time After getBattery query execution:"+endtime);
		log.debug("Time Taken for query execution:"+(endtime-startStarttime));
		String locationName= "TEST";
		
		List<Result> resultlist=queryResult.getResults();
	//	int recordSize=0;
		List<Data> retlist=new ArrayList<Data>();
	//	SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
		//SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ");
		//dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		//Date date1=new SimpleDateFormat("yyyy-MM-DDTHH:mm:ssz").parse(sDate1);
		Data resultData=null;
		//Instant  instant=null;
		for(Result result:resultlist){
			List<Series> serieslist=result.getSeries();
			if(serieslist == null){
				break;
			}
			for(Series series:serieslist){
				List<List<Object>> valuelist=series.getValues();
				for(List<Object> results:valuelist){
					String endTimeReturned=(String)results.get(0);
					/*log.debug("Date is:"+(endTimeReturned.split("T"))[0]);
					log.debug("Date2 is:"+(endTimeReturned.split("T"))[1]);*/
			//		instant= Instant.parse( endTimeReturned); 
					//Date date=java.util.Date.from(instant);
					//Date date=dateFormat.parse(endTimeReturned.split("T")[0]);
					
					resultData=new Data();
					resultData.setDevid(deviceId);
					resultData.setName(endTimeReturned.split("T")[0]);	
					resultData.setValue(((Double)results.get(1)).doubleValue());
					resultData.setSensor_locationname(locationName);
					retlist.add(resultData);
				}
				
				
			}
			
		}
		influxDB.close();
		return retlist;
	}
	
  public List<Data> geBatterytData(RequestData data) throws ParseException{
		
	  log.debug("Entering ConnectionStatisticsService.geBatterytData");
		
	//	SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		
		
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
		String query = "select last(value)  from batterylevelvalues where time >='"+startTime+"' and time<='"+endTime+"' and meter_id='"+deviceId+"' group by time("+groupVal+")";// now() - 10d and meter_id = '124' group by time(1d) fill(0)
		log.debug("Query is:"+query);
		
		
		//InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:32770", "root", "root");
		InfluxDB influxDB = InfluxDBFactory.connect(INFLUX_CONNECTION_STRING, INFLUX_USERNAME, INFLUX_PWD);
		long startStarttime=System.currentTimeMillis();
		log.debug("Time before getBattery query execution:"+startStarttime);
		QueryResult queryResult = influxDB.query(new Query(query, dbName));
		long endtime=System.currentTimeMillis();
		log.debug("Time After getBattery query execution:"+endtime);
		log.debug("Time Taken for query execution:"+(endtime-startStarttime));
		String locationName= "TEST";
		
		List<Result> resultlist=queryResult.getResults();
	//	int recordSize=0;
		List<Data> retlist=new ArrayList<Data>();
//		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
		//dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		//Date date1=new SimpleDateFormat("yyyy-MM-DDTHH:mm:ssz").parse(sDate1);
		Data resultData=null;
		
	//	Instant  instant=null;
		for(Result result:resultlist){
			List<Series> serieslist=result.getSeries();
			if(serieslist == null){
				break;
			}
			for(Series series:serieslist){
				List<List<Object>> valuelist=series.getValues();
				for(List<Object> results:valuelist){
					String endTimeReturned=(String)results.get(0);
					//log.debug("Date is:"+(endTimeReturned.split("T"))[0]);
				//	instant= Instant.parse( endTimeReturned); 
			//		Date date=java.util.Date.from(instant);
				//	Date date=dateFormat.parse(endTimeReturned);
					if(results.get(1)!=null){
						
				//	Date date=dateFormat.parse(endTimeReturned.split("T")[0]);
					resultData=new Data();
					resultData.setDevid(deviceId);
					resultData.setName(endTimeReturned.split("T")[0]);	
					
					resultData.setValue(((Double)results.get(1)).doubleValue());
					resultData.setSensor_locationname(locationName);
					retlist.add(resultData);
					}
				}
				
				
			}
			
		}
		influxDB.close();
		log.debug("Exiting ConnectionStatisticsService.geBatterytData");
		return retlist;
	}
  
  
  /**
   * 
   * @param data
   * @return
   * @throws ParseException
   */
  public List<Data> geInstanceTelemetrytData(TelemetryRequestData data) throws ParseException{
		
	  log.debug("Entering ConnectionStatisticsService.geInstanceTelemetrytData");
		
			
			int distanceValue=data.getSampleDistanceValue();
			//int distanceValue=30;
			String disVal=String.valueOf(distanceValue);
			
			String code="d";
			String groupVal=null;
			log.debug("Sample Distance:"+data.getSampleDistance());
			log.debug("distanceValue:"+distanceValue);
			boolean giveTimeStamp=false;
			//String groupVal="1d";
			if(data.getSampleDistance().equalsIgnoreCase("Day")){
				code="d";
				groupVal=disVal+code;
			}
			else if(data.getSampleDistance().equalsIgnoreCase("Hour")){
				code="h";
				groupVal=disVal+code;
				giveTimeStamp=true;
			}else if(data.getSampleDistance().equalsIgnoreCase("Month")){
				int monthgroupval=distanceValue*30;
				groupVal=String.valueOf(monthgroupval)+"d";
				
			}else{
				code="d";
				groupVal=disVal+code;
			}
			
			
			int deviceId=data.getHouseId();
			String seriesname=getSeriesForMetrics(data.getMetrics());
			
			
			
			//int deviceId=123;
			String query = "select last(value)  from "+ seriesname+" where time >='"+data.getStartTime()+"' and time<='"+data.getEndTime()+"' and meter_id='"+deviceId+"' group by time("+groupVal+")";// now() - 10d and meter_id = '124' group by time(1d) fill(0)
			if(data.getDefaultValueForMissingData()!=null){
				query = query+"fill("+data.getDefaultValueForMissingData()+")";
			}
			log.debug("Query is:"+query);
			
			
			//InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:32770", "root", "root");
			InfluxDB influxDB = InfluxDBFactory.connect(INFLUX_CONNECTION_STRING, INFLUX_USERNAME, INFLUX_PWD);
			long startStarttime=System.currentTimeMillis();
			QueryResult queryResult = influxDB.query(new Query(query, dbName));
			long endtime=System.currentTimeMillis();
			log.debug("Time After getBattery query execution:"+endtime);
			log.debug("Time Taken for query execution:"+(endtime-startStarttime));
			String locationName= "TEST";
			
			List<Result> resultlist=queryResult.getResults();

			List<Data> retlist=new ArrayList<Data>();
			Data resultData=null;
			
		//	Instant  instant=null;
			for(Result result:resultlist){
				List<Series> serieslist=result.getSeries();
				if(serieslist == null){
					break;
				}
				for(Series series:serieslist){
					List<List<Object>> valuelist=series.getValues();
					for(List<Object> results:valuelist){
						String endTimeReturned=(String)results.get(0);
						
						if(results.get(1)!=null){
							
					
						resultData=new Data();
						resultData.setDevid(deviceId);
						if(giveTimeStamp){
							resultData.setName(endTimeReturned);
						}else{
							resultData.setName(endTimeReturned.split("T")[0]);	
						}
							
						
						resultData.setValue(((Double)results.get(1)).doubleValue());
						resultData.setSensor_locationname(locationName);
						retlist.add(resultData);
						}
					}
					
					
				}
				
			}
			influxDB.close();
			log.debug("Entering ConnectionStatisticsService.geInstanceTelemetrytData");
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
		//InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:32770", "root", "root");
		InfluxDB influxDB =InfluxDBFactory.connect(INFLUX_CONNECTION_STRING, INFLUX_USERNAME, INFLUX_PWD);
		
		influxDB.setDatabase(dbName);
		influxDB.enableBatch(BatchOptions.DEFAULTS);
		String rpName = "aRetentionPolicy";
	//	influxDB.createRetentionPolicy(rpName, dbName, "365d", "30m", 2, true);
		influxDB.setRetentionPolicy("aRetentionPolicy");
		
		BatchPoints batchPoints = BatchPoints
				.database(dbName)
//				.tag("async", "true")
				.retentionPolicy(rpName)
				.consistency(ConsistencyLevel.ALL)
				.build();
		
		
		
		if(telemetry.getFlow()!=null){
			insertFlow(telemetry,influxDB,batchPoints);
			
			if(telemetry.getReading() == null){
				SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				long meterReading=getLastMeterReading(myFormat.format(telemetry.getDate()), telemetry.getMeter_id());
				log.debug("last flow value:"+meterReading);
				long newmeterreading=meterReading+telemetry.getFlow();
				telemetry.setReading(newmeterreading);
			}
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
		Map<String,String> tagMap=new HashMap<String,String>();
		tagMap.put("meter_id", Integer.toString(telemetry.getMeter_id()));
		Point point1 = Point.measurement("flowvalues")
				.time(telemetry.getDate().getTime(), TimeUnit.MILLISECONDS)
				.tag(tagMap)
				//.addField("meter_id", telemetry.getMeter_id())
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
		Map<String,String> tagMap=new HashMap<String,String>();
		tagMap.put("meter_id", Integer.toString(telemetry.getMeter_id()));
		Point point1 = Point.measurement("batterylevelvalues")
				.time(telemetry.getDate().getTime(), TimeUnit.MILLISECONDS)
				//.addField("meter_id", telemetry.getMeter_id())
				.tag(tagMap)
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
	
		Map<String,String> tagMap=new HashMap<String,String>();
		tagMap.put("meter_id",Integer.toString( telemetry.getMeter_id()));
		Point point1 = Point.measurement("flowratevalues")
				.time(telemetry.getDate().getTime(), TimeUnit.MILLISECONDS)
				.tag(tagMap)
				//.addField("meter_id", telemetry.getMeter_id())
				.addField("value", telemetry.getFlowrate())
				.build();
		batchPoints.point(point1);
		log.debug("Inserted Flowrate into db");
		
		log.info("Exiting ConnectionStatisticsService.insertFlowrate");
		
	}
	
 private void insertMeterReading(Telemetry telemetry,BatchPoints  batchPoints){
		
		log.info("Entering ConnectionStatisticsService.insertMeterReading");
	
		/*influxDB.write(Point.measurement("flowrate")
				.time(telemetry.getDate().getTime(), TimeUnit.MILLISECONDS)
				.addField("meter_id", telemetry.getMeter_id())
				.addField("value", telemetry.getFlowrate())
				.build());*/
	
		Map<String,String> tagMap=new HashMap<String,String>();
		tagMap.put("meter_id",Integer.toString( telemetry.getMeter_id()));
		Point point1 = Point.measurement("meterreadingvalues")
				.time(telemetry.getDate().getTime(), TimeUnit.MILLISECONDS)
				.tag(tagMap)
				//.addField("meter_id", telemetry.getMeter_id())
				.addField("value", telemetry.getReading())
				.build();
		batchPoints.point(point1);
		log.debug("Inserted meterreading into db");
		
		log.info("Exiting ConnectionStatisticsService.insertMeterReading");
		
	}
 
 /**
  * This methods gets data for specific meter for the day
  * @param meterId
  * @return
  * @throws ParseException
  */
 
    public ConnectionKpiData getAllMetricsForConnectionForDay(int meterId) throws ParseException{
    	
    	
    	log.info("Entering ConnectionStatisticsService.getAllMetricsForConnectionForDay");
    	SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Calendar dayStart=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    	dayStart.set(Calendar.HOUR_OF_DAY, 0);
    	dayStart.set(Calendar.MINUTE, 0);
    	dayStart.set(Calendar.SECOND, 0);
    	
    	
    	Calendar dayend=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    	dayend.set(Calendar.HOUR_OF_DAY, 0);
    	dayend.set(Calendar.MINUTE, 59);
    	dayend.set(Calendar.SECOND, 59);
    	dayend.add(Calendar.DATE,1);
    	
    	RequestData requestData=new RequestData();
    	requestData.setHouse_ID(meterId);
    	
    	requestData.setStart_Time(myFormat.format(dayStart.getTime()));
    	requestData.setEnd_Time(myFormat.format(dayend.getTime()));
    	requestData.setSample_Distance_value(1);
    	requestData.setSample_Distance("Day");
    	List<Data> batteryData=geBatterytData(requestData);
    	
    	Double batterylevel = new Double(2000);
    	if(batteryData != null && batteryData.size()>0){
    		batterylevel=batteryData.get(0).getValue();	
    	}
    	List<Data> consumptionData=getData(requestData);
    	Double consumption = new Double(0);
    	if(consumptionData != null && consumptionData.size()>0){
    		consumption=(consumptionData.get(0).getValue());	
    	}
    	
    	int status=getCurrentDeviceStatus(meterId);
    	
    	Kpi consumptionkpi= new Kpi("Consumption",consumption);
    	Kpi batterykpi= new Kpi("Battery",batterylevel);
    	String deviceStatus="IN ACTIVE";
    	if(status == 0){
    		deviceStatus="NOT WORKING";
    	}else if(status == 1){
    		deviceStatus="WORKING";
    	}else if(status == -1){
    		deviceStatus="IN ACTIVE";
    	}
    	Kpi statuskpi=new Kpi("Status",deviceStatus);
    	
    	List<Kpi> kpiList=new ArrayList<Kpi>();
    	kpiList.add(consumptionkpi);
    	kpiList.add(batterykpi);
    	kpiList.add(statuskpi);
    	
    	ConnectionKpiData data=new ConnectionKpiData(meterId, kpiList);
    	
    	
    	log.info("Exiting ConnectionStatisticsService.getAllMetricsForConnectionForDay");
    	return data;
    	
    }
    
    public int getCurrentDeviceStatus(int meterId){
    	log.info("Entering ConnectionStatisticsService.getCurrentDeviceStatus");
    	
    		
    	String query = "select last(value)  from devicestatus where  meter_id='"+meterId+"'";// now() - 10d and meter_id = '124' group by time(1d) fill(0)
		log.debug("Query is:"+query);
		
		
		//InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:32770", "root", "root");
		InfluxDB influxDB = InfluxDBFactory.connect(INFLUX_CONNECTION_STRING, INFLUX_USERNAME, INFLUX_PWD);
		long startStarttime=System.currentTimeMillis();
		QueryResult queryResult = influxDB.query(new Query(query, dbName));
    	log.info("Entering ConnectionStatisticsService.getCurrentDeviceStatus");
    	long endtime=System.currentTimeMillis();
		log.debug("Time After getBattery query execution:"+endtime);
		log.debug("Time Taken for query execution:"+(endtime-startStarttime));
    	List<Result> resultList=queryResult.getResults();
    	
    	int returnval=-1;
    	
    	if(resultList != null && resultList.size()>0){
    		Result result=resultList.get(0);
    		
    		List<Series> serieslist=result.getSeries();
    		if(serieslist != null && serieslist.size()>0){
    			Series series=serieslist.get(0);
    			
    			List<List<Object>> resultrow=series.getValues();
    			if(resultrow != null && resultrow.size()>0){
    				List<Object> row=resultrow.get(0);
    				returnval=((Double)row.get(1)).intValue();
    			}
    					
    		}
    	}
    	return returnval;
    	
    	
    	
    }
    
    /**
     * 
     * @param metrics
     * @return
     */
    private String getSeriesForMetrics(String metrics){
    	//TODO CHange this to take from DB or file
    	String returnVal=null;
    	
    	if("readings".equalsIgnoreCase(metrics)){
    		returnVal="meterreadingvalues";
    	}
    	return returnVal;
    }

    private long getLastMeterReading(String startTime,int meterId){
    	
    	
    	long baseReadingValue =0;
    	
    	String query = "select last(value)  from meterreadingvalues where time <='"+startTime+"' and meter_id'="+meterId+"'";// now() - 10d and meter_id = '124' group by time(1d) fill(0)
		log.info("Query is:"+query);
		
		
		//InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:32768", "root", "root");
		InfluxDB influxDB = InfluxDBFactory.connect(INFLUX_CONNECTION_STRING, INFLUX_USERNAME, INFLUX_PWD);
		String dbName = "mauritius_smartwater";
		QueryResult queryResult = influxDB.query(new Query(query, dbName));
		
		List<Result> results=queryResult.getResults();
		if(results != null && results.size()>0){
			Result result  = results.get(0);
			
			List<Series> serieslist=result.getSeries();
			if(serieslist !=null && serieslist.size()>0){
				Series series=serieslist.get(0);
				List<List<Object>> objects=series.getValues();
				List<Object> resultvals=objects.get(0);
				Double double1=(Double)resultvals.get(1);
				baseReadingValue=double1.longValue();
			}
				
			
		}
		return baseReadingValue;
    	
    }

}
