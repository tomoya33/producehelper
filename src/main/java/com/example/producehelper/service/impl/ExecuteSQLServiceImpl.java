package com.example.producehelper.service.impl;

import com.example.producehelper.dataSource.DynamicDataSource;
import com.example.producehelper.model.common.Constants;
import com.example.producehelper.service.inf.IExecuteSQLService;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ExecuteSQLServiceImpl implements IExecuteSQLService
{
    @Value("${file.sqlLogFile}")
    private String sqlLogFile;

    @Autowired
    private DynamicDataSource dynamicDataSource;

    @Override
    public void runSql(List<String> stationIds) throws Exception
    {
        String sqlFilePath = "sql/run.sql";
        ClassPathResource sqlResource = new ClassPathResource(sqlFilePath);

        String projectRootPath = new File("logs").getCanonicalPath();
        File logFile = new File(projectRootPath, sqlLogFile);
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
            writer.flush();
        }
        DynamicDataSource.setDataSourceKey(Constants.DEVELOP_STATION_ID);
        writer.close();
    }
}
