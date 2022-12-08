package com.example.trying;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.trying.annotation.AutoGenerate;
import com.example.trying.annotation.Hello;
import com.example.trying.annotation.Database.AutoIncrement;
import com.example.trying.annotation.Database.Column;
import com.example.trying.annotation.Database.DefaultTimeStamp;
import com.example.trying.annotation.Database.ForignerKey;
import com.example.trying.annotation.Database.ForignerKeyPart;
import com.example.trying.annotation.Database.Identity;
import com.example.trying.annotation.Database.IgnoreField;
import com.example.trying.annotation.Database.JoinTable;
import com.example.trying.annotation.Database.ManyToMany;
import com.example.trying.annotation.Database.ManyToOne;
import com.example.trying.annotation.Database.NotNull;
import com.example.trying.annotation.Database.OneToMany;
import com.example.trying.annotation.Database.PrimaryKey;
import com.example.trying.annotation.Database.PrimaryKeyPart;
import com.example.trying.annotation.Database.Table;
import com.example.trying.annotation.Database.Unique;
import com.example.trying.annotation.Database.Unsigned;
import com.example.trying.annotation.Database.Zerofill;
import com.example.trying.exception.ForignerKeyException;
import com.example.trying.exception.JoinTableException;
import com.example.trying.exception.PrimaryKeyException;
import com.example.trying.exception.UncompatibleAnnotationException;
import com.example.trying.utils.GFG;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import com.example.trying.*;
import com.example.trying.Constant.ReferenceOptions;

@SpringBootApplication
public class TryingApplication {
	static StringBuilder joinTablesStringBuilder= new StringBuilder();
	static Map<String, String> tablesMap= new HashMap<>();
	static List<String> orderedTables= new ArrayList<>();
	
	static Map<String, String> joinTablesMap= new HashMap<>();
	static List<String> orderedJoinTables= new ArrayList<>();
	
