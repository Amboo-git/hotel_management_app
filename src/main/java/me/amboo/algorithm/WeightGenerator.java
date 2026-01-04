package me.amboo.algorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class WeightGenerator {
    // 使用 Map 缓存权值：Key 为 "房间A-房间B"，Value 为生成的随机权值
    private static final Map<String, Integer> weightCache = new HashMap<>();

    public static int getWeight(int roomID1, int roomID2) {
        String key = roomID1 + "-" + roomID2;

        // 如果缓存中不存在权值，生成新权值并缓存；否则直接返回
        if (!weightCache.containsKey(key)) {
            int weight = ThreadLocalRandom.current().nextInt(-20, 41);
            weightCache.put(key, weight);
        }
        return weightCache.get(key);
    }

    // 提供一个清除缓存的方法，方便管理员在需要时重新随机化整个酒店的图结构
    public static void resetWeights() {
        weightCache.clear();
        System.out.println(">> 权值缓存已清空，下次将重新随机生成。");
    }
}

