package com.example.producehelper.controller.stationUpdateBatch;

import com.example.producehelper.service.inf.IExecuteSQLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 执行sql脚本
 */
@RestController
@RequestMapping("/sqlRunner")
public class ExecuteSQLController
{
    @Autowired
    private IExecuteSQLService executeSQLService;

    @PostMapping("/run")
    public String runSql(@RequestBody List<String> stationIds)
    {
        try
        {
            executeSQLService.runSql(stationIds);
            return "SUCCESS";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "FAIL";
        }
    }
}