	static int countTables= 0;
	public static void main(String[] args) throws Throwable  {

		List<Class<?>> allClassess= getAllClasses("com.example.trying.entity");
		List<Class<?>> targetedClasses=  allClassess.stream().filter(cs -> cs.isAnnotationPresent(AutoGenerate.class)).collect(Collectors.toList());
//		for(Class<?> cs : targetedClasses) {
//			if(cs.isAnnotationPresent(Table.class)) {
//				countTables++;
//			}
//		}
		
		
		
		findDependencyOrder(targetedClasses);
		findDependencyOrderJoinTable(targetedClasses);
		System.out.println("*****************************************");
		System.out.println(orderedJoinTables);
		System.out.println(orderedJoinTables.indexOf("Rating"));
		
		StringBuilder schemaFile= new StringBuilder();
		FileSystem fileSystem = FileSystems.getDefault();
		Path path = fileSystem.getPath("").toAbsolutePath();
		System.out.println(targetedClasses.size());
		System.out.println(targetedClasses);
		String total="";
		
		for(Class<?> cs : targetedClasses ) {
			try {
				checkAnnotationFieldsRules(cs);
				checkAnnotationFieldsRulesJoinTable(cs);
				total +=printDeclaredFieldsInfo(cs,null,0);
				
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} catch (PrimaryKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} catch (ForignerKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UncompatibleAnnotationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JoinTableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
//		targetedClasses.map(cs -> {
//			
//			
//			
//			
//			
//			
//			
//	
////			try {
////				createMapper(cs);
////				createService(cs);
////				createServiceImpl(cs);
////				createController(cs);
////			} catch (IOException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
//		}  
//		
//				);
		
		
		
		schemaFile.append("DROP SCHEMA IF EXISTS `test`;\n"
				+ "CREATE SCHEMA IF NOT EXISTS `test`;\n"
				+ " USE `test`;\n");
		for(String myTable : orderedTables ) {
			schemaFile.append(tablesMap.get(myTable));
		}
		
		for(String myTable : orderedJoinTables ) {
			schemaFile.append(joinTablesMap.get(myTable));
		}
		
		
		schemaFile.append(joinTablesStringBuilder.toString());
		
		String file = getLocationOfResourceFile("schema", "sql");
		if(CheckFileExists(file)) {
			//delete that file 
			File fc = new File(file);
		    fc.delete();  
		}
		 FileWriter fstream = new FileWriter(file);
		   
		BufferedWriter out = new BufferedWriter(fstream);
		System.out.println(schemaFile.toString());
		try {
			out.write(schemaFile.toString());
		}
		catch (Exception e) {
		    System.err.println("Error: " + e.getMessage());
		} finally {
		    //Close the output stream
			out.close();
			SpringApplication.run(TryingApplication.class, args);
		}
		
//		System.out.println(tablesMap);
		
		

	}
	
	public static <T> void findDependencyOrder(List<Class<?>> targetedClasses) {
		for(Class<?> cs : targetedClasses) {
			if(cs.isAnnotationPresent(JoinTable.class)) {
				continue;
			}
			getDependencyOfClass(cs);
		}
	}
	
	public static <T> void findDependencyOrderJoinTable(List<Class<?>> targetedClasses) {
		for(Class<?> cs : targetedClasses) {
			if(cs.isAnnotationPresent(Table.class)) {
				continue;
			}
			getDependencyOfJoinTableClass(cs);
		}
	}
	public static <T> void getDependencyOfClass(Class<? extends T> clazz) {
		if(orderedTables.contains(clazz.getSimpleName())) {
			return;
		}
		for(Field fd : clazz.getDeclaredFields()) {
			if(fd.isAnnotationPresent(ForignerKey.class)) {
				ForignerKey forignerKey= fd.getAnnotation(ForignerKey.class);
				if(!forignerKey.referencedClass().equals(clazz)) {
				getDependencyOfClass(forignerKey.referencedClass());
				}
			}
			
	
			
		}
		orderedTables.add(clazz.getSimpleName());
	}
	
	public static <T> void getDependencyOfJoinTableClass(Class<? extends T> clazz) {
		if(orderedJoinTables.contains(clazz.getSimpleName()) || clazz.isAnnotationPresent(Table.class)  ) {
			return;
		}
		for(Field fd : clazz.getDeclaredFields()) {
			if(fd.isAnnotationPresent(ForignerKey.class)) {
				ForignerKey forignerKey= fd.getAnnotation(ForignerKey.class);
				if(!forignerKey.referencedClass().equals(clazz)) {
					getDependencyOfJoinTableClass(forignerKey.referencedClass());
				}
			}
			
	
			
		}
		orderedJoinTables.add(clazz.getSimpleName());
	}
	public static <T> void checkAnnotationFieldsRules(Class<? extends T> clazz) throws PrimaryKeyException, UncompatibleAnnotationException, JoinTableException, NoSuchFieldException, SecurityException {
		int count=0;
		if(clazz.isAnnotationPresent(Table.class)) {
			for(Field field : clazz.getDeclaredFields()) {
				if(field.isAnnotationPresent(PrimaryKey.class)) {
					count++;
				}
				if(field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class) ||
						field.isAnnotationPresent(ManyToMany.class)) {
					
					
					if(field.isAnnotationPresent(OneToMany.class)) {
						if(!(field.getType().isArray() || field.getType().equals(List.class) ) ) {
							throw new JoinTableException("filed \"" + field.getType() +"\" used with @OneToMany must be an array or list");
						}
						OneToMany oneToMany = field.getAnnotation(OneToMany.class);
						if(field.getType().isArray() ) {
							Class<?> referencedClass = field.getType();	
							Class<?> arrayType = referencedClass.getComponentType();
							if(!(arrayType.isAnnotationPresent(JoinTable.class) || arrayType.isAnnotationPresent(Table.class))) {
								throw new UncompatibleAnnotationException("Entity class " + clazz.getName() + " feild type " + arrayType.getName()+
										" must be annotated either with @Table or @JoinTable ");
							}
							if(!oneToMany.referencedColumn().equals("")) {
								Field feildReferd = referencedClass.getDeclaredField(oneToMany.referencedColumn());
								if((feildReferd.isAnnotationPresent(OneToMany.class) || feildReferd.isAnnotationPresent(ManyToOne.class) 
									    || feildReferd.isAnnotationPresent(ManyToMany.class))) {
									
									throw new JoinTableException("referencedColumn of the @OneToMany annotation of field \""+feildReferd.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +arrayType.getName() );
										
									}
							}
							if(!oneToMany.chosenColumn().equals("")) {
								Field feildChosen = referencedClass.getDeclaredField(oneToMany.chosenColumn());
								if((feildChosen.isAnnotationPresent(OneToMany.class) ||
								    feildChosen.isAnnotationPresent(ManyToOne.class)  || feildChosen.isAnnotationPresent(ManyToMany.class) )) {
									throw new JoinTableException("chosenColumn of the @OneToMany annotation of field \""+feildChosen.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +arrayType.getName() );
									
								}
							}
						}else if(field.getType().equals(List.class) ) {
							Class<?> referencedClass = (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];	
							if(!(referencedClass.isAnnotationPresent(JoinTable.class) || referencedClass.isAnnotationPresent(Table.class))) {
								throw new UncompatibleAnnotationException("Entity class " + clazz.getName() + " feild type " + referencedClass.getName()+
										" must be annotated either with @Table or @JoinTable ");
							}
							if(!oneToMany.referencedColumn().equals("")) {
								Field feildReferd = referencedClass.getDeclaredField(oneToMany.referencedColumn());
								if((feildReferd.isAnnotationPresent(OneToMany.class) || feildReferd.isAnnotationPresent(ManyToOne.class) 
									    || feildReferd.isAnnotationPresent(ManyToMany.class))) {
									throw new JoinTableException("referencedColumn of the @OneToMany annotation of field \""+feildReferd.getName()
											+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +referencedClass.getName() );
										
									}
							}
							if(!oneToMany.chosenColumn().equals("")) {
								Field feildChosen = referencedClass.getDeclaredField(oneToMany.chosenColumn());
								if((feildChosen.isAnnotationPresent(OneToMany.class) ||
								    feildChosen.isAnnotationPresent(ManyToOne.class)  || feildChosen.isAnnotationPresent(ManyToMany.class) )) {
									throw new JoinTableException("chosenColumn of the @OneToMany annotation of field \""+feildChosen.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +clazz.getName() );
									
								}
							}
							
							
						}
						
						if(field.getAnnotations().length > 1) {
							throw new UncompatibleAnnotationException("Entity class "+ clazz.getName()+" Feild " + field.getName()
							+ " cannot be annotated with other annotations because it already have been annotated with @OneToMany");
						}
						
					}
					
					if(field.isAnnotationPresent(ManyToOne.class)) {
						if((field.getType().isArray() || field.getType().equals(List.class) ) ) {
							throw new JoinTableException("filed \"" + field.getType() +"\" used with @ManyToOne cannot be an array or list");
						}
						Class<?> referencedClass = field.getType();	
						if(!(referencedClass.isAnnotationPresent(JoinTable.class) || referencedClass.isAnnotationPresent(Table.class)) ) {
							throw new UncompatibleAnnotationException("Entity class " + clazz.getName() + " feild type " + referencedClass.getName()+
									" must be annotated either with @Table or @JoinTable ");
						}
						
						ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
						if(!manyToOne.referencedColumn().equals("")) {
							Field feildReferd = referencedClass.getDeclaredField(manyToOne.referencedColumn());
							if((feildReferd.isAnnotationPresent(OneToMany.class) || feildReferd.isAnnotationPresent(ManyToOne.class) 
								    || feildReferd.isAnnotationPresent(ManyToMany.class))) {
									throw new JoinTableException("referencedColumn of the @ManyToOne annotation of field \""+feildReferd.getName()
											+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +referencedClass.getName() );
									
								}
						}
						if(!manyToOne.chosenColumn().equals("")) {
							Field feildChosen = referencedClass.getDeclaredField(manyToOne.chosenColumn());
							if((feildChosen.isAnnotationPresent(OneToMany.class) ||
							    feildChosen.isAnnotationPresent(ManyToOne.class)  || feildChosen.isAnnotationPresent(ManyToMany.class) )) {
								throw new JoinTableException("chosenColumn of the @ManyToOne annotation of field \""+feildChosen.getName()
								+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +clazz.getName() );
								
								
							}
						}
						
						if(field.getAnnotations().length > 1) {
							throw new UncompatibleAnnotationException("Entity class "+ clazz.getName()+" Feild " + field.getName()
							+ " cannot be annotated with other annotations because it already have been annotated with @ManyToOne");
						}
						
						
					}
					
					
					if(field.isAnnotationPresent(ManyToMany.class)) {
						if(!(field.getType().isArray() || field.getType().equals(List.class) ) ) {
							throw new JoinTableException("filed \"" + field.getType() +"\" used with @OneToMany must be an array or list");
						}
						ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
						if(!((!manyToMany.id().equals("") && manyToMany.mapBy().equals("")) || (manyToMany.id().equals("") && !manyToMany.mapBy().equals("")) ) ) {
							if(manyToMany.id().equals("") && manyToMany.mapBy().equals("") ) {
								throw new JoinTableException("@ManyToMany annotation must defined an Id or mapBy in Entity Class" + clazz.getName() );
							} 
							if(!manyToMany.id().equals("") && !manyToMany.mapBy().equals("")) {
								throw new JoinTableException("@ManyToMany annotation cannot defined an Id and mapBy both at the same time in Entity Class" + clazz.getName() );
							}
						}
						if(field.getType().isArray() ) {
							Class<?> referencedClass = field.getType();	
							Class<?> arrayType = referencedClass.getComponentType();
							if(!(arrayType.isAnnotationPresent(JoinTable.class) || arrayType.isAnnotationPresent(Table.class))) {
								throw new UncompatibleAnnotationException("Entity class " + clazz.getName() + " feild type " + arrayType.getName()+
										" must be annotated either with @Table or @JoinTable ");
							}
							if(!manyToMany.referencedColumn().equals("")) {
								Field feildReferd = referencedClass.getDeclaredField(manyToMany.referencedColumn());
								if((feildReferd.isAnnotationPresent(OneToMany.class) || feildReferd.isAnnotationPresent(ManyToOne.class) 
									    || feildReferd.isAnnotationPresent(ManyToMany.class))) {
									
									throw new JoinTableException("referencedColumn of the @ManyToMany annotation of field \""+feildReferd.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +arrayType.getName() );
										
									}
							}
							if(!manyToMany.chosenColumn().equals("")) {
								Field feildChosen = referencedClass.getDeclaredField(manyToMany.chosenColumn());
								if((feildChosen.isAnnotationPresent(OneToMany.class) ||
								    feildChosen.isAnnotationPresent(ManyToOne.class)  || feildChosen.isAnnotationPresent(ManyToMany.class) )) {
									throw new JoinTableException("chosenColumn of the @ManyToMany annotation of field \""+feildChosen.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +arrayType.getName() );
									
								}
							}
						}else if(field.getType().equals(List.class) ) {
							Class<?> referencedClass = (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];	
							if(!(referencedClass.isAnnotationPresent(JoinTable.class) || referencedClass.isAnnotationPresent(Table.class))) {
								throw new UncompatibleAnnotationException("Entity class " + clazz.getName() + " feild type " + referencedClass.getName()+
										" must be annotated either with @Table or @JoinTable ");
							}	
							if(!manyToMany.referencedColumn().equals("")) {
								Field feildReferd = referencedClass.getDeclaredField(manyToMany.referencedColumn());
								if((feildReferd.isAnnotationPresent(OneToMany.class) || feildReferd.isAnnotationPresent(ManyToOne.class) 
									    || feildReferd.isAnnotationPresent(ManyToMany.class))) {
									throw new JoinTableException("referencedColumn of the @ManyToMany annotation of field \""+feildReferd.getName()
											+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +referencedClass.getName() );
										
									}
							}
							if(!manyToMany.chosenColumn().equals("")) {
								Field feildChosen = referencedClass.getDeclaredField(manyToMany.chosenColumn());
								if((feildChosen.isAnnotationPresent(OneToMany.class) ||
								    feildChosen.isAnnotationPresent(ManyToOne.class)  || feildChosen.isAnnotationPresent(ManyToMany.class) )) {
									throw new JoinTableException("chosenColumn of the @ManyToMany annotation of field \""+feildChosen.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +clazz.getName() );
									
								}
							}
						}
						
						if(field.getAnnotations().length > 1) {
							throw new UncompatibleAnnotationException("Entity class "+ clazz.getName()+" Feild " + field.getName()
							+ " cannot be annotated with other annotations because it already have been annotated with @ManyToMany");
						}
					}
					
					
				}
				
			}
			if(count == 0) {
				throw new PrimaryKeyException("Entity class "+ clazz.getName()+" should a have a primary key");
			}else if(count > 1) {
				throw new PrimaryKeyException("Entity class "+ clazz.getName()+" cannot have more than one primary key");
				
			}
		}
		
	}
	
	public static <T> void checkAnnotationFieldsRulesJoinTable(Class<? extends T> clazz) throws PrimaryKeyException, UncompatibleAnnotationException, JoinTableException {
		int count=0;
		int fkCount=0;
		if( clazz.isAnnotationPresent(JoinTable.class)) {
			for(Field field : clazz.getDeclaredFields()) {
				if(field.isAnnotationPresent(PrimaryKey.class)) {
					count++;
				}
				if(field.isAnnotationPresent(ForignerKey.class)) {
				
					fkCount++;
				}
				if(field.isAnnotationPresent(OneToMany.class)) {
					if(field.getAnnotations().length > 1) {
						throw new UncompatibleAnnotationException("Entity class "+ clazz.getName()+" Feild " + field.getName()
						+ " cannot be annotated with other annotations because it already have been annotated with @OneToMany");
					}
				}
				
				
				
			}
			if(count == 0) {
				throw new PrimaryKeyException("Join Table "+ clazz.getName()+" should a have a primary key");
			}else if(count > 1) {
				throw new PrimaryKeyException("Join Table "+ clazz.getName()+" cannot have more than one primary key");
				
			}else if(fkCount < 2) {
				throw new JoinTableException("Join Table " + clazz.getName()+" must have at least two forigner keys refererncing the tables that"
						+ "the relations is based on");
			}
		}
		
	}
	
	public static <T> int activeFeildsNumber(Class<? extends T> clazz){
		int count=0;
		if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
			for(Field field : clazz.getDeclaredFields()) {
				if(field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class) ||
						field.isAnnotationPresent(ManyToMany.class) || field.isAnnotationPresent(IgnoreField.class)  ) {
					
				}else {
					count++;
				}
				
				
				
			}
			
		}
		return count;
		
	}
	public static <T> boolean checkJoinTableAlreadyExsit(Class<? extends T> clazz) throws JoinTableException{
		boolean alreadyExist = false;
		if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
			for(Field field : clazz.getDeclaredFields()) {
				if(field.isAnnotationPresent(ManyToOne.class)) {
					Class<?> referencedClass= field.getType();
					
					for(Field fd : referencedClass.getDeclaredFields() ) {
						if(fd.isAnnotationPresent(OneToMany.class)) {
							Class<?> innerClass= null;
							if(fd.getType().isArray()) {
								innerClass = fd.getType().getComponentType();
							}else {
								innerClass = (Class<?>) ((ParameterizedType)fd.getGenericType()).getActualTypeArguments()[0];	
							}
				            if(innerClass.equals(clazz)) {
				            	alreadyExist=true;
								break;	
				            }
//							throw new JoinTableException("Entity class " + clazz.getName() +""
//									+ " field \"" +field.getName() +"\" annotated with @ManyToOne create same join table with Entity class " + 
//									referencedClass.getName() + " field \""+ field.getName() + ""
//											+ "\" annotated with @OneToMany" );
							
						}
					}
					
				}
				
				
				
			}
			
		}
		return alreadyExist;
		
	}
	public static <T> String getPrimaryFieldName(Class<? extends T> clazz) {
		String primaryFieldName= "";
		if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
			for(Field field : clazz.getDeclaredFields()) {
				if(field.isAnnotationPresent(PrimaryKey.class)) {
					primaryFieldName= field.getName();
					break;
				}
				
				
				
			}
			
		}
		
		return primaryFieldName;
	}
	public static <T> String constractPrimaryKey(Class<? extends T> clazz) {
		String primaryFieldName= "";
		String total="";
		if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
			for(Field field : clazz.getDeclaredFields()) {
				if(field.isAnnotationPresent(PrimaryKey.class)) {
					if(field.isAnnotationPresent(Column.class)) {
						Column cloumnAno= field.getAnnotation(Column.class);
						if(cloumnAno != null && !cloumnAno.name().equals("")) {
							primaryFieldName = cloumnAno.name();
						}else{
							primaryFieldName = GFG.camelToSnake(field.getName());
						}
						
					}else {
						primaryFieldName = GFG.camelToSnake(field.getName());
					}
				
					break;
				}
				
				
				
			}
			
			for(Field field : clazz.getDeclaredFields()) {
				if(field.isAnnotationPresent(PrimaryKeyPart.class)) {
					if(field.isAnnotationPresent(Column.class)) {
						Column cloumnAno= field.getAnnotation(Column.class);
						if(cloumnAno != null && !cloumnAno.name().equals("")) {
							total += cloumnAno.name() + ",";
						}else{
							total = GFG.camelToSnake(field.getName()) + ",";
						}
						
					}else {
						total = GFG.camelToSnake(field.getName()) + ",";
					}
				
					
				}
				
				
				
			}
			
			
			total = primaryFieldName +","+ total;
			char[] charArr = total.toCharArray();
			char[] newArr = new char[charArr.length-1];
			for(int i=0; i< charArr.length -1 ;i++) {
				newArr[i]=charArr[i] ;
			}
			
			return "PRIMARY KEY(" +new String(newArr) + ")\n";
			
		}else {
			return "";
		}
		
		
	}
	public static <T> int getTotalForignerKey(Class<? extends T> clazz){
		int count=0;
		if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
			for(Field field : clazz.getDeclaredFields()) {
				if(field.isAnnotationPresent(ForignerKey.class)) {
					count++;
				}
			}
			
		}
		return count;
		
	}
	
	public static <T> void checkForignerKeyRules(Class<? extends T> clazz) throws NoSuchFieldException, SecurityException, ForignerKeyException{
		if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
			for(Field field : clazz.getDeclaredFields()) {
				if(field.isAnnotationPresent(ForignerKey.class) || field.isAnnotationPresent(ForignerKeyPart.class)) {
					if(field.isAnnotationPresent(ForignerKey.class)) {
						ForignerKey forignerKey= field.getAnnotation(ForignerKey.class);
						Class<?> referencedClass = forignerKey.referencedClass();
						if(!forignerKey.referencedColumn().equals("")) {
						Field fd = referencedClass.getDeclaredField(forignerKey.referencedColumn());
						if(!(fd.isAnnotationPresent(PrimaryKey.class) || fd.isAnnotationPresent(PrimaryKeyPart.class) )) {
							throw new ForignerKeyException("@ForignerKey referencedColumn annotated feild \""+field.getName()+"\" in the Entity class " + clazz.getName() + ""
									+ " cannot be unindex");
						}
						}
					}else {
						ForignerKeyPart forignerKeyPart= field.getAnnotation(ForignerKeyPart.class);
						Class<?> referencedClass = forignerKeyPart.referencedClass();
						if(!forignerKeyPart.referencedColumn().equals("")) {
						Field fd = referencedClass.getDeclaredField(forignerKeyPart.referencedColumn());
						if(!(fd.isAnnotationPresent(PrimaryKey.class) || fd.isAnnotationPresent(PrimaryKeyPart.class) )) {
							throw new ForignerKeyException("@ForignerKeyPart referencedColumn annotated feild \""+field.getName()+"\" in the Entity class " + clazz.getName() + ""
									+ " cannot be unindex");
						}
						}
					}
					
				}
			}
			
		}
		
		
	}
	public static <T> Field getJustThatSpecificField(Class<? extends T> clazz, String fieldName) throws ForignerKeyException {
		if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
			
			//field not found exception 
			try {
				Field field = clazz.getDeclaredField(fieldName);
				return field;	
						
			} catch (NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				throw new ForignerKeyException("Referenced Class " + clazz.getName() + "does not contain a field with name "+ fieldName);
			}
		}else {
			// throw the referenceClass need to be annotated with Table
			throw new ForignerKeyException("Referenced Class " + clazz.getName() + "must be annotated with @Table or @JoinTable");
		}
	}
	public static <T> Field getThatSpecificField(Class<? extends T> clazz, String fieldName, Field referencingField) throws ForignerKeyException {
		
		if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
			//field not found exception 
			try {
				Field field = clazz.getDeclaredField(fieldName);
				
				if( ((field.getType().equals(int.class) || field.getType().equals(Integer.class)) &&
						(referencingField.getType().equals(int.class) || referencingField.getType().equals(Integer.class))) ||
						
					((field.getType().equals(short.class) || field.getType().equals(Short.class)) &&
							(referencingField.getType().equals(short.class) || referencingField.getType().equals(Short.class))) ||
					((field.getType().equals(long.class) || field.getType().equals(Long.class)) &&
							(referencingField.getType().equals(long.class) || referencingField.getType().equals(Long.class))) ||
					((field.getType().equals(float.class) || field.getType().equals(Float.class)) &&
							(referencingField.getType().equals(float.class) || referencingField.getType().equals(Float.class))) || 
					((field.getType().equals(double.class) || field.getType().equals(Double.class)) &&
							(referencingField.getType().equals(double.class) || referencingField.getType().equals(Double.class))) ||
					((field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) &&
							(referencingField.getType().equals(boolean.class) || referencingField.getType().equals(Boolean.class))) ||
					
					field.getType().equals(referencingField.getType())	) {
					return field;
					
					
				}else{
					throw new ForignerKeyException("Referenced Class " + clazz.getName() + " field type is not compatiable");
					
				}
				
			} catch (NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				throw new ForignerKeyException("Referenced Class " + clazz.getName() + "does not contain a field with name "+ fieldName);
			}
		}else {
			// throw the referenceClass need to be annotated with Table
			throw new ForignerKeyException("Referenced Class " + clazz.getName() + "must be annotated with @Table");
		}
	}
	public static <T> void printJoinTableInfo(Field field) {
		StringBuilder strBuilder= new StringBuilder();
		System.out.print("hi");
	}
	public static <T> List<String> getForignerFieldInfo(Class<? extends T> clazz,Field referencedfield) {
		List<String> res= new ArrayList<>();
		String referencedTable;
		// get the referenced table name as pre-specified
		if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
			Table tableAnnotaion= clazz.getAnnotation(Table.class);
			if(tableAnnotaion != null && !tableAnnotaion.name().equals("")) {
				referencedTable = tableAnnotaion.name();
			}else {
				referencedTable = GFG.camelToSnake(clazz.getSimpleName()) ;
				
			}
			
			
		}else {
			referencedTable = GFG.camelToSnake(clazz.getSimpleName());
			
		}
		
		
		//get the feild info
		int size = 0;
		int afterDec = 2;
		String columnName = "";
		String blobSize;
		String attributes;
		if(referencedfield.isAnnotationPresent(Column.class)) {
			Column cloumnAno= referencedfield.getAnnotation(Column.class);
			if(cloumnAno != null && !cloumnAno.name().equals("")) {
				columnName= cloumnAno.name();
			}else{
				columnName = GFG.camelToSnake(referencedfield.getName());
			}
			if(cloumnAno != null && cloumnAno.size() != 0 ) {
				size= cloumnAno.size();
			}
			if(cloumnAno != null && cloumnAno.afterDecimal() != 2 ) {
				afterDec = cloumnAno.afterDecimal();
			}
		}else{
			columnName = GFG.camelToSnake(referencedfield.getName());
		}
		
		attributes = getFieldAttributes(referencedfield);
		String fieldInfo= getFieldDatatypeSizeAttr(referencedfield, size, afterDec,attributes  );
