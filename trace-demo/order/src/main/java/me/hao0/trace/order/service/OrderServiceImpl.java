package me.hao0.trace.order.service;

import me.hao0.trace.order.dto.OrderDto;
import me.hao0.trace.order.model.Order;
import me.hao0.trace.user.model.User;
import me.hao0.trace.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
@Service("orderService")
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserService userService;

    public OrderDto create(Long userId, Integer amount) {

        // invoke other rpc service
        User user = userService.findById(userId);

        Order order = new Order();
        order.setId(2L);
        order.setAmount(amount);
        order.setBuyerId(user.getId());
        order.setOrderName("测试订单");
        order.setOrderNo("123456");

        OrderDto orderDto = new OrderDto();
        orderDto.setUser(user);
        orderDto.setOrder(order);

        return orderDto;
    }
}
