package com.example.dynamicroutingdatasource.config;

import inno.api.model.RoutingDatabaseContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MyRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return RoutingDatabaseContextHolder.getClientDatabase();
    }
}
