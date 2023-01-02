package com.example.trying;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.trying.annotation.Database.ForignerKey;
import com.example.trying.annotation.Database.JoinTable;
import com.example.trying.annotation.Database.Table;
import com.example.trying.utils.GFG;
/*
 * class responsible for handling the dependencies
 * 
 */
public class DependencyHandler {
	
	// first order in the schema files (always at the begging)
			// only classes annotated with Table annotation (tables in the database)
			 Map<String, String> tablesMap= new HashMap<>();
			
			//store the ordered tables from the one to be created first to the least to be created (Table annotated)
			 List<String> orderedTables= new ArrayList<>();
			
			// store the alter table sql in case of equal dependency and it used for removing these equal statement
			 List<String> altedTableStatments= new ArrayList<>();
			
			//store the already handledEqualDepedency to process it once for inner and outer classes
			 List<String> memoryOfHandlingEqualDependency= new ArrayList<>();
			
			
			// lower order(at the end) than joinTablesStringBuilder in the schema file (join tables in the database)
			// joinTable annotated with JoinTable annotation (the referencing class is just annotated with JoinTable)
			 Map<String, String> joinTablesMap= new HashMap<>();
			
			
			//store the ordered tables from the one to be created first to the least to be created (JoinTable annotated)
			 List<String> orderedJoinTables= new ArrayList<>();
	   
			
			
			public DependencyHandler( Map<String, String> tablesMap,List<String> orderedTables,List<String> altedTableStatments,
					List<String> memoryOfHandlingEqualDependency,Map<String, String> joinTablesMap,List<String> orderedJoinTables ) {
					this.tablesMap= tablesMap;
					this.orderedTables= orderedTables;
					this.altedTableStatments= altedTableStatments;
					this.memoryOfHandlingEqualDependency= memoryOfHandlingEqualDependency;
					this.joinTablesMap= joinTablesMap;
					this.orderedJoinTables= orderedJoinTables;
	        }
	
	
		
		
		
	public Map<String, String> getTablesMap() {
				return tablesMap;
			}





			public List<String> getOrderedTables() {
				return orderedTables;
			}





			public List<String> getAltedTableStatments() {
				return altedTableStatments;
			}





			public List<String> getMemoryOfHandlingEqualDependency() {
				return memoryOfHandlingEqualDependency;
			}





			public Map<String, String> getJoinTablesMap() {
				return joinTablesMap;
			}





			public List<String> getOrderedJoinTables() {
				return orderedJoinTables;
			}





		//this function is to remove the equal dependencies by changing the schema for the outer class
		//and create the alter sql statement for the outer class to include that forignerKey later  
		//the usedMap it used for generalization of the function to be used with tableMap and joinTableMap 
		public void removingEqualDependencies(String outClazz,String outClassDBName, String innerClazz,Map<String, String> usedMap) {
			StringBuilder str= new StringBuilder(usedMap.get(outClazz));
			// from where the ForignerKeys part of the schema starts of the outer class 
			int index= str.indexOf("CONSTRAINT");
			if(index == -1) {
				return;
			}
			String newString = "";
			// the previous part include the primary part as well but want only to get the ForignerKey part 
			newString = str.substring(index, str.indexOf("PRIMARY"));
			StringBuilder str2= new StringBuilder(newString);
			//this variable will store the part of the constraints that will rewrite 
			// the current part of the outer class   
			StringBuilder newPart= new StringBuilder();
			// find the constraint in outer class's schema that referencing the inner class
			String res= findTheConstrant(str2, innerClazz, newPart);
			//replace the old part of constraint with the new part 
			str.replace(index,str.indexOf("PRIMARY") , newPart.toString());
			//make the alter statement of the outer class and add it to altedTableStatments that will be add later in schema file 
			res = "ALTER TABLE " + outClassDBName + " ADD " + res + ";\n"; 
			usedMap.put(outClazz,str.toString());
			altedTableStatments.add(res);
		}
		
