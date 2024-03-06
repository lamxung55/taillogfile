package com.vcb.tlf.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcb.tlf.models.PatternModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Created by Mina Mimi on 8/21/2022.
 */
@Slf4j
@Component
public class CommonConfig {
    public static List<PatternModel> lstPattern;


    @Value("${influxdb_api_url}")
    private String influxApiUrl;

    public static String config_influxApiUrl;

    @Value("${influxdb_api_token}")
    private String token;

    public static String config_token;

    @Value("${folder_path}")
    private String folder_path;

    public static String config_folder_path;

    @Value("${file_path}")
    private String file_path;

    public static String config_file_path;

    @Value("${node_id}")
    private String nodeId;

    public static String config_nodeId;

    @Value("${send_metric_rate}")
    private long send_metric_rate;

    public static long config_send_metric_rate;

    @Value("${run_mode}")
    private String runMode;

    @Value("${pattern_path}")
    private String pattern_path;

    public static String config_runMode;

    private final static String orgName = "vietcombank";

    public static ExecutorService sendEventExecutor;

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        loadTelcoPrefixTable();
        sendEventExecutor = Executors.newFixedThreadPool(4);
        config_influxApiUrl = influxApiUrl;
        config_token = token;
        config_folder_path = folder_path;
        config_file_path = file_path;
        config_nodeId = nodeId;
        config_runMode = runMode;
        config_send_metric_rate = send_metric_rate;
    }

    private void loadTelcoPrefixTable() {
        log.info("Loading pattern table");
        // create Object Mapper
        ObjectMapper mapper = new ObjectMapper();
        try {
            String profileActive = env.getProperty("spring.profiles.active");
            if ("dev".equalsIgnoreCase(profileActive)) {
                ClassPathResource cpr = new ClassPathResource("pattern.json");
                String content = StreamUtils.copyToString(cpr.getInputStream(), Charset.defaultCharset());
                log.info(content);
                lstPattern = mapper.readValue(content, new TypeReference<List<PatternModel>>() {
                });
            } else {
                String path = "pattern.json";
                File file = new File(pattern_path);
                String content = StreamUtils.copyToString(new FileInputStream(file), Charset.defaultCharset());
                log.info(content);
                lstPattern = mapper.readValue(content, new TypeReference<List<PatternModel>>() {
                });
            }
            for (PatternModel patternModel : lstPattern) {
                patternModel.setPatternRegex(Pattern.compile(patternModel.getPattern(), Pattern.CASE_INSENSITIVE));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.info("Finished loading pattern table");
    }

}
