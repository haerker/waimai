//package com.sky.task;
//
//import com.sky.entity.Orders;
//import com.sky.mapper.OrderMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Component
//@Slf4j
//public class OrderTask {
//    @Autowired
//    private OrderMapper orderMapper;
//
//    //    @Scheduled(cron = "0/10 * * * * ? ")
//    @Scheduled(cron = "0 * * * * ? ")
//    public void processTimeoutOrder() {
//        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
//        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
//        if (ordersList != null && !ordersList.isEmpty()) {
//            ordersList.forEach(orders -> {
//                orders.setStatus(Orders.CANCELLED);
//                orders.setCancelReason("超时未付款,自动取消");
//                orders.setCancelTime(LocalDateTime.now());
//                orderMapper.update(orders);
//            });
//        }
//    }
//
//    @Scheduled(cron = "0 0 4 * * ? ")
//    public void processDeliveryOrder() {
//        List<Orders> ordersList = orderMapper.getByStatus(Orders.DELIVERY_IN_PROGRESS);
//        if (ordersList != null && !ordersList.isEmpty()) {
//            ordersList.forEach(orders -> {
//                orders.setStatus(Orders.COMPLETED);
//                orderMapper.update(orders);
//            });
//        }
//    }
//}
