package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();
        do{
            dateList.add(begin);
            begin = begin.plusDays(1);
        }while (!begin.equals(end.plusDays(1)));
        Map<String,Object> map = new HashMap<>();
        map.put("begin",beginTime);
        map.put("end",endTime);
        map.put("status",Orders.COMPLETED);
        List<Orders> ordersList  = orderMapper.getSum(map);
       for(LocalDate day : dateList){
           LocalDateTime beginDay = LocalDateTime.of(day, LocalTime.MIN);
           LocalDateTime endDay = LocalDateTime.of(day, LocalTime.MAX);
           double amount = 0.0;
           for(Orders orders : ordersList){
               LocalDateTime orderTime = orders.getOrderTime();
               if(orderTime.isAfter(beginDay) && orderTime.isBefore(endDay)){
                   amount += orders.getAmount().doubleValue();
               }
           }
           turnoverList.add(amount);
       }
//        dateList.forEach(day ->{
//            LocalDateTime beginTime = LocalDateTime.of(day, LocalTime.MIN);
//            LocalDateTime endTime = LocalDateTime.of(day, LocalTime.MAX);
//            Map<String,Object> map = new HashMap<>();
//            map.put("begin",beginTime);
//            map.put("end",endTime);
//            map.put("status", Orders.COMPLETED);
//            Double turnover = orderMapper.sumByMap(map);
//            turnover = turnover == null ? 0.0 : turnover;
//            turnoverList.add(turnover);
//        });
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();

    }
}
