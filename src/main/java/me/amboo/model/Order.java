package me.amboo.model;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Order {
    private Integer orderID; // 订单ID
    private String guestID; // 预定人ID
    private LocalDate startDate; // 预定日期
    private Integer stayTime; // 预定天数
    private String orderType; //预定房间类型

    private OrderStatus status; 
    public enum OrderStatus { BOOKED, OCCUPIED, CLEANING, FINISHED } 
     /* 订单状态：已预定、已入住、待清理、已结束 */


    public Order(String guestID, LocalDate startDate, Integer stayTime, String orderType){
        // 实例化时自动生成一个 7 位随机 ID
        this.orderID = ThreadLocalRandom.current().nextInt(1000000, 10000000);
        this.guestID = guestID;
        this.startDate = startDate;
        this.stayTime = stayTime;
        this.orderType = orderType;
        this.status = OrderStatus.BOOKED;
    }

    public LocalDate getEndDate() {
        return startDate.plusDays(stayTime); // 退房日期 = 预定日期 + 天数
    }

    public void CheckIn(){
        if(status != OrderStatus.BOOKED) throw new IllegalArgumentException("该订单状态不是 BOOKED");
        this.status = OrderStatus.OCCUPIED;
    }

    public void CheckOut(){
        if(status != OrderStatus.OCCUPIED) throw new IllegalArgumentException("该订单状态不是 OCCUPIED");
        this.status = OrderStatus.CLEANING;
    }

    public void Cleaning(){
        if(status != OrderStatus.CLEANING) throw new IllegalArgumentException("该订单状态不是 CLEANING");
        this.status = OrderStatus.FINISHED;
    }

    // 判断当前预订是否与另一段时间冲突
    // 逻辑：计算目标订单时间区间与已有订单时间区间是否有交集
    public boolean cantBeBook(LocalDate start, int days) {
        LocalDate end = start.plusDays(days);
        // 逻辑：(预订开始时间 < 上一订单退房时间) 且 (预订结束时间 > 上一订单入住时间)
        return start.isBefore(this.getEndDate()) && end.isAfter(this.startDate);
    }
}