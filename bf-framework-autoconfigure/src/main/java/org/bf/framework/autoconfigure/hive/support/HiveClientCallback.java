package org.bf.framework.autoconfigure.hive.support;

public interface HiveClientCallback<T> {
	T doInHive(HiveClient hiveClient) throws Exception;
}
