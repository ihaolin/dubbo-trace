package me.hao0.trace.order.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class Order implements Serializable {

    private static final long serialVersionUID = 52052173679233246L;

    private Long id;

    private String orderNo;

    private String orderName;

    private Long buyerId;

    private Integer amount;

    private Date ctime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", orderName='" + orderName + '\'' +
                ", buyerId=" + buyerId +
                ", amount=" + amount +
                ", ctime=" + ctime +
                '}';
    }
}
