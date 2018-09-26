package org.curefun.jdbc.transaction;
/** 
* @author chenlei: 
* @version 创建时间：2018年5月30日 下午7:16:24 
* 类说明 
*/

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transaction {
	// String value() default "";
}
