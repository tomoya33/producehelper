package com.example.producehelper.service.impl;

import com.example.producehelper.dataSource.DynamicDataSource;
import com.example.producehelper.model.StationDataSource;
import com.example.producehelper.model.StationSelected;
import com.example.producehelper.model.common.Constants;
import com.example.producehelper.service.inf.IExecuteSQLService;
import com.example.producehelper.util.FileUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ExecuteSQLServiceImpl implements IExecuteSQLService
{
    @Value("${file.sqlLogFile}")
    private String sqlLogFile;

    @Autowired
    private DynamicDataSource dynamicDataSource;

    @Override
    public String runSql(StationSelected stationSelected) throws Exception
    {
        List<String> stationIds;
        String allSelected = stationSelected.getSelected();
        if ("all".equals(allSelected))
        {
            ClassPathResource classPathResource = new ClassPathResource("config/上线站点信息.xlsx");
            List<StationDataSource> stationDataSourceList = FileUtils.readFromExcel(classPathResource.getFile(), StationDataSource.class);
            stationIds = stationDataSourceList.stream().map(StationDataSource::getStationId).collect(Collectors.toList());
        }
        else
        {
            stationIds = stationSelected.getStations();
        }

        if (stationIds == null || stationIds.isEmpty())
        {
            System.out.println("所选站点为空");
            return "所选站点为空";
        }

        String sqlFilePath = "sql/run.sql";

        executeSqlOnStation(stationIds, sqlFilePath);

        return "SUCCESS";
    }

    private void executeSqlOnStation(Collection<String> stationIds, String sqlFilePath) throws IOException, SQLException
    {
        ClassPathResource sqlResource = new ClassPathResource(sqlFilePath);

        String logRootPath = new File("logs").getCanonicalPath();
        File logFile = new File(logRootPath, sqlLogFile);
        PrintWriter writer = new PrintWriter(new FileWriter(logFile));

        for (String stationId : stationIds)
        {
            DynamicDataSource.setDataSourceKey(stationId);
            //获取数据库链接
            ScriptRunner runner = new ScriptRunner(dynamicDataSource.getConnection());
            runner.setStopOnError(true);
            runner.setLogWriter(writer);
            runner.setErrorLogWriter(writer);
            writer.println("-----------------" + stationId + "执行开始-----------------");
            writer.flush();
            runner.runScript(new InputStreamReader(sqlResource.getInputStream(), "UTF-8"));
            //释放链接，不释放会导致数据库链接一直被占用，后续的请求无法获取
            runner.closeConnection();
            writer.println("-----------------" + stationId + "执行完成-----------------");
            writer.println();
            writer.println();
            writer.flush();
        }
        DynamicDataSource.setDataSourceKey(Constants.DEVELOP_STATION_ID);
        writer.close();
    }
}
