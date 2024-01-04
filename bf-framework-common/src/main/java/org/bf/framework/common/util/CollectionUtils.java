package org.bf.framework.common.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ArrayUtil;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

public class CollectionUtils {
    public static boolean isEmpty(Collection<?> c){
        return CollectionUtil.isEmpty(c);
    }
    public static boolean isNotEmpty(Collection<?> c){
        return !isEmpty(c);
    }
    /**
     * 支持List，Collection Iterator ,Array,其实也支持String，但string建议用blank判断，不要用empty
     * 如果msgs不为空 会执行断言，抛出异常，异常的msg 就是传入的 msg 拼上公共msg,不能为null啥的
     */
    public static boolean allEmpty(Object... args) {
        return ArrayUtil.isAllEmpty(args);
    }
    /**
     * 支持List，Collection Iterator ,Array,其实也支持String，但string建议用blank判断，不要用empty
     * 如果msgs不为空 会执行断言，抛出异常，异常的msg 就是传入的 msg 拼上公共msg,不能为null啥的
     */
    public static boolean hasEmpty(Object... args) {
        return ArrayUtil.hasEmpty(args);
    }
    public static <T> HashSet<T> newHashSet() {
        return CollectionUtil.newHashSet();
    }
    public static <T> HashSet<T> newHashSet(T... ts) {
        return CollectionUtil.newHashSet(ts);
    }
    public static <T> HashSet<T> newHashSet(Collection<T> collection) {
        return newHashSet(false, collection);
    }

    public static <T> HashSet<T> newHashSet(boolean isSorted, Collection<T> collection) {
        return CollectionUtil.newHashSet(isSorted,collection);
    }

    public static <T> HashSet<T> newHashSet(boolean isSorted, Iterator<T> iter) {
        return CollectionUtil.newHashSet(isSorted,iter);
    }

    public static <T> HashSet<T> newHashSet(boolean isSorted, Enumeration<T> enumeration) {
        return CollectionUtil.newHashSet(isSorted,enumeration);
    }

    // ----------------------------------------------------------------------------------------------- List
    public static <T> List<T> list(boolean isLinked) {
        return CollectionUtil.list(isLinked);
    }

    public static <T> List<T> list(boolean isLinked, T... values) {
        return CollectionUtil.list(isLinked,values);
    }

    /**
     * 新建一个List
     *
     * @param <T>        集合元素类型
     * @param isLinked   是否新建LinkedList
     * @param collection 集合
     * @return List对象
     * @since 4.1.2
     */
    public static <T> List<T> list(boolean isLinked, Collection<T> collection) {
        return CollectionUtil.list(isLinked,collection);
    }
    public static <T> List<T> list(boolean isLinked, Iterable<T> iterable) {
        return CollectionUtil.list(isLinked,iterable);
    }

    public static <T> List<T> list(boolean isLinked, Iterator<T> iter) {
        return ListUtil.list(isLinked, iter);
    }

    public static <T> List<T> list(boolean isLinked, Enumeration<T> enumeration) {
        return ListUtil.list(isLinked, enumeration);
    }

    public static <T> ArrayList<T> newArrayList(T... values) {
        return ListUtil.toList(values);
    }

    public static <T> ArrayList<T> toList(T... values) {
        return ListUtil.toList(values);
    }

    public static <T> ArrayList<T> newArrayList(Collection<T> collection) {
        return ListUtil.toList(collection);
    }

    public static <T> ArrayList<T> newArrayList(Iterable<T> iterable) {
        return ListUtil.toList(iterable);
    }

    public static <T> ArrayList<T> newArrayList(Iterator<T> iterator) {
        return ListUtil.toList(iterator);
    }

    /**
     * 新建一个ArrayList<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>         集合元素类型
     * @param enumeration {@link Enumeration}
     * @return ArrayList对象
     * @since 3.0.8
     */
    public static <T> ArrayList<T> newArrayList(Enumeration<T> enumeration) {
        return ListUtil.toList(enumeration);
    }

    // ----------------------------------------------------------------------new LinkedList

    /**
     * 新建LinkedList
     *
     * @param values 数组
     * @param <T>    类型
     * @return LinkedList
     * @since 4.1.2
     */
    @SafeVarargs
    public static <T> LinkedList<T> newLinkedList(T... values) {
        return ListUtil.toLinkedList(values);
    }

    /**
     * 新建一个CopyOnWriteArrayList
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return {@link CopyOnWriteArrayList}
     */
    public static <T> CopyOnWriteArrayList<T> newCopyOnWriteArrayList(Collection<T> collection) {
        return ListUtil.toCopyOnWriteArrayList(collection);
    }

    /**
     * 新建{@link BlockingQueue}<br>
     * 在队列为空时，获取元素的线程会等待队列变为非空。当队列满时，存储元素的线程会等待队列可用。
     *
     * @param <T>      集合类型
     * @param capacity 容量
     * @param isLinked 是否为链表形式
     * @return {@link BlockingQueue}
     * @since 3.3.0
     */
    public static <T> BlockingQueue<T> newBlockingQueue(int capacity, boolean isLinked) {
        final BlockingQueue<T> queue;
        if (isLinked) {
            queue = new LinkedBlockingDeque<>(capacity);
        } else {
            queue = new ArrayBlockingQueue<>(capacity);
        }
        return queue;
    }

    public static <T> T[] toArray(Collection<T> collection, Class<T> componentType) {
        return ArrayUtil.toArray(collection,componentType);
    }
}
