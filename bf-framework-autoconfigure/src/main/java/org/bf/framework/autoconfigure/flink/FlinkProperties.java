package org.bf.framework.autoconfigure.flink;

import org.bf.framework.common.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FlinkProperties {
    /**
     * flink source instance,逗号分割
     */
    private String sourceInstanceRef;
    /**
     * flink sink instance,逗号分割
     */
    private String sinkInstanceRef;
    private Set<String> sourceSet;
    private Set<String> sinkSet;

    public Set<String> getSourceSet() {
        return sourceSet;
    }

    public Set<String> getSinkSet() {
        return sinkSet;
    }

    public String getSourceInstanceRef() {
        return sourceInstanceRef;
    }

    public void setSourceInstanceRef(String sourceInstanceRef) {
        this.sourceInstanceRef = sourceInstanceRef;
        if(sourceInstanceRef!= null && !sourceInstanceRef.isBlank()) {
            sourceSet = new HashSet<>();
            Arrays.stream(sourceInstanceRef.split(",")).map(s -> s.trim().toLowerCase()).filter(StringUtils::isNotBlank).forEach(sourceSet::add);
        }
    }

    public String getSinkInstanceRef() {
        return sinkInstanceRef;
    }

    public void setSinkInstanceRef(String sinkInstanceRef) {
        this.sinkInstanceRef = sinkInstanceRef;
        if(sinkInstanceRef!= null && !sinkInstanceRef.isBlank()) {
            sinkSet = new HashSet<>();
            Arrays.stream(sinkInstanceRef.split(",")).map(s -> s.trim().toLowerCase()).filter(StringUtils::isNotBlank).forEach(sinkSet::add);
        }
    }
}
