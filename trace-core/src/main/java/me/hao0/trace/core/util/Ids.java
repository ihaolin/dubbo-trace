package me.hao0.trace.core.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局ID生成工具：
 *
 * 采用64个二进制位来组成一个long型数字(也就是一个19位长的十进制的数字）
 * 第0位是符号位，始终为0
 * 用41位来表示时间戳(精确到ms) 2^41=2199023255552，以当前时间(eg:1387123200000)来算，可以支持70年不重复
 * 用10位表示当前服务器节点信息---->多节点部署不重复（多节点之间要注意时钟同步的问题）
 * 用12位表示计数器，每ms可支持生成2^12=4096个不重复的顺序号---->足够了，不行就从节点信息借2位来用，^_^
 * ------------------------------------------------------------
 * |0|  41bits time stamp  | 10bits node_id | 12bits counter |
 * ------------------------------------------------------------
 * 最后生成结果，eg: 5862810367993839695  5862810367993839699 ......
 **/
public class Ids {

    public static final int TOTAL_BITS_LENGTH = 63;

    public static final int TIME_BITS_LENGTH = 41;

    public static final int NODE_BITS_LENGTH = 10;

    private static final int COUNT_BITS_LENGTH = 12;

    private static final long TIME_BITS_MASK = (1L << TIME_BITS_LENGTH) - 1L;

    private static final int TIME_BITS_SHIFT_SIZE = TOTAL_BITS_LENGTH - TIME_BITS_LENGTH;

    private static final int NODE_BITS_MASK = (1 << NODE_BITS_LENGTH) - 1;

    private static final int MAX_COUNTER = 1 << COUNT_BITS_LENGTH;

    private int nodeId;

    private AtomicInteger counter;

    private long lastMillisecond;

    private static Ids instance = new Ids();

    private Ids() {
        this.nodeId = new Random().nextInt(1023) + 1;
        this.counter = new AtomicInteger(0);
    }

    public static long get() {
        long id = 0;
        //正常获取
        try {
            id = instance.nextTicket();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        //再试一次
        if (id == 0) {
            try {
                Thread.sleep(3);//等待3ms
                id = instance.nextTicket();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (id == 0) {
            //应急措施：返回当前时间戳 + 随机数
            return System.currentTimeMillis() + (int) Math.random() * 10000;
        } else {
            return id;
        }
    }

    private synchronized long nextTicket() {
        // 时钟校验
        long currentMillisecond = System.currentTimeMillis();
        if (currentMillisecond < lastMillisecond) {
            throw new RuntimeException("time is out of sync by " + (lastMillisecond - currentMillisecond) + "ms");
        }
        long ts = currentMillisecond & TIME_BITS_MASK;

        // 时间戳移位到前面41位的地方
        ts = ts << TIME_BITS_SHIFT_SIZE;

        if (currentMillisecond == lastMillisecond) {
            // 只有同一毫秒内，才使用小序号
            int count = counter.incrementAndGet();
            //如果计数器达到上限
            if (count >= MAX_COUNTER) {
                //同一毫秒内，直接抛异常，由调用方处理
                throw new RuntimeException("too much requests cause counter overflow");
            }
        }else{
            // 计数器重设为0,不同毫秒，没有必要使用中间值
            this.counter.set(0);
        }

        // 节点信息移位到指定位置
        int node = (nodeId & NODE_BITS_MASK) << COUNT_BITS_LENGTH;

        lastMillisecond = currentMillisecond;
        return ts + node + counter.get();
    }

    /**
     * 获取指定时间点上产生的ID最小值
     *
     * @param timeMs
     * @return
     */
    public static long timeStartId(long timeMs) {
        // 时钟校验
        long ts = timeMs & TIME_BITS_MASK;

        // 时间戳移位到前面41位的地方
        ts = ts << TIME_BITS_SHIFT_SIZE;
        return ts;
    }
}