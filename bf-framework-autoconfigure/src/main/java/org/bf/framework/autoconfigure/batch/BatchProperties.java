package org.bf.framework.autoconfigure.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.transaction.annotation.Isolation;

import static org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao.DEFAULT_TABLE_PREFIX;

/**
 */
@Slf4j
public class BatchProperties {

    public static final String DEFAULT_SCHEMA_LOCATION = "classpath:org/springframework/batch/core/schema-@@platform@@.sql";

    /**
     * 引用那个datasource实例
     */
    private String dataSourceRef;
    private String schema = DEFAULT_SCHEMA_LOCATION;
    private String tablePrefix = DEFAULT_TABLE_PREFIX;
    private Isolation isolationLevelForCreate = Isolation.SERIALIZABLE;
    private DatabaseInitializationMode initializeSchema = DatabaseInitializationMode.EMBEDDED;

    public String getTablePrefix() {
        return this.tablePrefix;
    }
    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }
    public Isolation getIsolationLevelForCreate() {
        return this.isolationLevelForCreate;
    }
    public void setIsolationLevelForCreate(Isolation isolationLevelForCreate) {
        this.isolationLevelForCreate = isolationLevelForCreate;
    }
    public DatabaseInitializationMode getInitializeSchema() {
        return this.initializeSchema;
    }
    public void setInitializeSchema(DatabaseInitializationMode initializeSchema) {
        this.initializeSchema = initializeSchema;
    }
    public String getDataSourceRef() {
        return dataSourceRef;
    }
    public void setDataSourceRef(String dataSourceRef) {
        this.dataSourceRef = dataSourceRef;
    }
    public String getSchema() {
        return schema;
    }
    public void setSchema(String schema) {
        this.schema = schema;
    }

}
