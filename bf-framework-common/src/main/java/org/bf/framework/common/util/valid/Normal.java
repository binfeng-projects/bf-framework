package org.bf.framework.common.util.valid;

/**
 * 一般建议把length size等允许不传，但是如果传了就必须校验规范性的放这个组
 * 比如update修改的时候，我们一般根据用户传条件拼接修改，条件可以不传，但是
 * 传了就必须符合要求，还有query查询也是一个意思
 * 也就是一般增改查都需要校验的可以放这个组
 */
public interface Normal {
}
