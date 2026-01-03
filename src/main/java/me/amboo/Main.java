package me.amboo;

import me.amboo.controller.HotelController;
import me.amboo.model.Room;
import me.amboo.service.OrderService;
import me.amboo.service.RoomService;
import me.amboo.algorithm.*;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static HotelController controller;
    private static final String ADMIN_PWD = "admin";

    public static void main(String[] args) {
        // 1. 初始化依赖
        RoomService roomService = new RoomService();
        OrderService orderService = new OrderService(roomService);
        controller = new HotelController(scanner, roomService, orderService);

        // 2. 初始化数据
        initData(roomService);

        // 3. 路由分发
        if (args.length > 0 && args[0].equals("-admin")) {
            if (authenticate()) adminLoop();
            else System.out.println("认证失败。");
        } else {
            userLoop();
        }
    }

    private static void initData(RoomService rs) {
        rs.addRoom(new Room(101, "单人间", 20.0));
        rs.addRoom(new Room(102, "单人间", 25.0));
        rs.addRoom(new Room(103, "单人间", 22.0));
        rs.addRoom(new Room(104, "单人间", 21.5));
      
        rs.addRoom(new Room(202, "双人间", 40.0));
        rs.addRoom(new Room(203, "双人间", 38.0));
        rs.addRoom(new Room(204, "双人间", 42.0));

        rs.addRoom(new Room(301, "单人间", 30.0));
        rs.addRoom(new Room(302, "双人间", 32.0));

        rs.addRoom(new Room(501, "双人间", 45.0));
        rs.addRoom(new Room(502, "双人间", 48.0));

        rs.addRoom(new Room(601, "总统套房", 65.0));

        rs.addRoom(new Room(801, "总统套房", 85.0));
        rs.addRoom(new Room(888, "总统套房", 120.0));
    }

    private static boolean authenticate() {
        System.out.print("请输入管理员密码: ");
    //    return ADMIN_PWD.equals(scanner.nextLine());
        java.io.Console console = System.console();
        if (console != null) {
            char[] passwordChars = console.readPassword();
            return ADMIN_PWD.equals(new String(passwordChars));
        }    else throw new IllegalArgumentException("终端创建异常");
}

    private static void userLoop() {
        System.out.println("=== 欢迎使用 Hotel A 自助系统 ===");
        while (true) {
            System.out.println("\n[用户菜单] \n1.预定房间 \n2.查询订单状态 \n3.自助办理入住 \n4.自助退房 \n0.退出\n");
            System.out.print("选择 > ");
            String choice = scanner.nextLine();
            if (choice.equals("0")) break;
            handleRequest(choice, false);
        }
    }

    private static void adminLoop() {
        System.out.println("=== 欢迎使用后端管理系统 ===");
        while (true) {
            System.out.println("\n[管理菜单] \n1.增删房间 \n2.代理下单 \n3.手动入住 \n4.手动退房 \n5.强制删除订单 \n6.酒店总房间列表 \n7.房间日志查询 ");
            System.out.println("8.[算法实验] AOV/AOE 自动化分析"); 
            System.out.println("9.[算法实验] 查看当前图权值分布");
            System.out.println("10.[算法实验] 清除权值缓存");
            System.out.println("0.退出");
            System.out.print("选择 > ");
            String choice = scanner.nextLine();
            if (choice.equals("0")) break;
            handleRequest(choice, true);
        }
    }

    private static void handleRequest(String choice, boolean isAdmin) {
        try {
            if (isAdmin) {
                switch (choice) {
                    case "1" -> controller.manageRooms();
                    case "2" -> controller.bookRoom();
                    case "3" -> controller.checkIn();
                    case "4" -> controller.checkOut();
                    case "5" -> controller.forceDeleteOrder();
                    case "6" -> controller.printAllRooms();
                    case "7" -> controller.printRoomHistory();
                    case "8" -> controller.runAlgorithmLab();
                    case "9" -> controller.printGraphWeights();
                    case "10" -> WeightGenerator.resetWeights();
                }
            } else {
                switch (choice) {
                    case "1" -> controller.bookRoom();
                    case "2" -> controller.queryOrder();
                    case "3" -> controller.checkIn();
                    case "4" -> controller.checkOut();
                }
            }
        } catch (Exception e) {
            System.err.println("操作失败：" + e.getMessage());
        }
    }
}