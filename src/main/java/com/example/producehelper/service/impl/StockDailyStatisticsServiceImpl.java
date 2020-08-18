package com.example.producehelper.service.impl;

import com.example.producehelper.mapper.StockDailyStatisticsMapper;
import com.example.producehelper.model.GoodsStockDailyStatistics;
import com.example.producehelper.model.GoodsStockRecord;
import com.example.producehelper.service.inf.IStockDailyStatisticsService;
import com.example.producehelper.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class StockDailyStatisticsServiceImpl implements IStockDailyStatisticsService
{
    @Autowired
    private StockDailyStatisticsMapper stockDailyStatisticsMapper;

    @Override
    public String updateStockDailyStatistics() throws Exception
    {
        Date date = stockDailyStatisticsMapper.getUpdateTime();

        Date endTime = DateUtils.addDate(DateUtils.getToday(), 0, 0, 0, 8, 0, 0, 0 );
//        Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-08-17 11:00:00");
        List<GoodsStockRecord> latestRecords = stockDailyStatisticsMapper.getLatestRecords(date, endTime);

        if (latestRecords == null || latestRecords.isEmpty())
        {
            System.out.println("不需要处理");
            return "FINISH";
        }

        Map<String, List<GoodsStockRecord>> goodsId2RecordList = latestRecords.stream().collect(Collectors.groupingBy(GoodsStockRecord::getGoodsId));

//        Date statisticsDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-08-17 00:00:00");
        Date statisticsDate = DateUtils.addDate(DateUtils.getToday(), 0, 0, -1, 0, 0, 0, 0);
        List<GoodsStockDailyStatistics> stockDailyStatistics = stockDailyStatisticsMapper.getAllStockDailyStatistics(statisticsDate);
        Map<String, GoodsStockDailyStatistics> goodsId2StatisticsList = stockDailyStatistics.stream().collect(Collectors.toMap(goodsStockDailyStatistics -> goodsStockDailyStatistics.getGoodsId(), goodsStockDailyStatistics -> goodsStockDailyStatistics));

        List<GoodsStockDailyStatistics> updateList = new ArrayList<>(0);
        for (Map.Entry<String, List<GoodsStockRecord>> entry : goodsId2RecordList.entrySet())
        {
            String goodsId = entry.getKey();
            List<GoodsStockRecord> records = entry.getValue();
            GoodsStockDailyStatistics statistics = goodsId2StatisticsList.get(goodsId);
            Integer count = 0;
            for (GoodsStockRecord record : records)
            {
                Integer recordType = record.getStockRecordType();
                switch (recordType)
                {
                    case 22:
                        count -= record.getStockRecordCount();
                        break;
                    case 23:
                        count += record.getStockRecordCount();
                        break;
                }
            }
            if (count != 0)
            {
                statistics.setEndStock(statistics.getEndStock() + count);
                statistics.setReceiveCount(statistics.getReceiveCount() + count);
                statistics.setEndValue(statistics.getTransferAvgPrice().multiply(new BigDecimal(statistics.getEndStock())));
                updateList.add(statistics);
            }
        }

        System.out.println(updateList);

        for (GoodsStockDailyStatistics statistics : updateList)
        {
            stockDailyStatisticsMapper.updateStatistics(statistics);
        }

        return "SUCCESS";
    }
}
