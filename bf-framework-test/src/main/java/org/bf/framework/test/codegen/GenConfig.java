package org.bf.framework.test.codegen;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class GenConfig {
    private String packageCore;
    private String appName;
    private String workspace;
    private List<String> middlewarePrefix;
}
