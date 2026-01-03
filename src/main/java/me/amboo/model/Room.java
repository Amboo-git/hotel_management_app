package me.amboo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import me.amboo.model.Order.OrderStatus;

@Getter
public class Room {

    private final int roomID;
    private final String roomType; // 房间类型
    private final BigDecimal roomArea; // 房间面积
    private BigDecimal roomPrice;

    private Map<Integer, Order> orderIdIndex = new HashMap<>();
    private Map<LocalDate, Order> orderIndexByDay  = new HashMap<>();

// 关键：存储该房间所有的预订记录
    private List<Order> orderList = new ArrayList<>();

    LocalDate checkInDate; // 实际入住日期

    public Room(int id, String type, double area){
        if(area <= 0){ throw new IllegalArgumentException("房间面积必须大于0"); }

        this.roomID = id;
        this.roomType = type;
        // 使用 valueOf 避免double的精度失真；area不直接使用String类型是为了便于判断其是否为正数
        this.roomArea = BigDecimal.valueOf(area);
    }

    public void addOrder(Order newOrder) {
        if (!this.roomType.equals(newOrder.getOrderType())) {
            throw new IllegalArgumentException("房间类型不匹配");
        }

        // 检查时间冲突 
        for (Order i : orderList) {     // 遍历已有订单
            if (i.cantBeBook(newOrder.getStartDate(), newOrder.getStayTime())) { 
                throw new IllegalStateException("该时间段该房间已被预订");
            }
        }

        // 如果没冲突，添加预订
        this.orderList.add(newOrder);
        this.orderIdIndex.put(newOrder.getOrderID(), newOrder);
        for (int i = 0; i < newOrder.getStayTime(); i++) {
            LocalDate date = newOrder.getStartDate().plusDays(i);
            this.orderIndexByDay.put(date, newOrder);
        }
    }

    public void deleteOrder(Integer id) {
        // 从 Map 中获取并移除订单对象
        Order removedOrder = this.orderIdIndex.remove(id);

        // 如果订单确实存在，则从 List 中也移除它
        if (removedOrder != null) {
            this.orderList.remove(removedOrder);
            for (int i = 0; i < removedOrder.getStayTime(); i++) {
                LocalDate date = removedOrder.getStartDate().plusDays(i);
                this.orderIndexByDay.remove(date, removedOrder);
        }
        } else {
            throw new IllegalArgumentException("错误：订单 ID " + id + " 不存在");
        }
    }

    public Boolean canBeDistribute(LocalDate targetDate, Integer stayTime) {
        // 检查昨天的订单状态
        Order lastOrder = this.orderIndexByDay.get(targetDate.minusDays(1));
        if(lastOrder != null && lastOrder.getStatus() != OrderStatus.FINISHED) return false;
        //     ^     这里之前出现过空指针，所以要先判断

         // 检查从入住日开始的每一天，房间是否已经被预订
        for (int i = 0; i < stayTime; i++) {
            LocalDate dateToCheck = targetDate.plusDays(i);
            if (this.orderIndexByDay.containsKey(dateToCheck)) return false;
        }
        return true;
    }

}
