package me.amboo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.amboo.model.Room;

public class RoomService {
    private List<Room> roomList = new ArrayList<>(); // List 是一个接口，ArrayList 是实现 List 的一个类
    // 快速索引：用于 ID 查找和后续生成图时建立映射
    private Map<Integer, Room> roomMap = new HashMap<>();

    public void addRoom(Room targetRoom) {
        if (roomMap.containsKey(targetRoom.getRoomID())) {
            throw new IllegalArgumentException("错误：ID 为" + targetRoom.getRoomID() + "的房间已存在");
        }
        roomList.add(targetRoom);
        roomMap.put(targetRoom.getRoomID(), targetRoom);
    }

    public void removeRoom(int roomID) {
        Room room = roomMap.get(roomID);
        if (room == null) {
            throw new IllegalArgumentException("错误：ID 为" + roomID + "的房间不存在");
        }
        roomList.remove(room);
        roomMap.remove(roomID);
    }

    // 查找房间
    public Room findRoomById(int id) {
        return roomMap.get(id);
    }

    // 获取所有房间，直接返回 ArrayList
    public List<Room> getAllRooms() {
        return roomList;
    }


    // 核心业务功能：寻找指定时间段内可用的特定类型房间
    public List<Room> findAvailableRooms(LocalDate startDate, int stayTime, String type) {
        return roomList.stream()
            .filter(room -> room.getRoomType().equals(type)) // 过滤类型
            .filter(room -> room.canBeDistribute(startDate, stayTime)) // 调用 Room 类里的逻辑检查冲突
            .collect(Collectors.toList());
    }

    // 简单的统计：获取指定日期所有空闲房间
    public long getAvailableRoomCount(LocalDate date) {
        return roomList.stream()
            .filter(room -> room.canBeDistribute(date, 1))
            .count();
    }
}
