package org.bf.framework.autoconfigure.hbase;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Scan;

import java.util.List;
import java.util.Map;

/**
 * 默认认为用户所有字段都是用string存入和读取的，那么可以使用此类封装的方法
 */
@Slf4j
public class HbaseProxy {

    private Connection connection;

    /**
     * hbase表名
     */
    private String tableName;

    /**
     * hbase family
     */
    private byte[] familyByte;

    public HbaseProxy(Connection connection) {
        this.connection = connection;
    }
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public byte[] getFamilyByte() {
        return familyByte;
    }

    public void setFamilyByte(byte[] familyByte) {
        this.familyByte = familyByte;
    }

    /**
     * 插入具体column（关系数据库意义上的某一行的某一列）
     * @param rowKey    行键
     * @param qualifier 列族中的列
     * @param value     列族中的列的值
     */
    public void insertCell( String rowKey, String qualifier, String value) {
        insertCell(tableName,familyByte,rowKey,qualifier,value);
    }

    /**
     * 插入一行数据（关系数据库意义上的行）
     * @param rowKey     行键
     * @param qualifierMap map中的key和value分别是qualifierName:qualifierValue
     */
    public void insertRow(String rowKey,Map<String,String> qualifierMap) {
        insertRow(tableName,familyByte,rowKey,qualifierMap);
    }

    /**
     * 插入多行数据
     * @param rowMap   外层map中key是 rowKey  value是内层map，内层map中的key和value分别是qualifierName:qualifierValue
     */
    public void insertRowBatch(Map<String,Map<String,String>> rowMap) {
        insertRowBatch(tableName,familyByte,rowMap);
    }

    /**
     * @param scan  传入自定义的scan对象
     * @return 返回map的列表，一个map是一行记录。key是字段名，value是字段值。“rowKey”是该条数据行键的key
     */
    public List<Map<String,String>> getWithScan(Scan scan){
        return getWithScan(tableName,familyByte,scan);
    }

    public Map<String,String> getRowAsMap(String rowKey,List<String> qualifiers) {
        return getRowAsMap(tableName,familyByte,rowKey,qualifiers);
    }

    public List<Map<String,String>> getRowsAsMapList(List<String> rowKeys,List<String> qualifiers) {
        return getRowsAsMapList(tableName,familyByte,rowKeys,qualifiers);
    }

    /**
     * 删除多行
     * @param rowKeys   待删除列的键
     */
    public void deleteRows(List<String> rowKeys) {
        deleteRows(tableName,rowKeys);
    }

    /**
     * 删除一行
     * @param rowKey    待删除列的键
     */
    public void deleteRow(String rowKey) {
        deleteRow(tableName,rowKey);
    }


    /**
     * 插入具体column（关系数据库意义上的某一行的某一列）
     */
    public void insertCell(String tableName,byte[] familyByte, String rowKey, String qualifier, String value) {
        HbaseUtil.insertCell(connection,tableName,familyByte,rowKey,qualifier,value);
    }

    /**
     * 插入一行数据（关系数据库意义上的行）
     */
    public void insertRow(String tableName,byte[] familyByte,String rowKey,Map<String,String> qualifierMap) {
        HbaseUtil.insertRow(connection,tableName,familyByte,rowKey,qualifierMap);
    }

    /**
     * 插入多行数据
     */
    public void insertRowBatch(String tableName,byte[] familyByte,Map<String,Map<String,String>> rowMap) {
        HbaseUtil.insertRowBatch(connection,tableName,familyByte,rowMap);
    }

    public List<Map<String,String>> getWithScan(String tableName,byte[] familyByte,Scan scan){
        return HbaseUtil.getWithScan(connection,tableName,familyByte,scan);
    }

    /**
     * 查询键为rowKey，返回一行数据, 通过qualifiers指定返回那些列标识数据
     */
    public Map<String,String> getRowAsMap(String tableName,byte[] familyByte,String rowKey,List<String> qualifiers) {
        return HbaseUtil.getRowAsMap(connection,tableName,familyByte,rowKey,qualifiers);
    }

    /**
     * 根据rowKey的列表查询数据，返回多行数据。通过qualifiers指定返回那些列标识。“rowKey”是该条数据行键的key
     */
    public List<Map<String,String>> getRowsAsMapList(String tableName,byte[] familyByte,List<String> rowKeys,List<String> qualifiers) {
        return HbaseUtil.getRowsAsMapList(connection,tableName,familyByte,rowKeys,qualifiers);
    }

    /**
     * 删除多行
     *
     * @param tableName 表名
     * @param rowKeys   待删除列的键
     */
    public void deleteRows(String tableName, List<String> rowKeys) {
        HbaseUtil.deleteRows(connection,tableName,rowKeys);
    }

    /**
     * 删除一行
     *
     * @param tableName 表名
     * @param rowKey    待删除列的键
     */
    public void deleteRow(String tableName, String rowKey) {
        HbaseUtil.deleteRow(connection,tableName,rowKey);
    }

}
