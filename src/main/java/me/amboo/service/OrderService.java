package me.amboo.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import me.amboo.model.Order;
import me.amboo.model.Room;

@Getter
public class OrderService {
    // 核心功能：预订房间
    // 逻辑：先找房，再建单，最后绑定
    private Map<Integer, Order> allOrders = new HashMap<>(); //
    private RoomService roomService; //

    public OrderService(RoomService roomService) {
        this.roomService = roomService;
    }

    // 下单逻辑：仅根据房型库存判断
    public Order placeOrder(String guestID, LocalDate startDate, int stayTime, String roomType) {
        // 1. 获取该房型的物理房间总数
        long totalRoomsOfType = roomService.getAllRooms().stream()
                .filter(r -> r.getRoomType().equals(roomType))
                .count(); // 将 r.getRoomType() 与 roomType 作比较，如果相等则计数器自增。工作流彻底结束后将计数器的值赋给这个long变量

        // 2. 检查在预订时段内，每一天的“已订”订单数是否超过总房数
        for (int i = 0; i < stayTime; i++) {
            LocalDate dateToCheck = startDate.plusDays(i);
            
            // 统计该日期、该房型下，有多少个订单（已预订或已入住）
            long occupiedCount = allOrders.values().stream()
                    .filter(o -> o.getOrderType().equals(roomType))
                    .filter(o -> o.getStatus() == Order.OrderStatus.BOOKED || o.getStatus() == Order.OrderStatus.OCCUPIED)
                    // 使用 Order 类中已有的时间判断逻辑：如果当前日期处于订单区间内
                    .filter(o -> !dateToCheck.isBefore(o.getStartDate()) && dateToCheck.isBefore(o.getEndDate()))
                    .count();

            if (occupiedCount >= totalRoomsOfType) {
                throw new IllegalStateException("错误：" + dateToCheck + " 该房型已售罄");
            }
        }

        // 3. 库存储备充足，生成订单，但不与具体 Room 绑定
        Order newOrder = new Order(guestID, startDate, stayTime, roomType); //
        allOrders.put(newOrder.getOrderID(), newOrder);
        return newOrder;
    }

    /**
     * 核心：入住分配逻辑
     * 将 Order 与具体的 Room 进行物理绑定
     */
    public void processCheckIn(int orderID, int roomID) {
        Order order = findOrderOrThrow(orderID);
        Room room = roomService.findRoomById(roomID);

        if (room == null) throw new IllegalArgumentException("房间不存在");
        if (!room.getRoomType().equals(order.getOrderType())) throw new IllegalStateException("房型不匹配");

        // 调用 Room 内部逻辑：检查该房间物理上是否可用（考虑昨天的打扫状态和未来冲突）
        if (room.canBeDistribute(order.getStartDate(), order.getStayTime())) {
            room.addOrder(order); // 正式写入房间的 orderList 和索引
            order.CheckIn();      // 修改状态为 OCCUPIED
        } else {
            throw new IllegalStateException("该房间当前无法分配（未打扫或有物理冲突）");
        }
    }

    // 办理退房
    public void processCheckOut(int orderID, int roomID) {
        Order order = findOrderOrThrow(orderID);
        order.CheckOut(); // 修改状态为 CLEANING
    }

    // 取消订单
    public void cancelOrder(int orderID, int roomID) {
        Room room = roomService.findRoomById(roomID);
        if (room == null) throw new IllegalArgumentException("房间不存在");

        room.deleteOrder(orderID); // 从房间的预订列表中移除
        allOrders.remove(orderID);  // 从总索引中移除
    }

    private Order findOrderOrThrow(int orderID) {
        Order order = allOrders.get(orderID);
        if (order == null) {
            throw new IllegalArgumentException("错误：未找到 ID 为 " + orderID + " 的订单");
        }
        return order;
    }

    public Order getOrder(int orderID) {
        return allOrders.get(orderID);
    }
}