package com.example.producehelper.service.inf;

import com.example.producehelper.model.StationSelected;

public interface IExecuteSQLService
{
    String runSql(StationSelected stationSelected) throws Exception;
}
