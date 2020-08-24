package com.example.producehelper.service.impl;

import com.example.producehelper.dataSource.DynamicDataSource;
import com.example.producehelper.model.StationDataSource;
import com.example.producehelper.model.StationSelected;
import com.example.producehelper.model.common.Constants;
import com.example.producehelper.model.common.ExecuteResult;
import com.example.producehelper.service.inf.IExecuteSQLService;
import com.example.producehelper.util.FileUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.Connection;
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

    @Autowired
    @Qualifier("stations")
    private List<StationDataSource> stationDataSourceList;

    @Override
    public String runSql(StationSelected stationSelected) throws Exception
    {
        Set<String> stationIds;
        String allSelected = stationSelected.getSelected();
        if ("all".equals(allSelected))
        {
            stationIds = new LinkedHashSet<>(0);
            for (StationDataSource stationDataSource : stationDataSourceList)
            {
                stationIds.add(stationDataSource.getStationId());
            }
        }
        else
        {
            stationIds = new LinkedHashSet<>(stationSelected.getStations());
        }

        if (stationIds == null || stationIds.isEmpty())
        {
            System.out.println("所选站点为空");
            return "所选站点为空";
        }

        String sqlFilePath = "sql/run.sql";

        executeSqlOnStation(stationIds, sqlFilePath);

        return "FINISH";
    }

    private void executeSqlOnStation(Collection<String> stationIds, String sqlFilePath) throws IOException, SQLException
    {
        ClassPathResource sqlResource = new ClassPathResource(sqlFilePath);

        String logRootPath = new File("logs").getCanonicalPath();
        File logFile = new File(logRootPath, sqlLogFile);
        PrintWriter writer = new PrintWriter(new FileWriter(logFile), true);

        for (String stationId : stationIds)
        {
            if (ExecuteResult.getExecuteResult(stationId) != null && ExecuteResult.getExecuteResult(stationId))
            {
                writer.println("-----------------" + stationId + "之前已执行成功，不再重复执行-----------------");
                writer.println();
                writer.println();
                continue;
            }
            DynamicDataSource.setDataSourceKey(stationId);
            //获取数据库链接
            Connection connection = null;
            ScriptRunner runner = null;
            try
            {
                connection = dynamicDataSource.getConnection();
                runner = new ScriptRunner(connection);
                connection.setAutoCommit(false);
                runner.setStopOnError(true);
                runner.setLogWriter(writer);
                runner.setErrorLogWriter(writer);
                writer.println("-----------------" + stationId + "执行开始-----------------");
                runner.runScript(new InputStreamReader(sqlResource.getInputStream(), "UTF-8"));

                writer.println("-----------------" + stationId + "执行完成-----------------");
                writer.println();
                writer.println();
                ExecuteResult.setExecuteResult(stationId, true);
            }
            catch (Exception e)
            {
                System.out.println("-----------------" + stationId + "执行失败-----------------");
                writer.println("-----------------" + stationId + "执行失败-----------------");
                writer.println();
                writer.println();
                if (connection != null)
                {
                    connection.rollback();
                }
                ExecuteResult.setExecuteResult(stationId, false);
            }
            finally
            {
                //释放链接，不释放会导致数据库链接一直被占用，后续的请求无法获取
                if (runner != null)
                {
                    runner.closeConnection();
                }
            }
        }
        DynamicDataSource.setDataSourceKey(Constants.DEVELOP_STATION_ID);
        writer.close();
    }
}
