package me.amboo.algorithm;

import java.util.*;

// 拓扑排序
public class TopoSort {
    /**
     * 对传入的 AOV 网进行拓扑排序并返回结果
     * @param network 已经构建好的 AOV 网
     * @return 排序后的房间 ID 列表（拓扑序列）
     * @throws IllegalStateException 如果图中存在环，则抛出异常
     */

    public List<Integer> execute(AOVNetwork network) {
        List<Integer> result = new ArrayList<>();

        // 借助一个临时的 Map 来存储排序过程中动态变化的入度，以保护原 AOV 网数据（尤其是邻接表）不被破坏
        Map<Integer, Integer> tempInDegreeMap = new HashMap<>();

        // 存储入度为 0 的节点队列
        Queue<Integer> queue = new LinkedList<>();

        // Set接口：定义一个无重复元素的集合
        // 这里只是创建一个集合，塞下所有节点（房间）的ID
        Set<Integer> allRoomIDs = network.getAllRoomIDs(); 

        // 1. 初始化：为临时邻接表导入 入度的值，然后把所有入度为 0 的房间加入队列 quese
        for (Integer id : allRoomIDs) {
            int inDegree = network.getInDegree(id); //
            tempInDegreeMap.put(id, inDegree);
            if (inDegree == 0) {
                queue.offer(id);
            }
        }

        // 2. 处理队列：依次移除入度为 0 的节点，并更新其邻居的入度
        while (!queue.isEmpty()) {
            Integer currentID = queue.poll(); // 返回队列的首个节点并在队列中删除
            result.add(currentID);

            // 获取当前房间指向的所有后续房间
            List<Integer> neighbors = network.getNeighbors(currentID); 
            for (Integer neighborID : neighbors) {
                // 模拟移除 currentID 后，其后继节点的入度减 1
                int updatedInDegree = tempInDegreeMap.get(neighborID) - 1;
                tempInDegreeMap.put(neighborID, updatedInDegree);

                // 如果某个邻居的入度减为 0，则说明它的所有前置条件已满足，加入队列
                if (updatedInDegree == 0) {
                    queue.offer(neighborID);
                }
            }
        }

        // 3. 闭环检测：如果结果集中的节点数少于图中的总节点数，说明图中存在环（在楼层约束下理论上不会发生）
        // 如果成环，图中就会有拓扑排序后入度仍不为0的节点，该节点不会进入result列表，故长度会发生变化
        if (result.size() != allRoomIDs.size()) {
            throw new IllegalStateException("错误：该 AOV 网中存在环路，无法生成完整的拓扑序列。");
        }

        return result;
    }
}