//		System.out.println("*****************************");
//		System.out.println(fieldInfo);
//		System.out.println(referencedTable +"(" + columnName + ")");
		res.add(fieldInfo);
		res.add(columnName);
		res.add(referencedTable);
		return res;
		
	}
	public static <T> String printDeclaredFieldsInfo(Class<? extends T> clazz, Class<? extends T> parentClass, int uni) throws IllegalArgumentException, IllegalAccessException, ForignerKeyException, UncompatibleAnnotationException, JoinTableException, NoSuchFieldException, SecurityException {
		if(clazz.isAnnotationPresent(JoinTable.class) && parentClass == null && uni== 0) {
			return "";
		}
		if(!(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class))) {
			return "";
		}
		StringBuilder strBuilder= new StringBuilder();
		StringBuilder forignerKeyStringBuilder= new StringBuilder();
		
		String className = "";
		int totalForignerKeys= getTotalForignerKey(clazz);
		strBuilder.append("Create Table ");
		if(clazz.isAnnotationPresent(Table.class)) {
			Table tableAnnotaion= clazz.getAnnotation(Table.class);
			if(tableAnnotaion != null && !tableAnnotaion.name().equals("")) {
				strBuilder.append(tableAnnotaion.name()+ "\n(\n");
				className=tableAnnotaion.name();
			}else {
				strBuilder.append(GFG.camelToSnake(clazz.getSimpleName()) + "\n(\n" );
				className=GFG.camelToSnake(clazz.getSimpleName());
			}
			
			
		}else if(clazz.isAnnotationPresent(JoinTable.class)) {
			//check the rules we made for the join table 
			JoinTable tableAnnotaion= clazz.getAnnotation(JoinTable.class);
			if(tableAnnotaion != null && !tableAnnotaion.name().equals("")) {
				strBuilder.append(tableAnnotaion.name()+ "\n(\n");
				className=tableAnnotaion.name();
			}else {
				strBuilder.append(GFG.camelToSnake(clazz.getSimpleName()) + "\n(\n" );
				className=GFG.camelToSnake(clazz.getSimpleName());
			}
		}
		int count = 0;
		boolean thereIsForignerKey= false;
		Field[] fiels= clazz.getDeclaredFields();
		int fieldSize= activeFeildsNumber(clazz);
		for(Field field : clazz.getDeclaredFields()) {
			int size = 0;
			int afterDec = 2;
			String columnName = "";
			if(  field.isAnnotationPresent(IgnoreField.class)  ) {
				
				continue;
				
			}
			if(field.isAnnotationPresent(Column.class) ) {
				Column cloumnAno= field.getAnnotation(Column.class);
				if(cloumnAno != null && !cloumnAno.name().equals("")) {
					strBuilder.append(cloumnAno.name() + " ");
					columnName=cloumnAno.name();
				}else{
					strBuilder.append(GFG.camelToSnake(field.getName()) + " ");
					columnName=GFG.camelToSnake(field.getName());
				}
				if(cloumnAno != null && cloumnAno.size() != 0 ) {
					size= cloumnAno.size();
				}
				if(cloumnAno != null && cloumnAno.afterDecimal() != 2 ) {
					afterDec = cloumnAno.afterDecimal();
				}
			}else{
				if(!(field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class) ||
						field.isAnnotationPresent(ManyToMany.class))) {
				strBuilder.append(GFG.camelToSnake(field.getName()) + " ");
				columnName=GFG.camelToSnake(field.getName());
				}
			}
			
			
