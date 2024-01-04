package org.bf.framework.common.base;

import org.bf.framework.common.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 根据id遍历全表，有很多场景需要遍历全表，比如同步ES,抽象下
 * @param <PK>
 * @param <E>
 */
public interface StartEndSelect<PK extends Number,E extends PkAble<PK>> {

    long PAGE_SIZE = 1000;

    List<E> selectStartEnd(Number startId, Number endId);

    List<E> selectStartLimit(Number startId, Long limit);

    Long maxId();


    /**
     * @param startId 起始id,避免扫描过大，根据实际业务
     * @param consumerFun 消费函数，可以具体写拿到数据后对逻辑
     */
    default void doScan(Long startId,Long endId,boolean cocurrency,Consumer<E> consumerFun){
        if(startId == null){
            //线上最小值
            startId = 0L;
        }
        int threadNum = 8;
        if(null == endId || endId <= 0){
            endId = maxId();
        }
        ExecutorService executor = null;
        try {
            if(cocurrency){
                executor = new ThreadPoolExecutor(threadNum ,threadNum,3, TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(1000),new ThreadPoolExecutor.CallerRunsPolicy());
            }
            Long cnt = endId - startId + 1;
            long perThreadCnt = cnt / threadNum;
            if (perThreadCnt <= 0) { //如果数量太少，少于线程数，说明一个线程就可以跑完
                scanStartEnd(startId, endId, consumerFun);
            } else {
                for (long i = startId; i < endId; i += perThreadCnt) {
                    Long threadEndTemp = i + perThreadCnt;
                    if (threadEndTemp >= endId) {
                        threadEndTemp = endId;
                    }
                    final Long threadEnd = threadEndTemp;
                    final Long threadStart = i;
                    if (cocurrency) {
                        executor.execute(() -> scanStartEnd(threadStart, threadEnd, consumerFun));
                    } else {
                        scanStartEnd(threadStart, threadEnd, consumerFun);
                    }
                }
            }
        }catch (Exception e){
            System.err.println("------------({})同步出错 -----------" + this.getClass().getName());
            e.printStackTrace();
        }finally {
            if(null != executor){
                executor.shutdown();
                try {
                    while (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                        // ignore
                    }
                }catch (InterruptedException e){
                    System.err.println("------------executor.awaitTermination出错 -----------" + this.getClass().getName());
                }
                System.out.println("------------ 全表扫描完毕 -----------");
            }
        }
    }


//    /**
//     * @param startId 起始id
//     * @param endId 结束id
//     */
//    default void scanStartLimit(Long startId, Long endId, Consumer<DTO> consumerFun){
//        System.err.println("scan start " + startId + " end:" + endId);
//        //每次只查pagesize条
//        out : for (long i = startId; i < endId;) {
//            try {
//                List<P> list = selectStartLimit(i,PAGE_SIZE);
//                //按照id排序的，如果已经空了，没必要继续往后迭代了
//                if(CollectionUtils.isEmpty(list)){
//                    break;
//                }
//                System.err.println("select start " + i + " limit :" + PAGE_SIZE + " totl fetch:" + list.size());
//                for (P p : list) {
//                    Object id = PropertyUtils.getSimpleProperty(p,"id");
//                    if (id == null) {
//                        continue;
//                    }
//                    if(id instanceof Number){
//                        Number n = (Number)(id);
//                        //不断赋值给i,往后迭代
//                        i = n.longValue();
//                        if(i >= endId){
//                            //不属于自己范围的数据不处理，交给别的线程处理，避免冲突
//                            break out;
//                        }
//                    }
//                    else{
//                        //id 必须是个数字，否则不处理
//                        continue;
//                    }
//                    try {
//                        consumerFun.accept(p);
//                    }catch (Exception e){
//                        e.printStackTrace();
//
//                    }
//                }
//            }catch (Exception e){
//                System.err.println("------------({})同步出错 -----------" + this.getClass().getName());
//                e.printStackTrace();
//                break;
//            }
//        }
//    }

    /**
     * @param startId 起始id
     * @param endId 结束id
     */
    default void scanStartEnd(Long startId, Long endId, Consumer<E> consumerFun){
        //每次只查pagesize条
        for (long i = startId; i < endId; i += PAGE_SIZE) {
            Long sqlEnd = i + PAGE_SIZE;
            if (sqlEnd >= endId) {
                sqlEnd = endId;
            }
            try {
                List<E> list = selectStartEnd(i,sqlEnd);
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                for (E p : list) {
                    try {
                        consumerFun.accept(p);
                    }catch (Exception e){
                        System.err.println("------------scanStartEnd consumerFun.accept error-----------" + this.getClass().getName());
                    }
                }
            }catch (Exception e){
                System.err.println("------------({})scanStartEnd出错 -----------" + this.getClass().getName());
                e.printStackTrace();
            }
        }
    }
}
