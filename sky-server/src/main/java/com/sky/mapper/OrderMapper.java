package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.aspectj.weaver.ast.Or;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);
    /**
     * 用于替换微信支付更新数据库状态的问题
     * @param orderStatus
     * @param orderPaidStatus
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);


    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select status from orders where status in (2,3,4)")
    List<Integer> countStatus();

    @Select("select * from orders where status = #{status} and  order_time < #{time} ")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime time);

    @Select("select * from orders where status = #{status}")
    List<Orders> getByStatus(Integer status);

    Double sumByMap(Map<String, Object> map);

    List<Orders> getSum(Map<String, Object> map);

    List<Orders> countByTime(LocalDateTime begin,LocalDateTime end);

    List<GoodsSalesDTO> getSalesTop(LocalDateTime begin, LocalDateTime end);

    Integer countByMap(Map map);
}
