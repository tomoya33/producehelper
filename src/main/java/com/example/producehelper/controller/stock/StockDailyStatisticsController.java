package com.example.producehelper.controller.stock;

import com.example.producehelper.service.inf.IStockDailyStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stockDailyStatistics")
public class StockDailyStatisticsController
{
    @Autowired
    private IStockDailyStatisticsService stockDailyStatisticsService;

    @PostMapping("/update")
    public String updateStockDailyStatistics()
    {
        try
        {
            return stockDailyStatisticsService.updateStockDailyStatistics();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "error";
    }
}