		// find the constraint in outer class's schema that referencing the inner class
		// this function will be called recursively 
		public String findTheConstrant(StringBuilder str, String innerClassName, StringBuilder newStr) {
			// get the first constraints statements
			int startIndex= str.indexOf("CONSTRAINT");
			// stop the recursive in case there are no more constraint statements
			if(startIndex == -1) {
				return null;
			}
			// determine the end of the first constraint statement
			int lastIndex=str.indexOf(",") + 1;
			// get the first statement here 
		    StringBuilder str2 = new StringBuilder(str.substring(startIndex, lastIndex - 1));
		    // check weather it is really the statement that referencing the inner class 
		    int index =str2.indexOf("REFERENCES "+innerClassName);
		    // in case no 
		    if(index == -1) {
		    	//remove the statement from the main body the contains all the statements to 
		    	// only check others in the further tries 
		    	// call the function recursively 
		    	str.replace(startIndex, lastIndex, "");
		    	//add the removed statements so we can rewrite the the part of the constraints
		    	newStr.append(str2 + ",\n");
		    	String res= findTheConstrant(str, innerClassName,newStr);
		    	return res;
		    }
		    // in case yes 
		    str.replace(startIndex, lastIndex, "");
		   //add the rest statements so we can rewrite the the part of the constraints
		    newStr.append(str);
		    //return the required statements
			return str2.toString();
			
		}
		
		
		// find the correct order of the schema section due we have dependencies among the tables 
		// for example one table must created before another table for example case of forignerKey or joinTable
		// here only for Tables
		public <T> void findDependencyOrder(List<Class<?>> targetedClasses) {
			// loop through all the classes within the packages 
			for(Class<?> cs : targetedClasses) {
				if(cs.isAnnotationPresent(JoinTable.class)) {
					continue;
				}
				//get the dependencies of the class (what the table need to be created) only Table annotated
				getDependencyOfClass(cs);
			}
		}
		
		
		// find the correct order of the schema section due we have dependencies among the tables 
		// for example one table must created before another table for example case of forignerKey or joinTable
		// here only for JoinTables
		public <T> void findDependencyOrderJoinTable(List<Class<?>> targetedClasses) {
			for(Class<?> cs : targetedClasses) {
				if(cs.isAnnotationPresent(Table.class)) {
					continue;
				}
				//get the dependencies of the class (what the table need to be created) only JoinTable annotated
				getDependencyOfJoinTableClass(cs);
			}
		}
		
		
		//this function will check the mutual dependencies between two tables 
		// for example table A contains forignerKey referencing Table B 
		//and Table B contains a ForignerKey for Table A hence we have a problem 
		// of which one will be placed it schema in the schema file 
		// and therefore we decided to remove one of the forignerKey from the schema of one of the 
		// Table to alter the table at the end of the schema file and bring it back again
		//the outer class is the first class that encounter a equal dependency and contains the forignerKey referencing the other class 
		// and the inner class it is what the outer class referencing
		// the function return true -> to indicate to no need to process the forignerKey specified with it 
		// because it is already removed from the schema of the inner class temporary
		// false if there is not equal dependencies 
		public <T> boolean checkEqualDepedencies(Class<? extends T> outClazz,Class<? extends T> innerClazz, Map<String, String> usedMap) {
			//check if any of the classes are within the memory in case yes 
			//then return already handled, just need to be processed once 
			// because same class name can come with other class name then we form a pair 
			// of classes and to check this pair we do it like this  
			if(memoryOfHandlingEqualDependency.contains(outClazz.getSimpleName()) && 
					memoryOfHandlingEqualDependency.contains(innerClazz.getSimpleName())) {
				return true;
			}
			//we need to make sure each class's added once 
			// because same class name can come with other class name then we form a pair 
			// of classes 
			if(!memoryOfHandlingEqualDependency.contains(outClazz.getSimpleName())) {
				memoryOfHandlingEqualDependency.add(outClazz.getSimpleName());	
			}
			
			if(!memoryOfHandlingEqualDependency.contains(innerClazz.getSimpleName())) {
				memoryOfHandlingEqualDependency.add(innerClazz.getSimpleName());	
			}		
			
			// add the inner and the outer class to the memory
			for(Field fd : innerClazz.getDeclaredFields()) {
				if(fd.isAnnotationPresent(ForignerKey.class)) {
					ForignerKey forignerKey= fd.getAnnotation(ForignerKey.class);
					if(forignerKey.referencedClass().equals(outClazz)) {
						// there is equal dependency 
						String outClassName= outClazz.getSimpleName();
						String outClassDBName= getTheDatabaseTableName(outClazz);
						String innerClassName= getTheDatabaseTableName(innerClazz);
						// call the function to remove the equal dependencies 
						removingEqualDependencies(outClassName,outClassDBName,innerClassName, usedMap);
						return true;
					}
				}
			}
			
			
			return false;
		} 
		
