package me.amboo.algorithm;

import java.util.*;

public class AOECalculator {

    public void calculateCriticalPath(AOENetwork network) {
        List<Integer> topoOrder = performTopologicalSort(network);
        if (topoOrder == null) return;

        Set<Integer> allIDs = network.getAllRoomIDs();
        Map<Integer, Integer> ve = new HashMap<>();
        Map<Integer, Integer> vl = new HashMap<>();

        // 1. 正向拓扑序列计算 ve (Earliest Event Time)
        allIDs.forEach(id -> ve.put(id, 0));
        for (int u : topoOrder) {
            for (Map.Entry<Integer, Integer> entry : network.getAdjList().get(u).entrySet()) {
                int v = entry.getKey();
                int weight = entry.getValue();
                if (ve.get(v) < ve.get(u) + weight) {
                    ve.put(v, ve.get(u) + weight);
                }
            }
        }

        // 2. 确定总耗时 (汇点的 ve 值)
        int maxTime = ve.values().stream().max(Integer::compare).orElse(0);
        System.out.println("最短总耗时（完成所有查房的最早时间）: " + maxTime);

        // 3. 逆向拓扑序列计算 vl (Latest Event Time)
        allIDs.forEach(id -> vl.put(id, maxTime));
        Collections.reverse(topoOrder);
        for (int u : topoOrder) {
            for (Map.Entry<Integer, Integer> entry : network.getAdjList().get(u).entrySet()) {
                int v = entry.getKey();
                int weight = entry.getValue();
                if (vl.get(u) > vl.get(v) - weight) {
                    vl.put(u, vl.get(v) - weight);
                }
            }
        }

        // 4. 计算活动 e 和 l，筛选关键活动与路径
        System.out.println("\n--- 关键活动与路径 ---");
        Collections.reverse(topoOrder); // 恢复正序
        for (int u : topoOrder) {
            for (Map.Entry<Integer, Integer> entry : network.getAdjList().get(u).entrySet()) {
                int v = entry.getKey();
                int weight = entry.getValue();
                
                int e = ve.get(u);           // 活动最早开始
                int l = vl.get(v) - weight;  // 活动最迟开始

                if (e == l) {
                    System.out.printf("关键活动: 房间[%d] -> 房间[%d], 耗时: %d\n", u, v, weight);
                }
            }
        }
    }

    private List<Integer> performTopologicalSort(AOENetwork network) {
        // 复用之前 AOV 的 Kahn 算法逻辑
        List<Integer> result = new ArrayList<>();
        Map<Integer, Integer> tempInDegree = new HashMap<>(network.getInDegreeMap());
        Queue<Integer> queue = new LinkedList<>();

        tempInDegree.forEach((id, degree) -> {
            if (degree == 0) queue.offer(id);
        });

        while (!queue.isEmpty()) {
            int u = queue.poll();
            result.add(u);
            network.getAdjList().get(u).keySet().forEach(v -> {
                tempInDegree.put(v, tempInDegree.get(v) - 1);
                if (tempInDegree.get(v) == 0) queue.offer(v);
            });
        }
        return result.size() == network.getAllRoomIDs().size() ? result : null;
    }
}