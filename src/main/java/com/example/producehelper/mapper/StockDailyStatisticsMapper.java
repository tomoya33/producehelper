package com.example.producehelper.mapper;

import com.example.producehelper.model.GoodsStockDailyStatistics;
import com.example.producehelper.model.GoodsStockRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
public interface StockDailyStatisticsMapper
{
    Date getUpdateTime();

    List<GoodsStockRecord> getLatestRecords(@Param("startTime") Date date, @Param("endTime") Date endTime);

    List<GoodsStockDailyStatistics> getAllStockDailyStatistics(@Param("statisticsDate") Date statisticsDate);

    void updateStatistics(GoodsStockDailyStatistics statistics);
}
