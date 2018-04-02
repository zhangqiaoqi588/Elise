package com.hnqc.ironhand.analyzer;

import com.hnqc.ironhand.common.pojo.entity.Seed;
import org.springframework.kafka.annotation.KafkaListener;

public class AnalyzerListener {
    private String groupId = "analyzer";
    private String topic = "analyzer";

    @KafkaListener(topics = "#{__listener.topic}", groupId = "#{__listener.groupId}")
    public void listen(Seed seed) {

    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}