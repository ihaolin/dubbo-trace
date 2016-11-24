package me.hao0.trace.order.service;

import me.hao0.trace.order.dto.OrderDto;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public interface OrderService {

    OrderDto create(Long userId, Integer amount);
}
