package com.vcb.tlf.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Data
public class PatternModel {
    private String name;
    private String level;
    private String pattern;
    private Pattern patternRegex;

    @Override
    public String toString() {
        return "PatternModel{" +
                "name='" + name + '\'' +
                ", level='" + level + '\'' +
                ", pattern='" + pattern + '\'' +
                '}';
    }
}
