package me.hao0.trace.web.dto;

import me.hao0.trace.order.dto.OrderDto;
import me.hao0.trace.user.model.Addr;
import java.io.Serializable;
import java.util.List;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class OrderCreateResp implements Serializable {

    private static final long serialVersionUID = -78879006483682489L;

    private OrderDto orderDto;

    private List<Addr> addrs;

    public OrderCreateResp(OrderDto orderDto, List<Addr> addrs) {
        this.orderDto = orderDto;
        this.addrs = addrs;
    }

    public OrderDto getOrderDto() {
        return orderDto;
    }

    public void setOrderDto(OrderDto orderDto) {
        this.orderDto = orderDto;
    }

    public List<Addr> getAddrs() {
        return addrs;
    }

    public void setAddrs(List<Addr> addrs) {
        this.addrs = addrs;
    }

    @Override
    public String toString() {
        return "OrderCreateResp{" +
                "orderDto=" + orderDto +
                ", addrs=" + addrs +
                '}';
    }
}
