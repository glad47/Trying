package com.example.trying.annotation.Database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.trying.Constant.ReferenceOptions;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToOne {
	String joinTableName() default "";
	String referencedColumn() default "";
	String chosenColumn() default "";
	String forignerName() default "";
	ReferenceOptions onUpdate() default ReferenceOptions.NO_ACT;
	ReferenceOptions onDelete() default ReferenceOptions.NO_ACT;
}