		//Given a class get its table name in the database 
		public <T> String getTheDatabaseTableName(Class<? extends T> clazz) {
			String className = "";
			if(clazz.isAnnotationPresent(Table.class)) {
				Table tableAnnotaion= clazz.getAnnotation(Table.class);
				//in case the name attribute for the Table annotation is there then we use that name 
				if(tableAnnotaion != null && !tableAnnotaion.name().equals("")) {
					className=tableAnnotaion.name();
				}// just use the default name which is the class name converted into snake 
				else {
					className=GFG.camelToSnake(clazz.getSimpleName());
				}
				
			// the table is annotated with the JoinTable 	
			}else if(clazz.isAnnotationPresent(JoinTable.class)) {
				//get the name of the class that will be used in the schema 
				JoinTable tableAnnotaion= clazz.getAnnotation(JoinTable.class);
				if(tableAnnotaion != null && !tableAnnotaion.name().equals("")) {
					className=tableAnnotaion.name();
				}else {
					className=GFG.camelToSnake(clazz.getSimpleName());
				}
			}
			
			return className;
		}
		
		//get the dependencies of the class (what the table need to be created) only Table annotated
		public <T> void getDependencyOfClass(Class<? extends T> clazz) {
		//check weather the table name is already in orderedTables in case yes just return (because that will form and endless loop)
			if(orderedTables.contains(clazz.getSimpleName())) {
				return;
			}
			// loop through all fields 
			for(Field fd : clazz.getDeclaredFields()) {
				// each ForignerKey is form a dependency 
				if(fd.isAnnotationPresent(ForignerKey.class)) {
					ForignerKey forignerKey= fd.getAnnotation(ForignerKey.class);
					//check equal dependencies here create and then alter the tables 
					boolean eqaulDependencies = checkEqualDepedencies(clazz,forignerKey.referencedClass(), tablesMap);
					// indicate no need to process the forignerKey specified with it 
					// because it is already removed from the schema of the inner class temporary
					if(eqaulDependencies) {
				    	continue;
				    }
					// check the forignerKey referenced class should not be equal to the current class (no need to form an infinite loop) execute the same process 
					// for the same class more than one time 
					if(!forignerKey.referencedClass().equals(clazz)) {
					//call the function recursively for the referenced class 	
					getDependencyOfClass(forignerKey.referencedClass());
					}
				}
				
		
				
			}
			//after we make sure there is not more ForignerKey to be check and more dependencies are there we add the table into the orederedTable list
			orderedTables.add(clazz.getSimpleName());
		}
		
		//get the dependencies of the class (what the table need to be created) only JoinTable annotated
		public <T> void getDependencyOfJoinTableClass(Class<? extends T> clazz) {
			//check weather the table name is already in orderedTables in case yes just return (because that will form and endless loop)
			//and check if the Table is annotated with Table if yes then also return (because here is the ordering of JoinTable)
			if(orderedJoinTables.contains(clazz.getSimpleName()) || clazz.isAnnotationPresent(Table.class)  ) {
				return;
			}
			// loop through the all fields and check the the dependencies represented by ForignerKey 
			for(Field fd : clazz.getDeclaredFields()) {
				// check weather a forignerKey is available
				if(fd.isAnnotationPresent(ForignerKey.class)) {
					ForignerKey forignerKey= fd.getAnnotation(ForignerKey.class);
					//check equal dependencies here create and then alter the tables 
					boolean eqaulDependencies = checkEqualDepedencies(clazz,forignerKey.referencedClass(), joinTablesMap);
					// indicate no need to process the forignerKey specified with it 
					// because it is already removed from the schema of the inner class temporary
					if(eqaulDependencies) {
				    	continue;
				    }
					// check the forignerKey referenced class should not be equal to the current class (no need to form an infinite loop) execute the same process 
					// for the same class more than one time 
					if(!forignerKey.referencedClass().equals(clazz)) {
						//call the function recursively for the referenced class 
						getDependencyOfJoinTableClass(forignerKey.referencedClass());
					}
				}
				
		
				
			}
			//after we make sure there is not more ForignerKey to be check and more dependencies are there we add the table into the orderedJoinTables list
			orderedJoinTables.add(clazz.getSimpleName());
		}
}
