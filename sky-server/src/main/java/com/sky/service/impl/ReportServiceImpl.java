package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesList = orderMapper.getSalesTop(beginTime,endTime);
        List<String> nameList = goodsSalesList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = goodsSalesList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

    @Override
    public void exportBussinessDate(HttpServletResponse response) {
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);

            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间： " + dateBegin + "至" + dateEnd);
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO businessData1 = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(i + 7);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData1.getTurnover());
                row.getCell(3).setCellValue(businessData1.getValidOrderCount());
                row.getCell(4).setCellValue(businessData1.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData1.getUnitPrice());
                row.getCell(6).setCellValue(businessData1.getNewUsers());
            }



            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
