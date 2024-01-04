package org.bf.framework.autoconfigure.hadoop;

import org.apache.hadoop.conf.Configuration;

public class HadoopProxy {
    private Configuration configuration;

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
