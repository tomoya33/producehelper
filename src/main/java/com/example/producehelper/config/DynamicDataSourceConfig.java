package com.example.producehelper.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.example.producehelper.dataSource.DynamicDataSource;
import com.example.producehelper.model.StationDataSource;
import com.example.producehelper.util.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DynamicDataSourceConfig
{
    @Value("${file.dataSourceFile}")
    private String dataSourceFile;

    private static final String SQL_CONN_TEMPLATE = "jdbc:mysql://{0}/local_station?useUnicode=true&characterEncoding=UTF-8";

    @Bean(name = "master")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource master()
    {
        return DruidDataSourceBuilder.create().build();
    }

    //注入动态数据源
    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dynamicDataSource(@Qualifier("master") DataSource defaultDataSource)
    {
        Map<Object, Object> dataSources;
        try
        {
            dataSources = readStationDataSourceFromExcel();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ExceptionInInitializerError("初始化数据库连接信息失败");
        }
        return new DynamicDataSource(defaultDataSource, dataSources);
    }

    //事务
    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("dynamicDataSource") DynamicDataSource dynamicDataSource)
    {
        return new DataSourceTransactionManager(dynamicDataSource);
    }

    /**
     * 从excel文件中读取站点的数据库连接信息
     * @return
     * @throws Exception
     */
    private Map<Object, Object> readStationDataSourceFromExcel() throws Exception
    {
        ClassPathResource classPathResource = new ClassPathResource(dataSourceFile);

        List<StationDataSource> stationDataSourceList = FileUtils.readFromExcel(classPathResource.getFile(), StationDataSource.class);

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
}
