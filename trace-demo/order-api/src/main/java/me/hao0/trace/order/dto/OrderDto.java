package me.hao0.trace.order.dto;

import me.hao0.trace.order.model.Order;
import me.hao0.trace.user.model.User;
import java.io.Serializable;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class OrderDto implements Serializable {

    private static final long serialVersionUID = 8142107812921077485L;

    private User user;

    private Order order;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "OrderDto{" +
                "user=" + user +
                ", order=" + order +
                '}';
    }
}
