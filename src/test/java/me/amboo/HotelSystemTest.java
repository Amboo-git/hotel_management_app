package me.amboo;

import me.amboo.model.Order;
import me.amboo.model.Room;
import me.amboo.service.OrderService;
import me.amboo.service.RoomService;

import java.time.LocalDate;

public class HotelSystemTest {
    public static void main(String[] args) {
        // 1. 初始化 Service
        RoomService roomService = new RoomService();
        OrderService orderService = new OrderService(roomService);

        // 2. 初始化物理房间：添加 2 间单人间
        System.out.println("=== 初始化房间 ===");
        roomService.addRoom(new Room(101, "单人间", 25.0));
        roomService.addRoom(new Room(102, "单人间", 25.0));
        System.out.println("已添加房间 101, 102 (类型: 单人间)");

        // 3. 模拟预订阶段：预订日期为 2026-01-10，住 2 天
        LocalDate startDate = LocalDate.of(2026, 1, 10);
        int stayTime = 2;

        try {
            System.out.println("\n=== 预订测试 ===");
            // 成功预订第 1 间
            Order o1 = orderService.placeOrder("Guest_001", startDate, stayTime, "单人间");
            System.out.println("订单 1 预订成功，ID: " + o1.getOrderID());

            // 成功预订第 2 间
            Order o2 = orderService.placeOrder("Guest_002", startDate, stayTime, "单人间");
            System.out.println("订单 2 预订成功，ID: " + o2.getOrderID());

            // 尝试预订第 3 间（此时库存应为 0）
            System.out.println("尝试预订第 3 间单人间...");
            Order o3 = orderService.placeOrder("Guest_003", startDate, stayTime, "单人间");
            
        } catch (IllegalStateException e) {
            System.err.println("预订失败捕获: " + e.getMessage()); // 预期结果：提示已售罄
        }

        // 4. 模拟入住阶段：为订单 1 分配 101 房
        try {
            System.out.println("\n=== 入住分配测试 ===");
            // 假设我们拿到了第一个订单的 ID
            Order firstOrder = orderService.getOrder(orderService.getAllOrders().keySet().iterator().next());
            
            System.out.println("为订单 " + firstOrder.getOrderID() + " 分配 101 房...");
            orderService.processCheckIn(firstOrder.getOrderID(), 101);
            
            System.out.println("入住成功！101 房当前状态: " + roomService.findRoomById(101).getRoomType());
            System.out.println("订单当前状态: " + firstOrder.getStatus());
            
            // 验证 101 房是否已记录该订单
            boolean hasOrder = roomService.findRoomById(101).getOrderList().contains(firstOrder);
            System.out.println("101 房物理绑定确认: " + (hasOrder ? "成功" : "失败"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}