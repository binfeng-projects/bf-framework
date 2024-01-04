package org.bf.framework.test.base;

import org.bf.framework.common.util.JSON;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *  mock步骤
   1.把需要mock注入的服务ServiceA，ServiceB加上@InjectMocks
   2.把需要mock的服务ServiceC加上@Mock或@Spy
   3.执行测试方法前@BeforeEach初始化Mock注入，加上MockitoAnnotations.initMocks(this);
   4.调用测试接口前执行需要mock的方法，指定mock的返回值 when(serviceC.getUser(anyInt())).thenReturn(user);

  Mock与Spy的区别：
  如果@Mock不写形如when(...).thenReturn(...)的打桩，服务下的所有属性方法将返回0或null；
  而@Spy不写when(...).thenReturn(...)打桩则是按真实的代码执行。

  when(...).thenReturn(...)与doReturn(..).when(...)的区别：
  两者都返回mock结果，前者会调用真实代码后返回mock结果，后者直接返回mock结果。
  用法区别：when(serviceA.getUser(anyInt())).thenReturn(user)、doReturn(user).when(serviceA).getUser(anyInt())。
 */
//由配置文件控制，以免引起混乱
//@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public interface BaseCoreTest {

	@BeforeAll
	static void allBefore(){
		beforeAll();
	}

	/**
	 * 子类可覆盖
	 */
	static void beforeAll(){
	}
	@AfterAll
	static void allAfter(){
		afterAll();
	}

	/**
	 * 子类可覆盖
	 */
	static void afterAll(){
	}
	@BeforeEach
	default void eachBefore(){
		beforeEach();
	}

	/**
	 * 子类可覆盖
	 */
	default void beforeEach(){
	}
	@AfterEach
	default void eachAfter(){
		afterEach();
	}

	/**
     * 子类可覆盖
	 */
	default void afterEach(){
	}
	/**
	 * 可以用log，但目的想在控制台高亮显示（err会打印红色）
	 * @param o
	 */
	default void error(Object o){
		System.err.println(JSON.toJSONString(o));
	}
	default void info(Object o){
		System.out.println(JSON.toJSONString(o));
	}
}

