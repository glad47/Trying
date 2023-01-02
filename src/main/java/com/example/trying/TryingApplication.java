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
	
	
	
	
	public static void main(String[] args) throws Throwable  {
		//get all classes for the package specified
		List<Class<?>> allClassess= getAllClasses("com.example.trying.entity");
		//filter out all the class without AutoGenerate annotation 
		List<Class<?>> targetedClasses=  allClassess.stream().filter(cs -> cs.isAnnotationPresent(AutoGenerate.class)).collect(Collectors.toList());

		//create Schema generator 
		SchemaGenerator schemaGenerator= new SchemaGenerator();
		
		
		
		for(Class<?> cs : targetedClasses ) {
			try {
				//check weather the fields of the classes annotated with Table is annotated correctly
				CheckAnnotationRuleshandler.checkAnnotationFieldsRules(cs);
				//check weather the fields of the classes annotated with JoinTable is annotated correctly
				CheckAnnotationRuleshandler.checkAnnotationFieldsRulesJoinTable(cs);
				// print the Statement of the create table used in the schema generating 
				schemaGenerator.printDeclaredFieldsInfo(cs,null,0);
				// create mapper class
				createMapper(cs);
				// create service class
				createService(cs);
				// create service impl class
				createServiceImpl(cs);
				// create controller class
				createController(cs);
				
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
		schemaGenerator.createSchemaFile(targetedClasses);
		SpringApplication.run(TryingApplication.class, args);
//		System.out.println(tablesMap);
		
		

	}
	
	
	
	
	// create a java class for the given file name and package 
	// will create a package folder if it is not exist 
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
	
	
	//return a path (String) for the fileName with ext to the resource folder 
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
	
	
	// heck wether the file with the current full location name specified name is existed
	public static boolean CheckFileExists(String fullLocationAndName) {
		Path checkName= Paths.get ( fullLocationAndName);
		if(Files.exists(checkName)) {
	    	return true;
	    }else {
	    	return false;
	    }
		
	}
	
	
	
	//create service class for the given entity class if not exists
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
	
	
	//create service impl class for the given entity class if not exists
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
	/*
	 * create mapper class for the given entity class if not exists 
	 * 
	 * */
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
	
	
	//create controller class for the given entity class if not exists 
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
	//get all classes in the current package
	// the input can be more than one packages at the time 
	public static List<Class<?>> getAllClasses(String... packageNames) throws URISyntaxException, IOException, ClassNotFoundException {
        List<Class<?>> allClasses = new ArrayList<>();
        //loop through a;; the packages   
        for (String packageName : packageNames) {
        	//replace all . with / in packagename 
            String packageRelativePath = "/"+ packageName.replace('.', '/');
            // get URI object relative path to the package 
            URI packageUri = TryingApplication.class.getResource(packageRelativePath).toURI();
            // if it is a project 
            if (packageUri.getScheme().equals("file")) {
                Path packageFullPath = Paths.get(packageUri);
                // call the function recursively 
                allClasses.addAll(getAllPackageClasses(packageFullPath, packageName));
                // if it is jar 
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
