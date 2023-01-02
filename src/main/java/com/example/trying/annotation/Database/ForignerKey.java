package com.example.trying.annotation.Database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.trying.Constant.ReferenceOptions;



/*
 * used to define a constraint on database column of type ForignerKey 
 *  
 * */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForignerKey {
	// the name of the constraint or the default value of fk_table A_Table B will be used
	String name() default "";
	//the referenced class which the ForignerKey is referencing and it must be annotated with Table or JoinTable  
	Class<?> referencedClass();
	//the referenced field in the referenced class  
	String referencedColumn() default "";
	//specify what will happen to ForignerColumn in case update of the referenced column on the the referenced class  
	ReferenceOptions onUpdate() default ReferenceOptions.NO_ACT;
	//specify what will happen to ForignerColumn in case delete of the referenced column on the the referenced class
	ReferenceOptions onDelete() default ReferenceOptions.NO_ACT;

}


