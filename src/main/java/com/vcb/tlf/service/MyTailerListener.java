package com.vcb.tlf.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.vcb.tlf.config.CommonConfig;
import com.vcb.tlf.models.EventModel;
import com.vcb.tlf.models.PatternModel;
import com.vcb.tlf.scheduler.TailLogScheduler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MyTailerListener extends TailerListenerAdapter {

    @Override
    public void handle(String line) {
        log.debug(line);
        //Kiem tra xem co match pattern khong
        for (PatternModel patternModel : CommonConfig.lstPattern) {
            Pattern pattern = patternModel.getPatternRegex();
            Matcher matcher = pattern.matcher(line);
            boolean matchFound = matcher.find();
            if (matchFound) {
                log.debug("Found matched line:" + line);
                log.debug("Pattern:" + patternModel);
                EventModel eventModel = new EventModel();
                eventModel.setLevel(patternModel.getLevel());
                eventModel.setLogLine(line);
                eventModel.setName(patternModel.getName());
                eventModel.setTime(new Date());
                TailLogScheduler.blockingQueues.add(eventModel);
            }
        }
    }
}
