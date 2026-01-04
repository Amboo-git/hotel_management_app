package me.amboo.algorithm;

import me.amboo.model.Room;
import java.util.*;
import java.util.stream.Collectors;

public class AOENetwork {
    // 邻接表：RoomID -> Map<邻接房间ID, 边权值>
    private Map<Integer, Map<Integer, Integer>> adjList = new HashMap<>();
    private Map<Integer, Integer> inDegreeMap = new HashMap<>();
    private Map<Integer, Room> roomNodes = new HashMap<>();

    public void buildNetwork(List<Room> rooms) {
        adjList.clear();
        inDegreeMap.clear();
        roomNodes.clear();

        // 将列表中的 Room 依次导入本程序的三个map
        for (Room r : rooms) {
            int rid = r.getRoomID();
            roomNodes.put(rid, r);
            adjList.put(rid, new HashMap<>());
            inDegreeMap.put(rid, 0);
        }

        // 按照 AOV 网的逻辑分组与排序楼层
        Map<Integer, List<Room>> roomsByFloor = rooms.stream()
                .collect(Collectors.groupingBy(r -> r.getRoomID() / 100));
        // 结果: 返回一个 Map<楼层，楼层号>

        List<Integer> sortedFloors = roomsByFloor.keySet()
            .stream()
            .sorted()
            .collect(Collectors.toList());

        for (int i = 0; i < sortedFloors.size() - 1; i++) {
            List<Room> currentFloor = roomsByFloor.get(sortedFloors.get(i));
            List<Room> nextFloor = roomsByFloor.get(sortedFloors.get(i + 1));

            for (Room a : currentFloor) {
                for (Room b : nextFloor) {
                    // 调用 WeightGenerator 生成权值
                    int weight = WeightGenerator.getWeight(a.getRoomID(), b.getRoomID());
                    if (weight > 0) {
                        addEdge(a.getRoomID(), b.getRoomID(), weight);
                    }
                }
            }
        }
    }

    private void addEdge(int u, int v, int w) {
        adjList.get(u).put(v, w);
        inDegreeMap.put(v, inDegreeMap.get(v) + 1);
    }

    // Getter 方法
    public Map<Integer, Map<Integer, Integer>> getAdjList() { return adjList; }
    public Map<Integer, Integer> getInDegreeMap() { return inDegreeMap; }
    public Set<Integer> getAllRoomIDs() { return roomNodes.keySet(); }
}