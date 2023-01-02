package com.example.trying.annotation.Database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.trying.Constant.ReferenceOptions;
/*
 * used to make an ManyToMany relations between two classes  
 *  
 * */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {
	//both id and mapby specify a single relation  
	//define the id of the relationships in one of the Tables
	String id() default "";
	//define the second table that belongs to that specific id 
	String mapBy() default "";
	//define the join table name or the default name will be used both table names table A_ table B
	String joinTableName() default "";
	//is the field that will identify the part of the relationship from the mapby  table 
	String referencedColumn() default "";
	//is the field that will identify the part of the relationship from the id  table
	String chosenColumn() default "";
	//specify what will happen to ForignerColumn in case update of the referenced column on the the referenced class
	ReferenceOptions onUpdate() default ReferenceOptions.NO_ACT;
	//specify what will happen to ForignerColumn in case delete of the referenced column on the the referenced class
	ReferenceOptions onDelete() default ReferenceOptions.NO_ACT;
}
