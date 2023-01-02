package com.example.trying.annotation.Database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.trying.Constant.ReferenceOptions;
/*
 * used to make an OneToMany relations between two classes  
 *  
 * */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
	//define the join table name or the default name will be used both table names table A_ table B
	String joinTableName() default "";
	//the referenced field in the referenced class  
	String referencedColumn() default "";
	//the field contribute in the relationship in the referencing class
	String chosenColumn() default "";
	//specify what will happen to ForignerColumn in case update of the referenced column on the the referenced class
	ReferenceOptions onUpdate() default ReferenceOptions.NO_ACT;
	//specify what will happen to ForignerColumn in case delete of the referenced column on the the referenced class
	ReferenceOptions onDelete() default ReferenceOptions.NO_ACT;
}
