package com.example.trying.annotation.Database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/*
 * used to define an contribution to the constraint on database column 
 *  
 * */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForignerKeyPart {
	//the referenced class which the ForignerKey is referencing and it must be annotated with Table or JoinTable
	Class<?> referencedClass();
	//the referenced field in the referenced class 
	String referencedColumn() default "";
}
