package com.vcb.tlf.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RollingQueue<T> {

    private final static int DEFAULT_MAX_LENGTH = 10;
    private int maxLength;
    private List<T> dataList;

    public RollingQueue(int maxLength) {
        this.maxLength = Math.max(maxLength, DEFAULT_MAX_LENGTH);
        dataList = new ArrayList<>();
    }

    public void push(T item) {
        dataList.add(item);
        if (dataList.size() > maxLength) {
            dataList.remove(0);
        }
    }

    public T itemAt(int index) throws Exception {
        if (index < dataList.size()) {
            return dataList.get(index);
        }
        throw new Exception("Invalid index");
    }

    public void clear() {
        dataList.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<dataList.size();i++) {
            String line = (String)dataList.get(i);
            sb.append(line);
            if (i < dataList.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
