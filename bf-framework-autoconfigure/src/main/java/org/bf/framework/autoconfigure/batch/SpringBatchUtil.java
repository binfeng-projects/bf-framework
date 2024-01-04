package org.bf.framework.autoconfigure.batch;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.*;
import org.springframework.core.io.Resource;

import java.util.regex.Pattern;

public class SpringBatchUtil {

    public static <T> FlatFileItemReader<T> generateFileReader(Class<T> cls, Resource resource, Object tokenParam, String[] columuNames) {
//        --------------------------------------------------------FlatFileItemReader----------------------------------------------------
        LineTokenizer tokenizer = null;
        if (tokenParam == null || tokenParam instanceof String) {
            DelimitedLineTokenizer t = new DelimitedLineTokenizer();
            if (tokenParam != null) {
                t.setDelimiter((String) tokenParam);
            }
            t.setNames(columuNames);
            tokenizer = t;
        } else if (tokenParam instanceof Range[]) {
            FixedLengthTokenizer t = new FixedLengthTokenizer();
            t.setColumns((Range[]) tokenParam);
            t.setNames(columuNames);
            tokenizer = t;
        } else if (tokenParam instanceof Pattern) {
            RegexLineTokenizer t = new RegexLineTokenizer();
            t.setPattern((Pattern) tokenParam);
            t.setNames(columuNames);
            tokenizer = t;
        } else if (tokenParam instanceof LineTokenizer) {
            tokenizer = (LineTokenizer) tokenParam;
        } else {
            throw new RuntimeException("unknown tokenParam type");
        }
        //切割后组装成的对象，映射成什么
        BeanWrapperFieldSetMapper<T> fieldSetMapper = new BeanWrapperFieldSetMapper<T>();
        fieldSetMapper.setTargetType(cls);

        //行Mapper,一行行解析文件
        DefaultLineMapper<T> lineMapper = new DefaultLineMapper<T>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        //以上所有组装成reader，传入读取文件的地址和上面组装好的解析器
        FlatFileItemReader<T> reader = new FlatFileItemReader<T>();
        reader.setResource(resource);
        reader.setLineMapper(lineMapper);
        return reader;
    }
}
