package com.example.producehelper.service.inf;

import java.util.List;

public interface IExecuteSQLService
{
    void runSql(List<String> stationIds) throws Exception;
}
