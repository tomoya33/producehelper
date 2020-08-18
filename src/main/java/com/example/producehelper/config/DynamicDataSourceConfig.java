package com.example.producehelper.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.example.producehelper.dataSource.DynamicDataSource;
import com.example.producehelper.util.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DynamicDataSourceConfig
{
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
        Map<Object, Object> dataSources = null;
        try
        {
            dataSources = FileUtils.readStationDataSourceFromExcel();
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
}
