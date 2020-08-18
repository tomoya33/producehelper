package com.example.producehelper.util;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.alibaba.druid.pool.DruidDataSource;
import com.example.producehelper.model.StationDataSource;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils
{
    private static final String SQL_CONN_TEMPLATE = "jdbc:mysql://{0}/local_station?useUnicode=true&characterEncoding=UTF-8";

    /**
     * 从excel文件中读取站点的数据库连接信息
     * @return
     */
    public static Map<Object, Object> readStationDataSourceFromExcel() throws Exception
    {
        ClassPathResource classPathResource = new ClassPathResource("config/上线站点信息.xlsx");

        List<StationDataSource> stationDataSourceList = readFromExcel(classPathResource.getFile(), StationDataSource.class);

        Map<Object, Object> dataSourceMap = new HashMap<>(stationDataSourceList.size());
        for (StationDataSource stationDataSource : stationDataSourceList)
        {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUsername(stationDataSource.getUserName());
            dataSource.setPassword(stationDataSource.getDataBasePass());
            dataSource.setInitialSize(1);
            dataSource.setMaxActive(2);
            dataSource.setMaxWait(60000);
            String urlConn = MessageFormat.format(SQL_CONN_TEMPLATE, stationDataSource.getStationIp());
            dataSource.setUrl(urlConn);
            dataSourceMap.put(stationDataSource.getStationId(), dataSource);
        }

        return dataSourceMap;
    }

    public static List<StationDataSource> readFromExcel(File file, Class entityClass)
    {
        ImportParams params = new ImportParams();
        params.setTitleRows(0);
        params.setHeadRows(1);

        List<StationDataSource> stationDataSourceList;
        try (InputStream in = new FileInputStream(file))
        {
            stationDataSourceList = ExcelImportUtil.importExcel(in, entityClass, params);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return stationDataSourceList;
    }
}
