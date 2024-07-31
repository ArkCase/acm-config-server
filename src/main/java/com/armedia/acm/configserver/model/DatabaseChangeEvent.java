package com.armedia.acm.configserver.model;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class DatabaseChangeEvent extends ApplicationEvent {
    private final String changeType;
    private final String details;

    public DatabaseChangeEvent(Object source, String changeType, String details) {
        super(source);
        this.changeType = changeType;
        this.details = details;
    }
}
