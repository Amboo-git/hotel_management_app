package me.amboo.algorithm;

import me.amboo.model.Room;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AOV 网类：用于处理房间之间的逻辑先后关系
 */
public class AOVNetwork {
    // 邻接表：RoomID -> 邻接房间号列表
    private Map<Integer, List<Integer>> adjList = new HashMap<>();
    // 存储邻接表，表示每个房间的后继房间列表

    // 入度表：RoomID -> 入度数
    private Map<Integer, Integer> inDegreeMap = new HashMap<>();

    // 存储 Room 对象引用，方便后续获取房间详细信息
    private Map<Integer, Room> roomNodes = new HashMap<>();

    /**
     * 重构后的构建 AOV 网方法
     * * 业务规则：
     * 1. 节点间是否有边取决于 WeightGenerator.getWeight(id1, id2) 的返回值。
     * 2. 只有权值为正数（> 0）时才建立有向边。
     * 3. 层级限制：边只能从当前楼层指向紧邻的下一个更高楼层。
     * 例如：1楼 -> 2楼 (如果2楼存在)，2楼 -> 3楼 (如果3楼不存在则找4楼)。
     * * @param rooms 酒店所有房间列表
     */
    public void buildNetwork(List<Room> rooms) {
        // 1. 清空旧数据并初始化节点信息
        adjList.clear();
        inDegreeMap.clear();
        roomNodes.clear();

        for (Room r : rooms) {
            int rid = r.getRoomID();
            roomNodes.put(rid, r);
            adjList.put(rid, new ArrayList<>());
            inDegreeMap.put(rid, 0);
        }

        // 2. 将房间按楼层分组 (楼层计算逻辑：RoomID / 100)
        Map<Integer, List<Room>> roomsByFloor = rooms.stream()
                .collect(Collectors.groupingBy(r -> r.getRoomID() / 100));
                // collect 返回值: Map<K, List<T>>，键是分组规则的结果，值是属于该分组的元素列表

        // 3. 获取所有存在房间的楼层，并按升序排列
        List<Integer> sortedFloors = roomsByFloor.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
                // collect：将排序后的结果依次手机进本列表

        // 4. 遍历楼层，尝试在相邻的“有房楼层”之间建立边
        // 注意：循环到倒数第二个楼层即可，最高层不需要指向更高处
        for (int i = 0; i < sortedFloors.size() - 1; i++) {
            int currentFloorNum = sortedFloors.get(i);
            int nextFloorNum = sortedFloors.get(i + 1);

            List<Room> currentFloorRooms = roomsByFloor.get(currentFloorNum);
            List<Room> nextFloorRooms = roomsByFloor.get(nextFloorNum);

            for (Room roomA : currentFloorRooms) {
                for (Room roomB : nextFloorRooms) {
                    // 调用 WeightGenerator 判定权值
                    int weight = WeightGenerator.getWeight(roomA.getRoomID(), roomB.getRoomID());
                    
                    // 规则：正数记为有边，非正数（<=0）记为无边
                    if (weight > 0) {
                        addEdge(roomA.getRoomID(), roomB.getRoomID());
                    }
                }
            }
        }
    }

    /**
     * 添加有向边并更新入度表
     */
    private void addEdge(int u, int v) {
        adjList.get(u).add(v);
        inDegreeMap.put(v, inDegreeMap.get(v) + 1);
    }

    // 获取所有房间ID
    public Set<Integer> getAllRoomIDs() {
        return roomNodes.keySet();
    }

    // 获取特定房间的入度
    public int getInDegree(int roomID) {
        return inDegreeMap.getOrDefault(roomID, 0);
    }

    // 返回指定节点的邻接节点列表
    public List<Integer> getNeighbors(int roomID) {
        return adjList.getOrDefault(roomID, new ArrayList<>());
    }
}