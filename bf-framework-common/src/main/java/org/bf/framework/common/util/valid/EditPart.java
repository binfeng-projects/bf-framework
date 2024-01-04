package org.bf.framework.common.util.valid;

/**
 * 修改的的时候校验，一般建议把notNull,NotBlank等非空类加在这个组（如果业务上要求非空）
 * 这个主要是解决和Edit冲突的时候用，比如一般Edit不会要求所有字段必传。但有些字段又有修改需求。
 * 这个group和Edit组都会校验id不为空，走update。update的字段可能和Edit不同。再有其他冲突，需要自定义了
 */
public interface EditPart {
}
