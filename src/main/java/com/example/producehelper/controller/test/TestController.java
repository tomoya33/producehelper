package com.example.producehelper.controller.test;

import com.example.producehelper.dataSource.DynamicDataSource;
import com.example.producehelper.mapper.SConfigMapper;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController
{
    @Autowired
    SConfigMapper sConfigMapper;

    @Autowired
    DynamicDataSource dynamicDataSource;

    @GetMapping("/multiStation")
    public String testMultiStation(@RequestParam("stationId") List<String> stations)
    {
        for (String stationId : stations)
        {
            DynamicDataSource.setDataSourceKey(stationId);
            String stationName = sConfigMapper.findDefValueByName("station_name");
            System.out.println(stationId + ": " + stationName);
        }
        return "SUCCESS";
    }


    @GetMapping("/multiStationScript")
    public String testMultiStationRunScript(@RequestParam("stationId") List<String> stations)
    {
        try
        {
            String sqlFilePath = "sql/test.sql";
            ClassPathResource file = new ClassPathResource(sqlFilePath);
            for (String stationId : stations)
            {
                DynamicDataSource.setDataSourceKey(stationId);
                ScriptRunner scriptRunner = new ScriptRunner(dynamicDataSource.getConnection());
                scriptRunner.setLogWriter(new PrintWriter(System.out));
                scriptRunner.setStopOnError(true);
                scriptRunner.runScript(new InputStreamReader(file.getInputStream(), "UTF-8"));
            }
        }
        catch (Exception e)
        {
            return "FAIL";
        }

        return "SUCCESS";
    }
}
