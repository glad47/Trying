package com.example.trying.annotation.Database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/*
 * used to make specific option for the database column 
 *  
 * */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	//name of the column or we are going to use the name of the field converted into snake format
	String name() default "";
	//the size of the column in the database 
	int size() default 0;
	//the number of digit after decimal only will be used for float 
	int afterDecimal () default 2;
	

}
