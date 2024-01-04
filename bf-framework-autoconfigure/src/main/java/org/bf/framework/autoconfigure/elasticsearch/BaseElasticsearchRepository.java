package org.bf.framework.autoconfigure.elasticsearch;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.HighlightParameters;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.io.Serializable;

public interface BaseElasticsearchRepository<E extends BaseElasticsearchEntity<PK>, PK extends Serializable> extends ElasticsearchRepository<E,PK> {

//	/**
//	 * 通过描述内容来搜索
//	 */
//	@Highlight(fields = {
//			@HighlightField(name = "title", parameters = @HighlightParameters(requireFieldMatch = false)),
//			@HighlightField(name = "remark", parameters = @HighlightParameters(requireFieldMatch = false)),
//			@HighlightField(name = "description", parameters = @HighlightParameters(requireFieldMatch = false)),
//	})
//	SearchPage<E> findByDescriptiveContent(String descriptiveContent, Pageable pageable);

}