package com.vcb.tlf.scheduler;


import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.vcb.tlf.config.CommonConfig;
import com.vcb.tlf.models.EventModel;
import com.vcb.tlf.service.MyTailerListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TailLogScheduler {


    private List<Tailer> lstTailer = new ArrayList<Tailer>();

    public static BlockingQueue<EventModel> blockingQueues = new LinkedBlockingDeque<>();

    private static final String bulk = "ist_monitor_keyword";

    @PostConstruct
    public void setup() {

    }

    @Value("${tail_file_rate}")
    private long tail_file_rate;

    //@Scheduled(cron = "0 15 10 15 * ?")
    public void tail() {
        try {
            if (lstTailer != null) {
                log.info("Clear last tailer first...");
                for (Tailer tailer : lstTailer) {
                    tailer.stop();
                }
                log.info("Finish clear last tailer!");
            }
            lstTailer = new ArrayList<Tailer>();
            //1. List file theo pattern
            try {
                // Đường dẫn tới thư mục chứa các tệp tin
                File directory = new File(CommonConfig.config_folder_path);
                Pattern pattern = Pattern.compile(CommonConfig.config_file_path, Pattern.CASE_INSENSITIVE);
                try {
                    // Lấy danh sách các tệp tin trong thư mục
                    File[] files = directory.listFiles();
                    if (files != null) {
                        // Lặp qua từng tệp tin và kiểm tra mẫu
                        for (File file : files) {
                            if (file.isFile()) {
                                Matcher matcher = pattern.matcher(file.getName());
                                boolean matchFound = matcher.find();
                                if (matchFound) {
                                    log.info("Found:" + file.getAbsolutePath());
                                    MyTailerListener listener = new MyTailerListener();
                                    Tailer tailer = Tailer.create(file, listener, tail_file_rate, true);
                                    lstTailer.add(tailer);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //2. Tail list file
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @Scheduled(fixedDelayString = "${send_metric_rate}")
    public void sendMetric() {
        try {
            //Send to timeseries influx db
            EventModel eventModel = null;
            List<EventModel> eventModelList = new ArrayList<>();
            HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
            while (((eventModel = blockingQueues.poll()) != null)) {
                if (hashMap.get(eventModel.getName()) == null) {
                    hashMap.put(eventModel.getName(), 1);
                } else {
                    hashMap.put(eventModel.getName(), ((Integer) hashMap.get(eventModel.getName())) + 1);
                }
            }
            if (hashMap.size() > 0) {
                log.info("hash to send:" + hashMap.size());
                if (!StringUtils.isEmpty(CommonConfig.config_runMode) && "PROD".equals(CommonConfig.config_runMode.toUpperCase())) {
                    InfluxDBClient client = InfluxDBClientFactory.create(CommonConfig.config_influxApiUrl, CommonConfig.config_token.toCharArray());

                    try (WriteApi writeApi = client.getWriteApi()) {
                        Point point = Point
                                .measurement(bulk)
                                .time(Instant.now(), WritePrecision.NS);
                        //point.addField(String.format("%s_%s", CommonConfig.config_nodeId, "ErrorCount"), errorCount);
                        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                            point.addField(entry.getKey(),entry.getValue().longValue());
                        }
                        writeApi.writePoint(bulk, "vietcombank", point);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        client.close();
                    }
                }
                eventModelList.clear();
                log.info("Sent:" + hashMap.size() + " to timeseries");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @PreDestroy
    public void destroy() {
        System.out.println("closing application context..let's do the final resource cleanup");
        if (lstTailer != null) {
            for (Tailer tailer : lstTailer) {
                tailer.stop();
            }
        }

    }

}