//			System.out.println(String.format("Field name: %s, type: %s",
//					field.getName(),
//					field.getType().getName()
//				
//					));
//			
//			System.out.println(String.format("is Syntheic Field : %s ", field.isSynthetic()));
			
			
			
		
			    List<String> res = new ArrayList<>();
				String primary= "";
				String autoIncrement= "";
				String notNull= "";
				String defaultTimeStamp= "";
				String unique= "";
				
				String unsigned= "";
				String zerofill= "";
				String identity= "";
				
				if(field.isAnnotationPresent(NotNull.class)) {
					notNull= " NOT NULL";
				}
				if(field.isAnnotationPresent(AutoIncrement.class)) {
					
					autoIncrement= " AUTO_INCREMENT";
				}
				if(field.isAnnotationPresent(DefaultTimeStamp.class)) {
					defaultTimeStamp= " DEFAULT CURRENT_TIMESTAMP";
				}
				if(field.isAnnotationPresent(Unique.class)) {
					unique= " UNIQUE";
				}
				if(field.isAnnotationPresent(Unsigned.class)) {
					unsigned=" Unsigned";
				}
				if(field.isAnnotationPresent(Zerofill.class)) {
					zerofill=" ZEROFILL";
				}
				if(field.isAnnotationPresent(Identity.class)) {
					Identity identityAnno = field.getAnnotation(Identity.class);
					identity=" IDENTITY("+identityAnno.startingValue()+","+identityAnno.incrementValue()+")";
				}
				
				if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(ManyToMany.class) ) {
					String joinTable = createTheJoinTable(clazz,field );
					
					joinTablesStringBuilder.append(joinTable );
					
					continue;
				}
				if(field.isAnnotationPresent(ForignerKey.class)) {
					checkForignerKeyRules(clazz);
					ForignerKey forigner= field.getAnnotation(ForignerKey.class);
					Class<?> referencedClass= forigner.referencedClass();
					String endPartFk="";
					String fieldName= forigner.referencedColumn();
					if(fieldName.equals("")) {
						//find the primary field name 
						fieldName= getPrimaryFieldName(referencedClass);
					}
					
					Field targetClassField = getThatSpecificField(referencedClass, fieldName,field);
					res= getForignerFieldInfo(referencedClass,targetClassField);
					thereIsForignerKey =true;
					
					if(totalForignerKeys > 0) {
						endPartFk = ",";
						totalForignerKeys--;
					}
					
					String contraint = constractTheForignerKey(clazz,forigner,endPartFk,res,className, columnName);
					forignerKeyStringBuilder.append(contraint);
//					CONSTRAINT fk_student_city_id
//					      FOREIGN KEY (city_id) REFERENCES city(id)
					
					
				}
				if(field.isAnnotationPresent(ForignerKeyPart.class)) {
					res= getForignerKeyPartInfo(field);
					
//					CONSTRAINT fk_student_city_id
//					      FOREIGN KEY (city_id) REFERENCES city(id)
					
					
				}
				
				
				String endPart= endPartConstraction(count, fieldSize, primary, notNull, unique , autoIncrement,identity,thereIsForignerKey);
				   String uniquex= "";
					if(field.isAnnotationPresent(ForignerKey.class) || field.isAnnotationPresent(ForignerKeyPart.class) ){
						if(parentClass != null && field.isAnnotationPresent(ForignerKey.class) ) {
							ForignerKey fk= field.getAnnotation(ForignerKey.class);
							if(uni == 0) {
								if(!fk.referencedClass().equals(parentClass)) {
									uniquex= " UNIQUE";
									
								}	
							}else if(uni == 1) {
								if(fk.referencedClass().equals(parentClass)) {
									uniquex= " UNIQUE";
									
								}
							}
							
							

						}
						String endPartx= endPartConstraction(count, fieldSize, "", "",uniquex , "", "",thereIsForignerKey);
						strBuilder.append(res.get(0) + endPartx);
					}else if(field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
						//INTEGER
						strBuilder.append("INTEGER" + ( size == 0 ? "(10)" : "("+size+")")+ unsigned + zerofill + endPart);
					}else if(field.getType().equals(long.class) || field.getType().equals(Long.class)) {
						//BIGINT
						strBuilder.append("BIGINT" + ( size == 0 ? "(10)" : "("+size+")") + unsigned + zerofill + endPart );
					}else if(field.getType().equals(short.class) || field.getType().equals(Short.class)) {
						//SMALLINT
						strBuilder.append("SMALLINT" + ( size == 0 ? "(10)" : "("+size+")") + unsigned + zerofill + endPart );
					}else if(field.getType().equals(float.class) || field.getType().equals(Float.class)) {
						//FLOAT
						strBuilder.append("FLOAT" +sizeConstraction(size,afterDec)  + endPart );
					}else if(field.getType().equals(double.class) || field.getType().equals(Double.class)) {
						//DOUBLE
						strBuilder.append("DOUBLE" + sizeConstraction(size,afterDec) + endPart  );
					}else if(field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
						//BOOLEAN
						strBuilder.append("BOOLEAN" + endPart);
					}else if(field.getType().equals(String.class)) {
						if(size > 65535) {
							//LONGTEXT
							strBuilder.append("LONGTEXT" + endPart) ;
						}else {
							//VARCHAR
							strBuilder.append("VARCHAR" + ( size == 0 ? "(255)" : "("+size+")") + endPart);
						}
						
					}else if(field.getType().equals(Blob.class)) {
						//BLOB
						if(size > 65535) {
							//LONGBLOB
							strBuilder.append("LongBLOB" + endPart);
						}else {
							//VARBINARY
							strBuilder.append("BLOB" + endPart);
						}
						
						
					}else if(field.getType().equals(byte[].class)) {
						if(size > 65535) {
							//LONGBLOB
							strBuilder.append("BLOB" + endPart);
						}else {
							//VARBINARY
							strBuilder.append("VARBINARY" + ( size == 0 ? "(255)" : "("+size+")") + endPart);
						}
						
					}else if(field.getType().equals(Time.class)) {
					//TIME
					strBuilder.append("TIME" + endPart);
					}else if(field.getType().equals(Date.class)) {
						//DATE
						strBuilder.append("DATE" + endPart);
					}else if(field.getType().equals(Timestamp.class)) {
						//TIMESTAMP
						strBuilder.append("TIMESTAMP" + defaultTimeStamp + endPart);
					}

			
			
