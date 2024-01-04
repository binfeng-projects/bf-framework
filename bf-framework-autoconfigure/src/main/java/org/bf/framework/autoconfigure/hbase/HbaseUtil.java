package org.bf.framework.autoconfigure.hbase;

import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class HbaseUtil {

    public static final String ROW_KEY = "rowKey";

    /**
     * @param tableName 表名 一般namespace:tableName
     * @param familyByte  列簇 对业务来说应该是一个常量，要用Hbase的Bytes.toBytes(string)转后传入
     * @param rowKey    行键
     * @param qualifier 列族中的列
     * @param value     列族中的列的值
     */
    public static void insertCell(Connection conn,String tableName,byte[] familyByte, String rowKey, String qualifier, String value) {
        commonAssert(tableName,rowKey,familyByte);
        Assert.notBlank(qualifier,"qualifier empty");
        Assert.notBlank(value,"qualifier value empty");
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            putAddColumn(familyByte,put,qualifier,value);
            table.put(put);
        } catch (IOException e) { //中断异常抓掉，向上抛出运行时异常，由上层决定是否处理
            log.error("insertCell error", e);
            throw new IllegalStateException("hbase insertCell error");
        } finally {
            IOUtils.closeQuietly(table);
        }
    }

    /**
     * @param tableName  表名  一般namespace:tableName
     * @param familyByte  列簇 对业务来说应该是一个常量，要用Hbase的Bytes.toBytes(string)转后传入
     * @param rowKey     行键
     * @param qualifierMap map中的key和value分别是qualifierName:qualifierValue
     */
    public static void insertRow(Connection conn,String tableName, byte[] familyByte, String rowKey, Map<String,String> qualifierMap) {
        commonAssert(tableName,rowKey,familyByte);
        Assert.notEmpty(qualifierMap,"qualifierMap empty");
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Put put = formatPut(familyByte,Bytes.toBytes(rowKey),qualifierMap);
            table.put(put);
        } catch (IOException e) {
            log.error("insertRow error", e);
            throw new IllegalStateException("hbase insertRow error");
        } finally {
            IOUtils.closeQuietly(table);
        }
    }

    /**
     * 插入多行数据
     * @param tableName  表名  一般namespace:tableName
     * @param familyByte  列簇 对业务来说应该是一个常量，要用Hbase的Bytes.toBytes(string)转后传入
     * @param rowMap   外层map中key是 rowKey  value是内层map，内层map中的key和value分别是qualifierName:qualifierValue
     */
    public static void insertRowBatch(Connection conn,String tableName,byte[] familyByte,Map<String,Map<String,String>> rowMap) {
        Assert.notBlank(tableName,"tableName empty");
        Assert.notNull(familyByte,"family empty");
        Assert.notEmpty(rowMap,"rowMap empty");
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            //走批量插入接口
            List<Put> putList = new ArrayList<Put>(rowMap.size());
            Iterator<Map.Entry<String,Map<String,String>>> it = rowMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String,Map<String,String>> entry = it.next();
                Put put = formatPut(familyByte,Bytes.toBytes(entry.getKey()),entry.getValue());
                putList.add(put);
            }
            table.put(putList);
        } catch (IOException e) {
            log.error("insertRowBatch error", e);
            throw new IllegalStateException("hbase insertRowBatch error");
        } finally {
            IOUtils.closeQuietly(table);
        }
    }


    public static Put formatPut(byte[] familyByte,byte[] rowKeyByte,Map<String,String> qualifierMap){
        if(familyByte == null || rowKeyByte == null || MapUtils.isEmpty(qualifierMap)){
            return null;
        }
        Iterator<Map.Entry<String,String>> it = qualifierMap.entrySet().iterator();
        Put put = new Put(rowKeyByte);
        while (it.hasNext()) {
            Map.Entry<String,String> entry = it.next();
            putAddColumn(familyByte,put,entry.getKey(),entry.getValue());
        }
        return put;
    }

    public static Put putAddColumn(byte[] familyByte,Put put,String qualifier, String value){
        if(familyByte == null || StringUtils.isEmpty(qualifier) || StringUtils.isEmpty(value)){
            return put;
        }
        put.addColumn(
            // 列族
            familyByte,
            // 列
            Bytes.toBytes(qualifier),
            // 列的值
            Bytes.toBytes(value));
        return put;
    }


    /**
     * @param tableName  表名  一般namespace:tableName
     * @param familyByte  列簇 对业务来说应该是一个常量，要用Hbase的Bytes.toBytes(string)转后传入
     * @param scan  传入自定义的scan对象
     * @return 返回map的列表，一个map是一行记录。key是字段名，value是字段值。“rowKey”是该条数据行键的key
     */
    public static List<Map<String,String>> getWithScan(Connection conn,String tableName, byte[] familyByte, Scan scan){
        Assert.notBlank(tableName,"tableName empty");
        Assert.notNull(familyByte,"family empty");
        Table table = null;
        ResultScanner scanner = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            //指定列簇
            scanner = table.getScanner(scan);
            List<Map<String,String>> resultList = Lists.newArrayList();
            if(scanner == null){
                return resultList;
            }
            Iterator<Result> iterator = scanner.iterator();
            while (iterator.hasNext()) {
                Result result = iterator.next();
                Map<String,String> rowMap = fommatResultAsMap(result,familyByte);
                if(MapUtils.isNotEmpty(rowMap)){
                    resultList.add(rowMap);
                }
            }
            return resultList;
        } catch (IOException e) {
            log.error("getWithScan error", e);
            throw new IllegalStateException("hbase getWithScan error");
        } finally {
            IOUtils.closeQuietly(table);
            IOUtils.closeQuietly(scanner);
        }
    }

    /**
     * 查询键为rowKey，返回一行数据, 通过qualifiers指定返回那些列标识数据
     *
     * 如果你确保你的所有字段（注意是所有）都是用string存入的，那么直接使用这个方法，否则用底层的hbase的Result自己转
     *    （比如底层存的是Long，用toLong。是Int，用toInt）
     *      * 示例代码如下
     *      *
     *      *         Result res = getRow(tableName,rowKey,familyName,qualifiers);
     *      *         Map<byte[], byte[]> result = res.getFamilyMap(familyByte);
     *      *         Iterator<Map.Entry<byte[], byte[]>> it = result.entrySet().iterator();
     *      *         while (it.hasNext()) {
     *      *             Map.Entry<byte[], byte[]> entry = it.next();
     *      *             byte[] value = entry.getValue();
     *      *             String key = Bytes.toString(entry.getKey());
     *      *             if("uid".equals(key)){    //
     *      *                 resultMap.put(key,Bytes.toLong(value));
     *      *             } else if("mt".equals(key)){
     *      *                 resultMap.put(key,Bytes.toInt(value));
     *      *             }else {
     *      *                 resultMap.put(key,Bytes.toString(value));
     *      *             }
     *      *         }
     * @param tableName  表名
     * @param familyByte  列簇 对业务来说应该是一个常量，要用Hbase的Bytes.toBytes(string)转后传入
     * @param rowKey     行键
     * @param qualifiers 需要返回的列名，可以为空，为空则所有列全返回
     * @return 返回Map，key是字段名，value是字段值。“rowKey”是该条数据行键的key
     */
    public static Map<String,String> getRowAsMap(Connection conn,String tableName,byte[] familyByte,String rowKey,List<String> qualifiers) {
        commonAssert(tableName,rowKey,familyByte);
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Get get = formmatGet(familyByte,Bytes.toBytes(rowKey),qualifiers);
            Result res =  table.get(get);
            return fommatResultAsMap(res,familyByte);
        } catch (IOException e) {
            log.error("getRowAsMap error", e);
            throw new IllegalStateException("hbase getRowAsMap error");
        } finally {
            IOUtils.closeQuietly(table);
        }
    }

    /**
     * 根据rowKey的列表查询数据，返回多行数据。通过qualifiers指定返回那些列标识(mysql列)数据
     *
     * @param tableName  表名
     * @param rowKeys rowKey列表
     * @param familyByte 列簇  对业务来说应该是一个常量，要用Hbase的Bytes.toBytes(string)转后传入
     * @param qualifiers 需要返回的列名，可以为空，为空则所有列全返回
     * @return 返回map的列表，一个map是一行记录。key是字段名，value是字段值。“rowKey”是该条数据行键的key
     */
    public static List<Map<String,String>> getRowsAsMapList(Connection conn,String tableName,byte[] familyByte,List<String> rowKeys,List<String> qualifiers) {
        Assert.notBlank(tableName,"tableName empty");
        Assert.notEmpty(rowKeys,"rowKeys empty");
        Assert.notNull(familyByte,"familyByte null");
        Table table = null;
        try {
            List<Get> getList = Lists.newArrayList();
            for (String rowKey : rowKeys) {
                getList.add(formmatGet(familyByte,Bytes.toBytes(rowKey),qualifiers));
            }
            table = conn.getTable(TableName.valueOf(tableName));
            Result[] results = table.get(getList);
            List<Map<String,String>> resultList = Lists.newArrayList();
            if(results == null || results.length <= 0){
                return resultList;
            }
            for (Result re:results) {
                Map<String,String> resultMap = fommatResultAsMap(re,familyByte);
                if(MapUtils.isNotEmpty(resultMap)){
                    resultList.add(resultMap);
                }
            }
            return resultList;
        } catch (IOException e) {
            log.error("getRowsAsMapList error", e);
            throw new IllegalStateException("hbase getRowsAsMapList error");
        } finally {
            IOUtils.closeQuietly(table);
        }
    }

    public static Get formmatGet(byte[] familyByte,byte[] rowKeyByte,List<String> qualifiers){
        if(familyByte == null || rowKeyByte == null){
            return null;
        }
        Get get = new Get(rowKeyByte);
        //指定列簇
        get.addFamily(familyByte);
        //指定列
        if (CollectionUtils.isEmpty(qualifiers)){
            return get;
        }
        for (String column : qualifiers) {
            get.addColumn(familyByte, Bytes.toBytes(column));
        }
        return get;
    }

    /**
     *
     * @param res 一条结果
     * @param familyByte 列簇 对业务来说应该是一个常量，要用Hbase的Bytes.toBytes(string)转后传入
     * @return
     */
    public static Map<String,String> fommatResultAsMap(Result res,byte[] familyByte){
        Map<String,String> resultMap = Maps.newHashMap();
        if(res == null || res.isEmpty()){
            return resultMap;
        }
        Map<byte[], byte[]> result = res.getFamilyMap(familyByte);
        Iterator<Map.Entry<byte[], byte[]>> it = result.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<byte[], byte[]> entry = it.next();
            resultMap.put(Bytes.toString(entry.getKey()),Bytes.toString(entry.getValue()));

        }
        resultMap.put(ROW_KEY,Bytes.toString(res.getRow()));
        return resultMap;
    }



    /**
     * 删除多行
     *
     * @param tableName 表名
     * @param rowKeys   待删除列的键
     */
    public static void deleteRows(Connection conn,String tableName, List<String> rowKeys) {
        Assert.notBlank(tableName,"tableName empty");
//        Assert.notBlank(rowKey,"rowKey empty");
        Table table = null;
        try {
            if (null != rowKeys && rowKeys.size() > 0) {
                table = conn.getTable(TableName.valueOf(tableName));
                List<Delete> deleteList = new ArrayList<Delete>(rowKeys.size());
                Delete delete;
                for (String rowKey : rowKeys) {
                    delete = new Delete(Bytes.toBytes(rowKey));
                    deleteList.add(delete);
                }
                table.delete(deleteList);
            }
        } catch (IOException e) {
            log.error("deleteRows error", e);
            throw new IllegalStateException("hbase deleteRows error");
        } finally {
            IOUtils.closeQuietly(table);
        }
    }

    /**
     * 删除一行
     *
     * @param tableName 表名
     * @param rowKey    待删除列的键
     */
    public static void deleteRow(Connection conn,String tableName, String rowKey) {
        Assert.notBlank(tableName,"tableName empty");
        Assert.notBlank(rowKey,"rowKey empty");
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            table.delete(delete);
        } catch (IOException e) {
            log.error("deleteRow error", e);
            throw new IllegalStateException("hbase deleteRow error");
        } finally {
            IOUtils.closeQuietly(table);
        }
    }


    /**
     * 这三个字段必须不能为空，统一判断
     * @param tableName
     * @param rowKey
     * @param familyByte
     */
    private static void commonAssert(String tableName,String rowKey,byte[] familyByte){
        Assert.notBlank(tableName,"tableName empty");
        Assert.notBlank(rowKey,"rowKey empty");
        Assert.notNull(familyByte,"family null");
    }
}
