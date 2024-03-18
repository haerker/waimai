package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
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
    @Autowired
    private UserMapper userMapper;

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

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        Map<String,Object> map = new HashMap<>();
        map.put("end",endTime);
        List<User> TotalUserList = userMapper.countByTime(map);
        map.put("begin",beginTime);
        List<User> NewUserList = userMapper.countByTime(map);
        for (LocalDate day : dateList){
            LocalDateTime beginDay = LocalDateTime.of(day, LocalTime.MIN);
            LocalDateTime endDay = LocalDateTime.of(day, LocalTime.MAX);
            int countNew = 0;
            for(User user : NewUserList){
                LocalDateTime createTime = user.getCreateTime();
                if(createTime.isAfter(beginDay) && createTime.isBefore(endDay)) {
                    countNew++;
                }
            }
            newUserList.add(countNew);
            int countTotal = 0;
            for(User user : TotalUserList){
                LocalDateTime createTime = user.getCreateTime();
                if(createTime.isBefore(endDay))
                    countTotal++;
            }
            totalUserList.add(countTotal);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> vaildOrderCountList = new ArrayList<>();
        List<Orders> ordersList = orderMapper.countByTime(beginTime,endTime);
        Integer orderCountTotal = 0;
        Integer vaildOrderCountTotal = 0;
        for(LocalDate day : dateList){
            LocalDateTime beginDay = LocalDateTime.of(day, LocalTime.MIN);
            LocalDateTime endDay = LocalDateTime.of(day, LocalTime.MAX);
            int orderCount = 0;
            int vaildOrderCount = 0;
            for(Orders orders : ordersList){
                LocalDateTime orderTime = orders.getOrderTime();
                if(orderTime.isAfter(beginDay) && orderTime.isBefore(endDay)){
                    orderCount++;
                    if(orders.getStatus().equals(Orders.COMPLETED))
                        vaildOrderCount++;
                }
            }
            orderCountTotal += orderCount;
            vaildOrderCountTotal += vaildOrderCount;
            orderCountList.add(orderCount);
            vaildOrderCountList.add(vaildOrderCount);
        }
        Double orderCompletionRate = 0.0;
        if(orderCountTotal != 0) {
           orderCompletionRate = (vaildOrderCountTotal.doubleValue() / orderCountTotal);
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(vaildOrderCountList,","))
                .totalOrderCount(orderCountTotal)
                .validOrderCount(vaildOrderCountTotal)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
}
