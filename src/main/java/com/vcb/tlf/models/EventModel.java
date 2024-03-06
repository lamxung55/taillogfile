package com.vcb.tlf.models;

import lombok.Data;

import java.util.Date;

@Data
public class EventModel {
    String name;
    String level;
    String logLine;
    Date time;
}
