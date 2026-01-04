package me.amboo.controller;

import me.amboo.algorithm.*;
import me.amboo.model.Order;
import me.amboo.model.Room;
import me.amboo.service.OrderService;
import me.amboo.service.RoomService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class HotelController {
    private final Scanner scanner;
    private final RoomService roomService;
    private final OrderService orderService;

    public HotelController(Scanner scanner, RoomService roomService, OrderService orderService) {
        this.scanner = scanner;
        this.roomService = roomService;
        this.orderService = orderService;
    }

    // --- 用户侧核心逻辑 ---

    public void bookRoom() {
        System.out.print("请输入您的姓名/证件号: ");
        String guestId = scanner.nextLine();
        System.out.print("请输入入住日期 (YYYY-MM-DD): ");
        LocalDate startDate = LocalDate.parse(scanner.nextLine());
        System.out.print("请输入预定天数: ");
        int days = Integer.parseInt(scanner.nextLine());
        System.out.print("请选择房型 [A]单人间 [B]双人间 [C]总统套房: ");
        String choice = scanner.nextLine().toUpperCase();
        
        String type = switch (choice) {
            case "B" -> "双人间";
            case "C" -> "总统套房";
            default -> "单人间";
        };

        Order order = orderService.placeOrder(guestId, startDate, days, type);
        System.out.println("\n预定成功！您的订单 ID 是: " + order.getOrderID());
    }

    public void queryOrder() {
        System.out.print("请输入您的订单 ID: ");
        int id = Integer.parseInt(scanner.nextLine());
        Order order = orderService.getOrder(id);
        if (order != null) {
            System.out.println("\n--- 订单信息 ---");
            System.out.println("状态: " + order.getStatus() + " | 日期: " + order.getStartDate() + " | 房型: " + order.getOrderType());
        } else {
            System.out.println("未找到该订单。");
        }
    }

    public void checkIn() {
        System.out.print("请输入订单 ID: ");
        int orderId = Integer.parseInt(scanner.nextLine());
        Order order = orderService.getOrder(orderId);
        
        if (order == null) throw new IllegalArgumentException("订单不存在");

        List<Room> availableRooms = roomService.findAvailableRooms(
            order.getStartDate(), order.getStayTime(), order.getOrderType());

        if (availableRooms.isEmpty()) {
            System.out.println("抱歉，当前暂无准备就绪的房源。");
            return;
        }

        System.out.println("可用房间: ");
        availableRooms.forEach(r -> System.out.print("[" + r.getRoomID() + "] "));
        System.out.print("\n请选择房间号: ");
        int roomId = Integer.parseInt(scanner.nextLine());

        orderService.processCheckIn(orderId, roomId);
        System.out.println("入住成功！");
    }

    public void checkOut() {
        System.out.print("请输入订单 ID: ");
        int orderId = Integer.parseInt(scanner.nextLine());
        System.out.print("请输入房间号: ");
        int roomId = Integer.parseInt(scanner.nextLine());
        orderService.processCheckOut(orderId, roomId);
        System.out.println("退房成功，房间进入清洗状态。");
    }

    // --- 管理员专用逻辑 ---

    public void manageRooms() {
        System.out.print("请选择操作 [A]添加房间 [D]删除房间: ");
        String op = scanner.nextLine().toUpperCase();
        if (op.equals("A")) {
            System.out.print("房间号: "); int id = Integer.parseInt(scanner.nextLine());
            System.out.print("房型: "); String type = scanner.nextLine();
            System.out.print("面积: "); double area = Double.parseDouble(scanner.nextLine());
            roomService.addRoom(new Room(id, type, area));
            System.out.println("房间添加成功。");
        } else if (op.equals("D")) {
            System.out.print("输入要删除的房间号: ");
            int id = Integer.parseInt(scanner.nextLine());
            roomService.removeRoom(id);
            System.out.println("房间已移除。");
        }
    }

    public void forceDeleteOrder() {
        System.out.print("请输入订单 ID: ");
        int oid = Integer.parseInt(scanner.nextLine());
        System.out.print("关联的房间 ID: ");
        int rid = Integer.parseInt(scanner.nextLine());
        orderService.cancelOrder(oid, rid);
        System.out.println("订单已强行删除。");
    }

    public void printAllRooms() {
        System.out.println("\n--- 酒店全量房间列表 ---");
        System.out.printf("%-8s %-12s %-10s\n", "房号", "类型", "面积");
        for (Room r : roomService.getAllRooms()) {
            System.out.printf("%-8d %-12s %-10.2f\n", r.getRoomID(), r.getRoomType(), r.getRoomArea().doubleValue());
        }
    }

    public void printRoomHistory() {
        System.out.print("输入房号查看预定记录: ");
        int rid = Integer.parseInt(scanner.nextLine());
        Room r = roomService.findRoomById(rid);
        if (r == null) return;
        System.out.println("房间 " + rid + " 的记录数: " + r.getOrderList().size());
        r.getOrderList().forEach(o -> System.out.println("-> 订单ID:" + o.getOrderID() + " | 日期:" + o.getStartDate() + " | 状态:" + o.getStatus()));
    }

    /**
     * 自动化算法实验室：一键生成 AOV 和 AOE 结果
     */
    public void runAlgorithmLab() {
        List<Room> currentRooms = roomService.getAllRooms();
        if (currentRooms.isEmpty()) {
            System.out.println("警告：当前系统中没有房间，无法生成实验数据。");
            return;
        }

        System.out.println("\n========== 数据结构实验：查房逻辑分析 ==========");
        System.out.println("当前参与计算的房间数量：" + currentRooms.size());

        // --- 第一阶段：AOV 网与拓扑排序 ---
        System.out.println("\n[1/2] 正在构建 AOV 网并执行拓扑排序...");
        AOVNetwork aov = new AOVNetwork();
        aov.buildNetwork(currentRooms); // 内部执行楼层限制和权重判定

        TopoSort sorter = new TopoSort();
        try {
            List<Integer> topoOrder = sorter.execute(aov);
            String orderStr = topoOrder.stream()
                    .map(String::valueOf) // 将流中的每个 Integer 转换为 String
                    .collect(Collectors.joining(" -> ")); // 作用是将流中的元素用 " -> " 连接起来
            System.out.println(">> 合法的查房拓扑序列：");
            System.out.println(orderStr);
        } catch (IllegalStateException e) {
            System.err.println(">> AOV 排序失败：" + e.getMessage());
        }

        // --- 第二阶段：AOE 网与关键路径 ---
        System.out.println("\n[2/2] 正在构建 AOE 网并计算关键路径...");
        AOENetwork aoe = new AOENetwork();
        aoe.buildNetwork(currentRooms); // 权值视为移动耗时

        AOECalculator calculator = new AOECalculator();
        calculator.calculateCriticalPath(aoe); // 计算 ve, vl, e, l 指标

        System.out.println("\n================ 实验结束 ================");
    }

/**
 * 打印当前 AOE 网的完整权值分布
 */
public void printGraphWeights() {
    List<Room> currentRooms = roomService.getAllRooms();
    if (currentRooms.isEmpty()) {
        System.out.println("系统内无房间，无法生成图。");
        return;
    }

    // 构建 AOE 网以获取权值数据
    AOENetwork aoe = new AOENetwork();
    aoe.buildNetwork(currentRooms);

    System.out.println("\n========== 当前酒店图权值一览 (移动耗时) ==========");
    System.out.printf("%-15s %-15s %-10s\n", "起始房间", "目标房间", "权值(分钟)");
    System.out.println("--------------------------------------------------");

    boolean hasEdges = false;
    // 获取邻接表 Map<Integer, Map<Integer, Integer>>
    Map<Integer, Map<Integer, Integer>> adj = aoe.getAdjList();

    // 排序打印，方便查看
    List<Integer> sortedSourceIds = adj.keySet().stream().sorted().toList();

    for (Integer u : sortedSourceIds) {
        Map<Integer, Integer> neighbors = adj.get(u);
        for (Map.Entry<Integer, Integer> entry : neighbors.entrySet()) {
            Integer v = entry.getKey();
            Integer weight = entry.getValue();
            System.out.printf("房间 [%-5d]  --->  房间 [%-5d]  :  %-10d\n", u, v, weight);
            hasEdges = true;
        }
    }

    if (!hasEdges) {
        System.out.println("提示：当前逻辑下没有生成任何正权值的边。");
    }
    System.out.println("==================================================\n");
}
    
}