//				System.out.println(String.format("Annotation Name: %s, type: %s",
//						fieldAnnotation.getClass().getSimpleName(),
//						fieldAnnotation.getClass().toString()
//						));
			count++;
		}
		strBuilder.append(forignerKeyStringBuilder.toString());
		strBuilder.append(constractPrimaryKey(clazz));
		strBuilder.append(");\n");
//		System.out.println("the result *******************************");
//		System.out.println(joinTablesStringBuilder.toString());
//		System.out.println("end result *******************************");
//		strBuilder.append(joinTablesStringBuilder.toString());
//	    System.out.println(strBuilder.toString());
		if(clazz.isAnnotationPresent(Table.class)) {
			tablesMap.put(clazz.getSimpleName(), strBuilder.toString());	
		}
		
	    return strBuilder.toString();
	}
	
	public static <T> String createTheJoinTable(Class<? extends T> clazz, Field field) throws ForignerKeyException, IllegalArgumentException, IllegalAccessException, UncompatibleAnnotationException, JoinTableException, NoSuchFieldException, SecurityException {
		if(checkJoinTableAlreadyExsit(clazz)) {
			return "";
		}
		StringBuilder strBuilder = new StringBuilder();
		Class<?> referencedClass= null;
		List<String> res = new ArrayList<>();
		List<String> res1 = new ArrayList<>();
		String joinTableName="";
		// 0 OneToMany, 1 ManyToOne , 2 ManyToMany
		int unique= 0;
		String referencedFieldName= "";
		String chosenFieldName= "";
		String fkName= "";
		ReferenceOptions onUpdate= ReferenceOptions.NO_ACT;
		ReferenceOptions onDelete= ReferenceOptions.NO_ACT;
		if(field.isAnnotationPresent(OneToMany.class)) {
			OneToMany oneToMany= field.getAnnotation(OneToMany.class);
			joinTableName= oneToMany.joinTableName();	
			referencedFieldName = oneToMany.referencedColumn();
			chosenFieldName = oneToMany.chosenColumn();
			fkName= oneToMany.forignerName();
			onUpdate= oneToMany.onUpdate();
			onDelete= oneToMany.onDelete();
			referencedClass = field.getType().getComponentType();
			if(referencedClass == null) {
				referencedClass = (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
			}
		}else if(field.isAnnotationPresent(ManyToOne.class)) {
			ManyToOne manyToOne= field.getAnnotation(ManyToOne.class);
			joinTableName= manyToOne.joinTableName();
			referencedFieldName = manyToOne.referencedColumn();
			chosenFieldName = manyToOne.chosenColumn();
			fkName= manyToOne.forignerName();
			onUpdate= manyToOne.onUpdate();
			onDelete= manyToOne.onDelete();
			referencedClass = field.getType();
			unique=1;
		}else if(field.isAnnotationPresent(ManyToMany.class)) {
			ManyToMany manyToMany= field.getAnnotation(ManyToMany.class);
			if(!manyToMany.mapBy().equals("")) {
				return "";
			}
			joinTableName= manyToMany.joinTableName();	
			referencedFieldName = manyToMany.referencedColumn();
			chosenFieldName = manyToMany.chosenColumn();
			fkName= manyToMany.forignerName();
			onUpdate= manyToMany.onUpdate();
			onDelete= manyToMany.onDelete();
			referencedClass = field.getType().getComponentType();
			if(referencedClass == null) {
				referencedClass = (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
			}
			unique=2;
		}
		
		
		if(referencedClass.isAnnotationPresent(Table.class)) {
			strBuilder.append("Create Table ");
			//get the joinTableName
			
			if(joinTableName.equals("")) {
				joinTableName= GFG.camelToSnake(clazz.getSimpleName() + referencedClass.getSimpleName());
			}
			strBuilder.append(joinTableName +"\n(\n");
			// get the target cloumn name
			
			
			if(referencedFieldName.equals("")) {
				referencedFieldName = getPrimaryFieldName(referencedClass);
			}
			if(chosenFieldName.equals("")) {
				chosenFieldName = getPrimaryFieldName(clazz);
			}
			
			Field targetClassField = getJustThatSpecificField(referencedClass, referencedFieldName);
			res= getForignerFieldInfo(referencedClass,targetClassField);
			
			targetClassField = getJustThatSpecificField(clazz, chosenFieldName);
			res1= getForignerFieldInfo(clazz,targetClassField);
			if(unique == 0) {
				strBuilder.append(res1.get(2)+"_"+chosenFieldName + " " + res1.get(0) + ",\n");
				strBuilder.append(res.get(2)+"_"+referencedFieldName + " " + res.get(0)+" UNIQUE" + ",\n");				
			}else if(unique == 1) {
				strBuilder.append(res1.get(2)+"_"+chosenFieldName + " " + res1.get(0)+" UNIQUE"+ ",\n");
				strBuilder.append(res.get(2)+"_"+referencedFieldName + " " + res.get(0)+ ",\n");
			}else if(unique == 2) {
				strBuilder.append(res1.get(2)+"_"+chosenFieldName + " " + res1.get(0) + ",\n");
				strBuilder.append(res.get(2)+"_"+referencedFieldName + " " + res.get(0)+",\n");
			}

			
			StringBuilder forignerKeyStringBuilder = new StringBuilder();
			
			
				
//			List<String> columnName= constractColumnName(clazz,columnNameFK,className, referencedClass);
//			String referencedClassColumnName= constractReferencedClassColumnName(clazz,res1.get(1), referencedClass);
//			System.out.println("hello my friend");
//			System.out.println(columnName);
//			System.out.println(referencedClassColumnName);
			
			if(res1.get(2).equals(res.get(2))) {
				if(fkName.equals("")) {
					fkName += "fk_1_"+joinTableName;
				}
				forignerKeyStringBuilder.append("CONSTRAINT " + fkName+ "\n");
				forignerKeyStringBuilder.append("\tFOREIGN KEY ("+res1.get(2)+"_"+chosenFieldName+"," 
						+res.get(2)+"_"+referencedFieldName+ ") REFERENCES "+ 
						res1.get(2)+"("+res1.get(1)+","+res.get(1)+")"+
								" ON UPDATE " + onUpdate.getType() + " ON DELETE " + onDelete.getType()+ ",\n" );
				
			}else {
				String fkName1= "";
				if(fkName.equals("")) {
					fkName += "fk_1_"+joinTableName;
					fkName1 +="fk_2_"+joinTableName;
				}
				forignerKeyStringBuilder.append("CONSTRAINT " + fkName+ "\n");
				forignerKeyStringBuilder.append("\tFOREIGN KEY ("+res1.get(2)+"_"+chosenFieldName+") REFERENCES "+ 
						res1.get(2)+"("+res1.get(1)+")"+
								" ON UPDATE " + onUpdate.getType() + " ON DELETE " + onDelete.getType()+ ",\n" );
				
				forignerKeyStringBuilder.append("CONSTRAINT " + fkName1+ "\n");
				forignerKeyStringBuilder.append("\tFOREIGN KEY ("+res.get(2)+"_"+referencedFieldName+") REFERENCES "+ 
						res.get(2)+"("+res.get(1)+")"+
								" ON UPDATE " + onUpdate.getType() + " ON DELETE " + onDelete.getType()+ ",\n" );
			}
			
			
			
			
			strBuilder.append(forignerKeyStringBuilder.toString());
			
			strBuilder.append("PRIMARY KEY ("+res1.get(2)+"_"+chosenFieldName+"," 
					+res.get(2)+"_"+referencedFieldName+ ")\n");
			
			strBuilder.append(");\n");
			
			
		}else if(referencedClass.isAnnotationPresent(JoinTable.class)) {
			//we are working on
			if(unique == 2) {
				strBuilder.append(printDeclaredFieldsInfo(referencedClass,null,unique)); 
			}else {
				strBuilder.append(printDeclaredFieldsInfo(referencedClass,clazz,unique)); 	
//				System.out.println("the result *******************************");
//				System.out.println(strBuilder.toString());
//				System.out.println("end result *******************************");
			}
			
		}
		
		if(referencedClass.isAnnotationPresent(JoinTable.class)) {
			joinTablesMap.put(referencedClass.getSimpleName(), strBuilder.toString());
			return "";
		}
		return strBuilder.toString();
	}
	
	
	
	
	public static <T> String constractReferencedClassColumnName(Class<? extends T> clazz, String columnNameFK, Class<?> referencedClass) throws ForignerKeyException {
		String cloumName= columnNameFK + ','; 
		String tableName="";
		List<String> res= new ArrayList<>();
		if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
			for(Field field : clazz.getDeclaredFields()) {
				tableName = getForignerFieldInfo(referencedClass,field).get(2);
				if(field.isAnnotationPresent(ForignerKeyPart.class) ) {
					ForignerKeyPart forignerPart=field.getAnnotation(ForignerKeyPart.class);
					if(!forignerPart.referencedClass().equals(referencedClass)) {
						continue;
					}
					res=getForignerKeyPartInfo(field);
					cloumName += res.get(1) + ',';
					tableName = res.get(2);
					
					
				  }
				}
			}
		char[] charArr = cloumName.toCharArray();
		char[] newArr = new char[charArr.length-1];
		for(int i=0; i< charArr.length -1 ;i++) {
			newArr[i]=charArr[i] ;
		}
		return tableName + "(" +new String(newArr) + ")";
	}
	
	public static <T> List<String> constractColumnName(Class<? extends T> clazz, String columnNameFK,String className, Class<?> referencedClass) throws ForignerKeyException {
		String cloumName= columnNameFK + ','; 
	    String tableName= className;
	    List<String> res= new ArrayList<>();
		if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
			for(Field field : clazz.getDeclaredFields()) {
				 
				if(field.isAnnotationPresent(ForignerKeyPart.class) ) {
					ForignerKeyPart forignerPart=field.getAnnotation(ForignerKeyPart.class);
					if(!forignerPart.referencedClass().equals(referencedClass)) {
						continue;
					}
					if(field.isAnnotationPresent(Column.class)) {
						Column cloumnAno= field.getAnnotation(Column.class);
						if(cloumnAno != null && !cloumnAno.name().equals("")) {
							cloumName += cloumnAno.name() + ",";
						}else{
							cloumName += GFG.camelToSnake(field.getName())+ ",";
						}
						
					}else{
						cloumName += GFG.camelToSnake(field.getName()) + ",";
					}
					
					
					
				}
				
				
				
			}
			
			
		}
		char[] charArr = cloumName.toCharArray();
		char[] newArr = new char[charArr.length-1];
		for(int i=0; i< charArr.length -1 ;i++) {
			newArr[i]=charArr[i] ;
		}
		res.add(new String(newArr));
		res.add(tableName + "(" + new String(newArr) + ")");
		return res;
		
	}
	
	
	public static <T> String constractTheForignerKey(Class<? extends T> clazz,ForignerKey forigner, String endPartFk, List<String> primaryKeyRes, String className, String columnNameFK ) throws ForignerKeyException {
		StringBuilder forignerKeyStringBuilder = new StringBuilder();
		
		String fkName = forigner.name();
		ReferenceOptions onUpdate = forigner.onUpdate();
		ReferenceOptions onDelete = forigner.onDelete();
		Class<?> referencedClass= forigner.referencedClass();
		
		
		
		
		List<String> columnName= constractColumnName(clazz,columnNameFK,className, referencedClass);
		String referencedClassColumnName= constractReferencedClassColumnName(clazz,primaryKeyRes.get(1), referencedClass);
		if(fkName.equals("")) {
			fkName += "fk_"+className+"_"+primaryKeyRes.get(2)+"_"+columnName.get(0).replaceAll(",", "_");
		}
		forignerKeyStringBuilder.append("CONSTRAINT " + fkName+ "\n");
		forignerKeyStringBuilder.append("\tFOREIGN KEY "+columnName.get(1)+" REFERENCES "+ referencedClassColumnName+
				" ON UPDATE " + onUpdate.getType() + " ON DELETE " + onDelete.getType()+ endPartFk+ "\n" );
		return forignerKeyStringBuilder.toString();
	}
	
	public static List<String> getForignerKeyPartInfo(Field field) throws ForignerKeyException {
		List<String> res= new ArrayList<>();
		ForignerKeyPart forignerPart = field.getAnnotation(ForignerKeyPart.class);
		Class<?> referencedClass= forignerPart.referencedClass();
		String fieldName= forignerPart.referencedColumn();
		if(fieldName.equals("")) {
			//find the primary field name 
			fieldName= getPrimaryFieldName(referencedClass);
		}
		
		Field targetClassField = getThatSpecificField(referencedClass, fieldName,field);
		res= getForignerFieldInfo(referencedClass,targetClassField);
		return res;
	}
	
	public static String getFieldDatatypeSizeAttr(Field field, int size, int afterDec, String  attributes) {
		String datatype = "";
		if(field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
			//INTEGER
			datatype = "INTEGER" + ( size == 0 ? "(10)" : "("+size+")")+ attributes;
		}else if(field.getType().equals(long.class) || field.getType().equals(Long.class)) {
			//BIGINT
			datatype = "BIGINT" + ( size == 0 ? "(10)" : "("+size+")") + attributes;
		}else if(field.getType().equals(short.class) || field.getType().equals(Short.class)) {
			//SMALLINT
			datatype = "SMALLINT" + ( size == 0 ? "(10)" : "("+size+")") + attributes;
		}else if(field.getType().equals(float.class) || field.getType().equals(Float.class)) {
			//FLOAT
			datatype = "FLOAT" +sizeConstraction(size,afterDec);
		}else if(field.getType().equals(double.class) || field.getType().equals(Double.class)) {
			//DOUBLE
			datatype = "DOUBLE" + sizeConstraction(size,afterDec);
		}else if(field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
			//BOOLEAN
			datatype = "BOOLEAN";
		}else if(field.getType().equals(String.class)) {
			if(size > 65535) {
				//LONGTEXT
				datatype = "LONGTEXT";
			}else {
				//VARCHAR
				datatype = "VARCHAR" + ( size == 0 ? "(255)" : "("+size+")");
			}
			
		}else if(field.getType().equals(Blob.class)) {
			//BLOB
			
			if(size > 65535) {
				//LONGBLOB
				datatype = "LONGBLOB"  + ( size == 0 ? "(255)" : "("+size+")") ;
			}else {
				//LONGBLOB
				datatype = "BLOB" + ( size == 0 ? "(255)" : "("+size+")");
			}
		
			
		}else if(field.getType().equals(byte[].class)) {
			if(size > 65535) {
				//LONGBLOB
				datatype = "BLOB";
			}else {
				//VARBINARY
				datatype = "VARBINARY" + ( size == 0 ? "(255)" : "("+size+")");
			}
			
		}else if(field.getType().equals(Time.class)) {
		//TIME
		datatype = "TIME";
		}else if(field.getType().equals(Date.class)) {
			//DATE
			datatype = "DATE";
		}else if(field.getType().equals(Timestamp.class)) {
			//TIMESTAMP
			datatype = "TIMESTAMP";
		}
		
		return datatype;
	}
	
	public static String getFieldAttributes(Field field) {
		
		String unsigned= "";
		String zerofill= "";
		
		if(field.isAnnotationPresent(Zerofill.class)) {
			zerofill=" ZEROFILL";
		}
		if(field.isAnnotationPresent(Unsigned.class)) {
			unsigned=" Unsigned";
		}
		
		return unsigned + zerofill;
	}
	public static String sizeConstraction(int size, int afterDec) {
		String sizeFinal= "";
		if(size ==0) {
			sizeFinal += "(10,"+afterDec+")";
			
		}else {
			sizeFinal += "("+ size+ ","+ afterDec + ")";
		}
		
		return sizeFinal;
	}
	
	public static String endPartConstraction(int count, int fieldSize, String primary,String notNull,String unique, String autoIncrement, String identity, boolean check  ) {
		String finalEnd= notNull + unique + autoIncrement + identity + primary;
		if(count  < fieldSize) {
			finalEnd += ",\n";
		}
		
		return finalEnd;
		
	}
	
	public static String getLocationOfFile(String whichPackage, String fileName) {
		BufferedWriter out = null;
		 
 	    String path2 = TryingApplication.class.getPackageName().replace(".", "\\");
	    	System.out.println(path2);
	
	    //Specify directory
		FileSystem fileSystem = FileSystems.getDefault();
		Path path = fileSystem.getPath("").toAbsolutePath();
	    String directory = path.toString() + "\\src\\main\\java\\"+ path2 + "\\" + whichPackage + "\\";
	    //check wether there is a directory if not create a new one
	    File theDir = new File(directory);
	    if (!theDir.exists()){
	        theDir.mkdirs();
	    }
	    
	    return directory + fileName + ".java";
	}
	
	public static String getLocationOfResourceFile(String fileName, String ext) {
		BufferedWriter out = null;
		 
 	    String path2 = TryingApplication.class.getPackageName().replace(".", "\\");
	    	System.out.println(path2);
	
	    //Specify directory
		FileSystem fileSystem = FileSystems.getDefault();
		Path path = fileSystem.getPath("").toAbsolutePath();
	    String directory = path.toString() + "\\src\\main\\resources\\";
	    return directory + fileName + "." + ext;
	}
	
	public static boolean CheckFileExists(String fullLocationAndName) {
		Path checkName= Paths.get ( fullLocationAndName);
		if(Files.exists(checkName)) {
	    	return true;
	    }else {
	    	return false;
	    }
		
	}
	
	
	
	
	public static void createService(Class<?> clazz) throws IOException{
		BufferedWriter out = null;	
		String className= clazz.getSimpleName()+"Service";
		String parentName= clazz.getSimpleName();
		String location= getLocationOfFile("service",className);
     		boolean resCheck= CheckFileExists(location);
		    //Specify filename
		    if(resCheck) {
		    	System.out.println("File is already exist");
		    	return ;
		    }else {
		    	try {
					 String name= clazz.getSimpleName().toLowerCase();	
			    	 FileWriter fstream = new FileWriter(location);
					    out = new BufferedWriter(fstream);
					    StringBuilder strBuilder= new StringBuilder();
					    strBuilder.append("package " +TryingApplication.class.getPackageName() +".service"+";\n");
//					    strBuilder.append("import" +TryingApplication.class.getPackageName() +".mapper."+clazz.getSimpleName()+"Mapper;\n");
//					    strBuilder.append("org.springframework.beans.factory.annotation.Autowired;\n");
//					    strBuilder.append("org.springframework.stereotype.Service;\n");
					    strBuilder.append("import com.example.trying.entity.*;\n");
					    strBuilder.append("import java.util.List;\n");
					    strBuilder.append("\n\n\n\n");
					    strBuilder.append("public interface "+ className+" {\n");
					    //insert
					    strBuilder.append("\n\n");
					    strBuilder.append("\tpublic int insert"+parentName+"("+ parentName + " "+parentName.toLowerCase()+");\n");
					    
					    
					    //update 
					    strBuilder.append("\n\n");
					    strBuilder.append("\tpublic int update"+parentName+"("+ parentName + " "+parentName.toLowerCase()+");\n");
					    
					   
					    
					    
					    //getAll
					    strBuilder.append("\n\n");
					    strBuilder.append("\tpublic List<"+parentName+"> getAll"+parentName+"s();\n");
					    
					    
					  //getById
					    strBuilder.append("\n\n");
					    strBuilder.append("\tpublic "+parentName+" get"+parentName+"ById(Integer id);\n");
					    
					    
					    //DeleteById
					    strBuilder.append("\n\n");
					    strBuilder.append("\tpublic int delete"+parentName+"ById(Integer id);\n");
				
					    strBuilder.append("}\n");
					    
					    //insert your xml content here
					    out.write(strBuilder.toString());
					} catch (Exception e) {
					    System.err.println("Error: " + e.getMessage());
					} finally {
					    //Close the output stream
						out.close();
					}
		    }
	}
	
	public static void createServiceImpl(Class<?> clazz) throws IOException {
		BufferedWriter out = null;	
		String className= clazz.getSimpleName()+"ServiceImpl";
		String parentName= clazz.getSimpleName();
		String location= getLocationOfFile("service\\impl",className);
     		boolean resCheck= CheckFileExists(location);
		    //Specify filename
		    if(resCheck) {
		    	System.out.println("File is already exist");
		    	return ;
		    }else {
		    	try {
					 String name= clazz.getSimpleName().toLowerCase();	
			    	 FileWriter fstream = new FileWriter(location);
					    out = new BufferedWriter(fstream);
					    StringBuilder strBuilder= new StringBuilder();
					    strBuilder.append("package " +TryingApplication.class.getPackageName() +".service.impl"+";\n");
					    strBuilder.append("import " +TryingApplication.class.getPackageName() +".mapper."+clazz.getSimpleName()+"Mapper;\n");
					    strBuilder.append("import " +TryingApplication.class.getPackageName() +".service."+clazz.getSimpleName()+"Service;\n");
					    strBuilder.append("import org.springframework.stereotype.Service;\n");
					    strBuilder.append("import org.springframework.beans.factory.annotation.Autowired;\n");
					    strBuilder.append("import com.example.trying.entity.*;\n");
					    strBuilder.append("import java.util.List;\n");
					    strBuilder.append("\n\n\n\n");
					    strBuilder.append("@Service\n");
					    strBuilder.append("public class "+ className+" implements "+clazz.getSimpleName()+"Service {\n\n\n");
					    strBuilder.append("\t @Autowired\n");
					    strBuilder.append("\t private "+ parentName+"Mapper"+ " "+parentName.toLowerCase()+"Mapper;\n");
					    //insert
					    strBuilder.append("\n\n\n\n");
					    strBuilder.append("\t @Override\n");
					    strBuilder.append("\t public int insert"+parentName+"("+ parentName + " "+parentName.toLowerCase()+"){\n");
					    strBuilder.append("\t\t return "+parentName.toLowerCase()+"Mapper.insert"+parentName+"("+parentName.toLowerCase()+");\n");
					    strBuilder.append("\t }\n");
					    
					    //update 
					    strBuilder.append("\n\n\n\n");
					    strBuilder.append("\t @Override\n");
					    strBuilder.append("\t public int update"+parentName+"("+ parentName + " "+parentName.toLowerCase()+"){\n");
					    strBuilder.append("\t\t return "+parentName.toLowerCase()+"Mapper.update"+parentName+"("+parentName.toLowerCase()+");\n");
					    strBuilder.append("\t }\n");
					   
					    
					    
					    //getAll
					    strBuilder.append("\n\n\n\n");
					    strBuilder.append("\t @Override\n");
					    strBuilder.append("\t public List<"+parentName+"> getAll"+parentName+"s(){\n");
					    strBuilder.append("\t\t return "+parentName.toLowerCase()+"Mapper.getAll"+parentName+"s();\n");
					    strBuilder.append("\t }\n");
					    
					    
					  //getById
					    strBuilder.append("\n\n\n\n");
					    strBuilder.append("\t @Override\n");
					    strBuilder.append("\t public "+parentName+" get"+parentName+"ById(Integer id){\n");
					    strBuilder.append("\t\t return "+parentName.toLowerCase()+"Mapper.get"+parentName+"ById(id);\n");
					    strBuilder.append("\t }\n");
					    
					    
					    //DeleteById
					    strBuilder.append("\n\n\n\n");
					    strBuilder.append("\t @Override\n");
					    strBuilder.append("\t public int delete"+parentName+"ById(Integer id){\n");
					    strBuilder.append("\t\t return "+parentName.toLowerCase()+"Mapper.delete"+parentName+"ById(id);\n");
					    strBuilder.append("\t }\n");
				
					    strBuilder.append("}\n");
					    
					    //insert your xml content here
					    out.write(strBuilder.toString());
					} catch (Exception e) {
					    System.err.println("Error: " + e.getMessage());
					} finally {
					    //Close the output stream
						out.close();
					}
		    }
	}
	
	public static void createMapper(Class<?> clazz) throws IOException {
		BufferedWriter out = null;	
		String className= clazz.getSimpleName()+"Mapper";
		String parentName= clazz.getSimpleName();
		String location= getLocationOfFile("mapper",className);
     		boolean resCheck= CheckFileExists(location);
		    //Specify filename
		    if(resCheck) {
		    	System.out.println("File is already exist");
		    	return ;
		    }else {
		    	try {
					 String name= clazz.getSimpleName().toLowerCase();	
			    	 FileWriter fstream = new FileWriter(location);
					    out = new BufferedWriter(fstream);
					    StringBuilder strBuilder= new StringBuilder();
					    strBuilder.append("package " +TryingApplication.class.getPackageName() +".mapper"+";\n");
					    strBuilder.append("import org.apache.ibatis.annotations.*;\n");
					    strBuilder.append("import com.example.trying.entity.*;\n");
					    strBuilder.append("import java.util.List;\n");
					    strBuilder.append("\n\n\n\n");
					    strBuilder.append("@Mapper\n");
					    strBuilder.append("public interface "+ className+" {\n");
					    //insert
					    strBuilder.append("\n\n");
					    strBuilder.append("\tpublic int insert"+parentName+"("+ parentName + " "+parentName.toLowerCase()+");\n");
					    
					    
					    //update 
					    strBuilder.append("\n\n");
					    strBuilder.append("\tpublic int update"+parentName+"("+ parentName + " "+parentName.toLowerCase()+");\n");
					    
					   
					    
					    
					    //getAll
					    strBuilder.append("\n\n");
					    strBuilder.append("\tpublic List<"+parentName+"> getAll"+parentName+"s();\n");
					    
					    
					  //getById
					    strBuilder.append("\n\n");
					    strBuilder.append("\tpublic "+parentName+" get"+parentName+"ById(Integer id);\n");
					    
					    
					    //DeleteById
					    strBuilder.append("\n\n");
					    strBuilder.append("\tpublic int delete"+parentName+"ById(Integer id);\n");
				
					    strBuilder.append("}\n");
					    
					    //insert your xml content here
					    out.write(strBuilder.toString());
					} catch (Exception e) {
					    System.err.println("Error: " + e.getMessage());
					} finally {
					    //Close the output stream
						out.close();
					}
		    }
	}
	public static void createController(Class<?> clazz) throws IOException {
		
		BufferedWriter out = null;	
		String className= clazz.getSimpleName()+"Controler";
		String parentName= clazz.getSimpleName();
		String location= getLocationOfFile("controler",className);
     		boolean resCheck= CheckFileExists(location);
		    //Specify filename
		    if(resCheck) {
		    	System.out.println("File is already exist");
		    	return ;
		    }else {
		    	
				try {
				 String name= clazz.getSimpleName().toLowerCase();	
		    	 FileWriter fstream = new FileWriter(location);
				    out = new BufferedWriter(fstream);
				    StringBuilder strBuilder= new StringBuilder();
				    strBuilder.append("package " +TryingApplication.class.getPackageName() +".controler"+";\n");
				    strBuilder.append("import "+TryingApplication.class.getPackageName() +".service.*"+";\n" );
				    strBuilder.append("import org.springframework.web.bind.annotation.*;\n");
				    strBuilder.append("import org.springframework.beans.factory.annotation.Autowired;\n");
				    strBuilder.append("import com.example.trying.entity.*;\n");
				    strBuilder.append("import java.util.List;\n");
//				    strBuilder.append("import org.slf4j.Logger;\n");
				    strBuilder.append("\n\n\n\n");
				    strBuilder.append("@RestController\n");
				    strBuilder.append("@RequestMapping(\"/"+name+ "Api\")\n");
//				    strBuilder.append("@Slf4j\n");
				    strBuilder.append("public class "+ className+" {\n\n\n");
				    strBuilder.append("\t @Autowired\n");
				    strBuilder.append("\t private "+ parentName+"Service"+ " "+parentName.toLowerCase()+"Service;\n");
				    //insert
				    strBuilder.append("\n\n\n\n");
				    strBuilder.append("\t @PostMapping\n");
				    strBuilder.append("\t public int insert"+parentName+"(@RequestBody "+ parentName + " "+parentName.toLowerCase()+"){\n");
				    strBuilder.append("\t\t return "+parentName.toLowerCase()+"Service.insert"+parentName+"("+parentName.toLowerCase()+");\n");
				    strBuilder.append("\t }\n");
				    
				    //update 
				    strBuilder.append("\n\n\n\n");
				    strBuilder.append("\t @PutMapping\n");
				    strBuilder.append("\t public int update"+parentName+"(@RequestBody "+ parentName + " "+parentName.toLowerCase()+"){\n");
				    strBuilder.append("\t\t return "+parentName.toLowerCase()+"Service.update"+parentName+"("+parentName.toLowerCase()+");\n");
				    strBuilder.append("\t }\n");
				   
				    
				    
				    //getAll
				    strBuilder.append("\n\n\n\n");
				    strBuilder.append("\t @GetMapping\n");
				    strBuilder.append("\t public List<"+parentName+"> getAll"+parentName+"s(){\n");
				    strBuilder.append("\t\t return "+parentName.toLowerCase()+"Service.getAll"+parentName+"s();\n");
				    strBuilder.append("\t }\n");
				    
				    
				  //getById
				    strBuilder.append("\n\n\n\n");
				    strBuilder.append("\t @GetMapping(\"/{id}\")\n");
				    strBuilder.append("\t public "+parentName+" get"+parentName+"ById(@PathVariable(\"id\") Integer id){\n");
				    strBuilder.append("\t\t return "+parentName.toLowerCase()+"Service.get"+parentName+"ById(id);\n");
				    strBuilder.append("\t }\n");
				    
				    
				    //DeleteById
				    strBuilder.append("\n\n\n\n");
				    strBuilder.append("\t @DeleteMapping(\"/{id}\")\n");
				    strBuilder.append("\t public int delete"+parentName+"ById(@PathVariable(\"id\") Integer id){\n");
				    strBuilder.append("\t\t return "+parentName.toLowerCase()+"Service.delete"+parentName+"ById(id);\n");
				    strBuilder.append("\t }\n");
			
				    strBuilder.append("}\n");
				    
				    //insert your xml content here
				    out.write(strBuilder.toString());
				} catch (Exception e) {
				    System.err.println("Error: " + e.getMessage());
				} finally {
				    //Close the output stream
					out.close();
				}
				
		       
		    	
		    }
		    // Create file 
	
		
//		System.out.println(hello.getClass().getAnnotations());
//		for(Annotation an : anoo) {
//			System.out.println("Hello");
//			System.out.print(an.getClass().getName());
//		}
		
		
	}
	
	
	public static <T> T createInstanceWithArguments(Class<T> clazz, Object ...args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for(Constructor<?> constructor: clazz.getDeclaredConstructors()) {
			
				constructor.setAccessible(true);
				return (T) constructor.newInstance();
			
		}
		
		
		System.out.println("An approprate constructor was not found");
		return null;
		
		
		
	}
	
	private static void callInvokeMethod(Object instance, Method method) throws Throwable {
		
		
			try {
				method.invoke(instance);
				
			} catch (InvocationTargetException e) {
			 System.out.print("exception of invoking the method");   
			}	
			
		
	}
	
	private static List<Method> getAllMethods(Class<?> clazz){
		List<Method> initMethods= new ArrayList<>();
		for(Method method: clazz.getDeclaredMethods()) {
				initMethods.add(method);
		}
		
		return initMethods;
		
	}
	public static List<Class<?>> getAllClasses(String... packageNames) throws URISyntaxException, IOException, ClassNotFoundException {
        List<Class<?>> allClasses = new ArrayList<>();

        for (String packageName : packageNames) {
            String packageRelativePath = "/"+ packageName.replace('.', '/');

            URI packageUri = TryingApplication.class.getResource(packageRelativePath).toURI();

            if (packageUri.getScheme().equals("file")) {
                Path packageFullPath = Paths.get(packageUri);
                allClasses.addAll(getAllPackageClasses(packageFullPath, packageName));
            } else if (packageUri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(packageUri, Collections.emptyMap());

                Path packageFullPathInJar = fileSystem.getPath(packageRelativePath);
                allClasses.addAll(getAllPackageClasses(packageFullPathInJar, packageName));

                fileSystem.close();
            }
        }
        return allClasses;
    }
	
	private static List<Class<?>> getAllPackageClasses(Path packagePath, String packageName ) throws IOException, ClassNotFoundException{
		if(!Files.exists(packagePath)) {
			return Collections.EMPTY_LIST;
		}
		
		List<Path> filePaths= Files.list(packagePath).filter(Files::isRegularFile).collect(Collectors.toList());
		List<Class<?>> classes= new ArrayList<>();
		
		for(Path filePath : filePaths) {
			String fileName= filePath.getFileName().toString();
			if(fileName.endsWith(".class")) {
				String classFullName= packageName + "." + fileName.replaceFirst("\\.class$", "");
//				System.out.println(classFullName);
				Class<?> clazz= Class.forName(classFullName);
				classes.add(clazz);
				
			}
			
		}
		return classes;
	}
	
  

  }
