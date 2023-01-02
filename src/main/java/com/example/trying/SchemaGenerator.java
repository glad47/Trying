package com.example.trying;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.trying.Constant.ReferenceOptions;
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
import com.example.trying.exception.UncompatibleAnnotationException;
import com.example.trying.utils.GFG;

public class SchemaGenerator {
	
	
	// middle order in the schema files
		// joinTable annotated with Table annotation (the referencing class is just annotated with Table) (Join Tables in the database)
		StringBuilder joinTablesStringBuilder= new StringBuilder();
		
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
		
	//first generate the schema and then create a schema.sql file and write the generated schema to it 
		public <T> void createSchemaFile(List<Class<?>> targetedClasses) throws IOException {
			// define the schema builder that will include all schema part of the tables and joinTables and alter statement 
					StringBuilder schemaFile= new StringBuilder();
					//get the path to the current file 
					FileSystem fileSystem = FileSystems.getDefault();
					Path path = fileSystem.getPath("").toAbsolutePath();
					//create dependency handler 
					DependencyHandler handler= new DependencyHandler(tablesMap,orderedTables,altedTableStatments,memoryOfHandlingEqualDependency,joinTablesMap,orderedJoinTables);
					//find the correct order for Tables 
					handler.findDependencyOrder(targetedClasses);
					//find the correct order for JoinTable 
					handler.findDependencyOrderJoinTable(targetedClasses);
					//reset all variable
					setVariableAfterHandlingDependenecy(handler);
					// start writing the schema file 
					schemaFile.append("DROP SCHEMA IF EXISTS `test`;\n"
							+ "CREATE SCHEMA IF NOT EXISTS `test`;\n"
							+ " USE `test`;\n");
					
					// first put all tables in tablesMap in the same order specified orderedTable  
					for(String myTable : orderedTables ) {
						schemaFile.append(tablesMap.get(myTable));
					}
					
					// second put all join tables JoinTable annotated with JoinTable annotation in joinTablesMap in the same order specified orderedJoinTables
					for(String myTable : orderedJoinTables ) {
						schemaFile.append(joinTablesMap.get(myTable));
					}
					
					// third store the join table -> joinTable annotated with Table annotation in joinTablesStringBuilder into schema file 
					schemaFile.append(joinTablesStringBuilder.toString());
					
					// forth store the alter statement in case of equal dependencies
					for(String myAlterSql : altedTableStatments ) {
						schemaFile.append(myAlterSql);
					}
					// create a schema file 
					String file = getLocationOfResourceFile("schema", "sql");
					//if exist then first we will delete that file 
					if(CheckFileExists(file)) {
						//delete that file 
						File fc = new File(file);
					    fc.delete();  
					}
					//with the help of the FileWriterr we are going to write the schema builder to the schema.sql file 
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
						
					}
		}
		
		
		//reset the variables after handling the dependencies
		public void setVariableAfterHandlingDependenecy(DependencyHandler handler) {
			tablesMap= handler.getTablesMap();
			orderedTables= handler.getOrderedTables();
			altedTableStatments= handler.getAltedTableStatments();
			memoryOfHandlingEqualDependency= handler.getMemoryOfHandlingEqualDependency();
			joinTablesMap= handler.getJoinTablesMap();
			orderedJoinTables= handler.getOrderedJoinTables();
		}
		//return a path (String) for the fileName with ext to the resource folder 
		public String getLocationOfResourceFile(String fileName, String ext) {
			BufferedWriter out = null;
			 
	 	    String path2 = TryingApplication.class.getPackageName().replace(".", "\\");
		    	System.out.println(path2);
		
		    //Specify directory
			FileSystem fileSystem = FileSystems.getDefault();
			Path path = fileSystem.getPath("").toAbsolutePath();
		    String directory = path.toString() + "\\src\\main\\resources\\";
		    return directory + fileName + "." + ext;
		}
		
		
		// check weather the file with the current full location name specified name is existed
		public boolean CheckFileExists(String fullLocationAndName) {
			Path checkName= Paths.get ( fullLocationAndName);
			if(Files.exists(checkName)) {
		    	return true;
		    }else {
		    	return false;
		    }
			
		}
		
		// get the count for all fields that is not annotated with relational annotation (not involve in the database schema generation)
		public <T> int activeFeildsNumber(Class<? extends T> clazz){
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
		// check weather ManyToOne annotation already have a corresponding OneToMany
		// in case yes then return true
		// we need this function to not generate two tables for the relationship says Student_Course and Course_Student
		public <T> boolean checkJoinTableAlreadyExsit(Class<? extends T> clazz) throws JoinTableException{
			boolean alreadyExist = false;
			if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
				for(Field field : clazz.getDeclaredFields()) {
					if(field.isAnnotationPresent(ManyToOne.class)) {
						Class<?> referencedClass= field.getType();
						
						for(Field fd : referencedClass.getDeclaredFields() ) {
							if(fd.isAnnotationPresent(OneToMany.class)) {
								Class<?> innerClass= null;
								if(fd.getType().isArray()) {
									// get the array type in case array 
									innerClass = fd.getType().getComponentType();
								}else {
									// get the generic type in case list
									innerClass = (Class<?>) ((ParameterizedType)fd.getGenericType()).getActualTypeArguments()[0];	
								}
								// check weather the referenced class have an annotated field with OneToMany with type equal to the ManytoOne annotated field type  
					            if(innerClass.equals(clazz)) {
					            	alreadyExist=true;
									break;	
					            }
//								throw new JoinTableException("Entity class " + clazz.getName() +""
//										+ " field \"" +field.getName() +"\" annotated with @ManyToOne create same join table with Entity class " + 
//										referencedClass.getName() + " field \""+ field.getName() + ""
//												+ "\" annotated with @OneToMany" );
								
							}
						}
						
					}
					
					
					
				}
				
			}
			return alreadyExist;
			
		}
		//get the name field annotated with primary key annotation (class level)
		public <T> String getPrimaryFieldName(Class<? extends T> clazz) {
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
		// get the name of the the primary key field that will be used while generating schema (including the primaryKey part) 
		public <T> String constractPrimaryKey(Class<? extends T> clazz) {
			String primaryFieldName= "";
			String total="";
			if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
				for(Field field : clazz.getDeclaredFields()) {
					// check weather the primaryKey annotation present 
					if(field.isAnnotationPresent(PrimaryKey.class)) {
						// check weather the Column annotation is present 
						if(field.isAnnotationPresent(Column.class)) {
							Column cloumnAno= field.getAnnotation(Column.class);
							// if the Column name annotation's parameter name is specified 
							if(cloumnAno != null && !cloumnAno.name().equals("")) {
								// get the column name ass the primary key name 
								primaryFieldName = cloumnAno.name();
							}else{
								// Column annotation present but no name specified, directly get the name of the primary key and change it to snake format 
								primaryFieldName = GFG.camelToSnake(field.getName());
							}
							
						}else {
							// no Column annotation present directly get the name of the primary key and change it to snake format 
							primaryFieldName = GFG.camelToSnake(field.getName());
						}
					
						break;
					}
					
					
					
				}
				// get the primary key part names field that used in generating the schema 
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
				
				// concatenate the whole name 
				total = primaryFieldName +","+ total;
				char[] charArr = total.toCharArray();
				// to remove the last comma from the pervious loop 
				char[] newArr = new char[charArr.length-1];
				for(int i=0; i< charArr.length -1 ;i++) {
					newArr[i]=charArr[i] ;
				}
				
				return "PRIMARY KEY(" +new String(newArr) + ")\n";
				
			}else {
				// class is not annotated with Table or JoinTable annotation 
				return "";
			}
			
			
		}
		
		// get the total number of the forigner key for the given class 
		public <T> int getTotalForignerKey(Class<? extends T> clazz){
			int count=0;
			// check weather the class is annotated with Table or JoinTable in case yes then enter 
			if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
				// loop through the fields of the class 
				for(Field field : clazz.getDeclaredFields()) {
					// if the filed is annotated with forigner Key annotation then increment the counter 
					if(field.isAnnotationPresent(ForignerKey.class)) {
						count++;
					}
				}
				
			}
			return count;
			
		}
		
		
		// Check weather the filed annotated with ForignerKey or ForignerKeyPart annotations is annotated correctly 
		public <T> void checkForignerKeyRules(Class<? extends T> clazz) throws NoSuchFieldException, SecurityException, ForignerKeyException{
			// check weather the class is annotated with either Table or JoinTable annotation 
			if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
				// loop through all the fields 
				for(Field field : clazz.getDeclaredFields()) {
					// check weather the filed is annotated with ForignerKey or ForignerKeyPart annotations 
					if(field.isAnnotationPresent(ForignerKey.class) || field.isAnnotationPresent(ForignerKeyPart.class)) {
						// case ForignerKey 
						if(field.isAnnotationPresent(ForignerKey.class)) {
							ForignerKey forignerKey= field.getAnnotation(ForignerKey.class);
							Class<?> referencedClass = forignerKey.referencedClass();
							// might be a bug
							// in case the referencedColumn is specified, then we will check weather that field is PrimaryKey or PriamryKeyPart if not throw an Exception   
							if(!forignerKey.referencedColumn().equals("")) {	
							Field fd = referencedClass.getDeclaredField(forignerKey.referencedColumn());
							if(!(fd.isAnnotationPresent(PrimaryKey.class) || fd.isAnnotationPresent(PrimaryKeyPart.class) )) {
								throw new ForignerKeyException("@ForignerKey referencedColumn annotated feild \""+field.getName()+"\" in the Entity class " + clazz.getName() + ""
										+ " cannot be unindex");
							}
							}
							// case ForignerKeyPart 
						}else {
							ForignerKeyPart forignerKeyPart= field.getAnnotation(ForignerKeyPart.class);
							Class<?> referencedClass = forignerKeyPart.referencedClass();
							// in case the referencedColumn is specified, then we will check weather that field is PrimaryKey or PriamryKeyPart if not throw an Exception
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
		// get the class's field object , by class type and fieldName 
		public <T> Field getJustThatSpecificField(Class<? extends T> clazz, String fieldName) throws ForignerKeyException {
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
		
		
		
		
		
		
		
		
		
		// get the in target class field  referenced by ForignerKey (return Field object)
		public <T> Field getThatSpecificField(Class<? extends T> clazz, String fieldName, Field referencingField) throws ForignerKeyException {
			// check weather the class is annotated with Table or JoinTable annotation 
			if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
				//field not found exception 
				try {
					// get the field specified by the fieldName of the target class clazz 
					Field field = clazz.getDeclaredField(fieldName);
					// check the Compatibility of the filed type of the target class clazz and check weather 
					// it is compatible with the referencing filed which annotated with ForignerKey or ForignerKeyPart
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
						//if it is not compatible then throw the exception 
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
		
		
		
		
		// get the info of the filed referenced by ForignerKey annotation (target class,target fields)
		public <T> List<String> getForignerFieldInfo(Class<? extends T> clazz,Field referencedfield) {
			List<String> res= new ArrayList<>();
			String referencedTable;
			// get the referenced table name as where it will be used in schema generating 
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
			// get the field name as if would used for schema generating 
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
			// get the target filed attribute(ZeroFill and Unsigned) 
			attributes = getFieldAttributes(referencedfield);
			String fieldInfo= getFieldDatatypeSizeAttr(referencedfield, size, afterDec,attributes  );
			// return an array with the first is the field info include DataType, size, attributes  
			// the second is the name of the column (in the database)
			// the third is the name of the table (class) (in the database)
			res.add(fieldInfo);
			res.add(columnName);
			res.add(referencedTable);
			return res;
			
		}
		// print the Statement of the create table used in the schema generating 
		public <T> String printDeclaredFieldsInfo(Class<? extends T> clazz, Class<? extends T> parentClass, int uni) throws IllegalArgumentException, IllegalAccessException, ForignerKeyException, UncompatibleAnnotationException, JoinTableException, NoSuchFieldException, SecurityException {
			// no need to execute this function directly, it only can be called from the createJoinTable to this function 
			// we identified this scenario with the check weather the parentClass is null with unique indicator being 0 
			// then it must be called from the main loop no the create joinTable 
			//uni = 0 , parentClass not null -> OneToMany -> from createJointTable 
			//uni = 1 , parentClass is not null -> ManyToOne -> from createJointTable
			//uni = 2, parent class in null -> from createJointTable
			if(clazz.isAnnotationPresent(JoinTable.class) && parentClass == null && uni== 0) {
				return "";
			}
			// only execute this function for entity's classes annotated with Table and JoinTable 
			if(!(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class))) {
				return "";
			}
			// store the main section of the schema 
			StringBuilder strBuilder= new StringBuilder();
			// store the sectionof the ForignerKey 
			//ex -> CONSTRAINT fk_rating_teacher_teacher_id
			//			FOREIGN KEY rating(teacher_id) REFERENCES teacher(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
			StringBuilder forignerKeyStringBuilder= new StringBuilder();
			
			String className = "";
			//get the total ForignerKey number
			int totalForignerKeys= getTotalForignerKey(clazz);
			// start creating the schema 
			strBuilder.append("Create Table ");
			//get the name of the class that will be used in the schema 
			if(clazz.isAnnotationPresent(Table.class)) {
				Table tableAnnotaion= clazz.getAnnotation(Table.class);
				//in case the name attribute for the Table annotation is there then we use that name 
				if(tableAnnotaion != null && !tableAnnotaion.name().equals("")) {
					strBuilder.append(tableAnnotaion.name()+ "\n(\n");
					className=tableAnnotaion.name();
				}// just use the default name which is the class name converted into snake 
				else {
					strBuilder.append(GFG.camelToSnake(clazz.getSimpleName()) + "\n(\n" );
					className=GFG.camelToSnake(clazz.getSimpleName());
				}
				
			// the table is annotated with the JoinTable 	
			}else if(clazz.isAnnotationPresent(JoinTable.class)) {
				//get the name of the class that will be used in the schema 
				JoinTable tableAnnotaion= clazz.getAnnotation(JoinTable.class);
				if(tableAnnotaion != null && !tableAnnotaion.name().equals("")) {
					strBuilder.append(tableAnnotaion.name()+ "\n(\n");
					className=tableAnnotaion.name();
				}else {
					strBuilder.append(GFG.camelToSnake(clazz.getSimpleName()) + "\n(\n" );
					className=GFG.camelToSnake(clazz.getSimpleName());
				}
			}
			//responsible for the number of each loop while looping through all the active fields (not including those whole annotated 
			// with OneToMany, ManyToOne, and ManyToMany )
			int count = 0;
			boolean thereIsForignerKey= false;
			Field[] fiels= clazz.getDeclaredFields();
			// get the total number of the field involve in creating the schema 
			int fieldSize= activeFeildsNumber(clazz);
			// loop through all fields 
			for(Field field : clazz.getDeclaredFields()) {
				// default size 
				int size = 0;
				// size after decimal point (how many number after decimal point)
				int afterDec = 2;
				String columnName = "";
				// check weather is annotated with the ignore sign just ignore it 
				if(  field.isAnnotationPresent(IgnoreField.class)  ) {
					
					continue;
					
				}
				//extract the column name used in the database schema from the field 
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
					// if the field is not annotated with column and it is not annotated with relational annotations
					// then get the default name
					// because relational filed is not to be include with the current schema instead with it will be create it is own join table 
					if(!(field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class) ||
							field.isAnnotationPresent(ManyToMany.class))) {
					strBuilder.append(GFG.camelToSnake(field.getName()) + " ");
					columnName=GFG.camelToSnake(field.getName());
					}
				}
				
				
//				System.out.println(String.format("Field name: %s, type: %s",
//						field.getName(),
//						field.getType().getName()
//					
//						));
//				
//				System.out.println(String.format("is Syntheic Field : %s ", field.isSynthetic()));
				
				
				
				// check what might the field is annotated with and keep track of all these with the following variables 
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
					// relational annotations is exist
					if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(ManyToMany.class) ) {
						
						// output from createTheJoinTable 1) empty string in case the (relational annotation OneToMany, ManyToOne and ManyToMany) referencing JoinTable 
						// if the class referencing it is Table annotated then the string will be added to this global variable which would be used later to 
						// form the correct order of the schema with the help of finding dependencies functions 
						String joinTable = createTheJoinTable(clazz,field );
						// append the JoinTable with the joinTablesStringBuilder to decide the right location of the schema in the schema file 
						joinTablesStringBuilder.append(joinTable );
						//no need to complete executing of the loop 
						continue;
					}
					// it is a ForignerKey 
					if(field.isAnnotationPresent(ForignerKey.class)) {
						// Check weather the filed annotated with ForignerKey or ForignerKeyPart annotations is annotated correctly
						checkForignerKeyRules(clazz);
						ForignerKey forigner= field.getAnnotation(ForignerKey.class);
						Class<?> referencedClass= forigner.referencedClass();
						String endPartFk="";
						String fieldName= forigner.referencedColumn();
						//if the referenced column is set then we are going to use it otherwise we are going to use target class primary key 
						if(fieldName.equals("")) {
							//find the primary field name 
							fieldName= getPrimaryFieldName(referencedClass);
						}
						// get the field object for the filedName
						Field targetClassField = getThatSpecificField(referencedClass, fieldName,field);
						//get the info of the target field represented as follow
						// res contains-> fieldInfo(1) Datatype ,Size, attributes(ZeroFill, Unsigned), 2) ColumnName, 3) Table Name )
						// res represent the target class 
						res= getForignerFieldInfo(referencedClass,targetClassField);
						// set the indicator of there is a ForignerKey 
						thereIsForignerKey =true;
						// decrease the number of the ForignerKey
						if(totalForignerKeys > 0) {
							endPartFk = ",";
							totalForignerKeys--;
						}
						
					    // construct the constaint 
						//something similar to CONSTRAINT fk_student_city_id
//						      FOREIGN KEY (city_id) REFERENCES city(id)
						String contraint = constractTheForignerKey(clazz,forigner,endPartFk,res,className, columnName);
						//put the final result into forignerKeyStringBuilder
						forignerKeyStringBuilder.append(contraint);
//						
						
						
					}
					if(field.isAnnotationPresent(ForignerKeyPart.class)) {
						// res contains-> fieldInfo(1) Datatype ,Size, attributes(ZeroFill, Unsigned), 2) ColumnName, 3) Table Name )
						// res represent the target class 
						res= getForignerKeyPartInfo(field);
						
						
						
					}
					
					
					String endPart= endPartConstraction(count, fieldSize, primary, notNull, unique , autoIncrement,identity,thereIsForignerKey);
					   
					//constructing the end part of the ForignerKey or ForignerKeyPart why it is not the same as the normal end part?
					// because this function it might be called recursively in case the OneToMany, ManyToOne, and ManyToMany and the referenced classes 
					// were JoinTable so in that case we would have at least two ForignerKeys and if it is OneToMany or ManyToOne we would have one as unique  
					String uniquex= "";
						if(field.isAnnotationPresent(ForignerKey.class) || field.isAnnotationPresent(ForignerKeyPart.class) ){
							// check weather the field is in JoinTable, function called recursively with the parentClass which is the JoinTable class from
							// createJoinTable and the parentClass is indicator of that it is ManyToOne or OneToMany annotation
							if(parentClass != null && field.isAnnotationPresent(ForignerKey.class) ) {
								ForignerKey fk= field.getAnnotation(ForignerKey.class);
								// it is the type of OneToMany 
								if(uni == 0) {
									// the target class is unique
									if(!fk.referencedClass().equals(parentClass)) {
										uniquex= " UNIQUE";
										
									}	
								// it is the type of ManyToOne	
								}else if(uni == 1) {
									// the current class is unique 
									if(fk.referencedClass().equals(parentClass)) {
										uniquex= " UNIQUE";
										
									}
								}
								
								

							}
							// any case weather it is recursively called from CreateJoinTable for ManyToMany or just our notmal case 
							if(!unique.equals("")) {
								uniquex=unique;
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

				
				count++;
			}
			// append the constraint to the end of the table schema 
			strBuilder.append(forignerKeyStringBuilder.toString());
			//primary key  (including the primaryKey part)
			strBuilder.append(constractPrimaryKey(clazz));
			strBuilder.append(");\n");
			
			// check weather it is not JoinTable 
			if(clazz.isAnnotationPresent(Table.class)) {
				// case yes then store the result into the Table map
				tablesMap.put(clazz.getSimpleName(), strBuilder.toString());	
			}
			/// else return the result to the createJoinTable function
		    return strBuilder.toString();
		}
		
		// print the Crate table schema for the Join Tables -> input (class, and the filed annotated with any OneToMany, ManyToOne, ManyToMany)
		public <T> String createTheJoinTable(Class<? extends T> clazz, Field field) throws ForignerKeyException, IllegalArgumentException, IllegalAccessException, UncompatibleAnnotationException, JoinTableException, NoSuchFieldException, SecurityException {
			// check weather the joinTable is already printed -> Case Student_Coursse , Course_Student
			if(checkJoinTableAlreadyExsit(clazz)) {
				return "";
			}
			// Declare the required fields 
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
			// we have three cases
			// the filed is annotated with the OneToMany class
			if(field.isAnnotationPresent(OneToMany.class)) {
				// extract the required info from the annotation  
				OneToMany oneToMany= field.getAnnotation(OneToMany.class);
				joinTableName= oneToMany.joinTableName();	
				referencedFieldName = oneToMany.referencedColumn();
				chosenFieldName = oneToMany.chosenColumn();
				fkName="";
				onUpdate= oneToMany.onUpdate();
				onDelete= oneToMany.onDelete();
				// OneToMany annotate array or List
				// if it is an array it will not be null (getting the array type)
				referencedClass = field.getType().getComponentType();
				if(referencedClass == null) {
					// the filed is a list (getting the list generic type)
					referencedClass = (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
				}
				// case ManyToOne 
			}else if(field.isAnnotationPresent(ManyToOne.class)) {
				//extract the required info from the annotation 
				ManyToOne manyToOne= field.getAnnotation(ManyToOne.class);
				joinTableName= manyToOne.joinTableName();
				referencedFieldName = manyToOne.referencedColumn();
				chosenFieldName = manyToOne.chosenColumn();
				fkName= "";
				onUpdate= manyToOne.onUpdate();
				onDelete= manyToOne.onDelete();
				referencedClass = field.getType();
				// set the indicator 
				unique=1;
				//case ManyToMany
			}else if(field.isAnnotationPresent(ManyToMany.class)) {
				ManyToMany manyToMany= field.getAnnotation(ManyToMany.class);
				// if ManyToMany annotation define mapBy then we don't need to process any Further (not responsible of generating Schema but it would be helpful for xml and so on)
				if(!manyToMany.mapBy().equals("")) {
					return "";
				}
				//extract the info from the annotation 
				joinTableName= manyToMany.joinTableName();	
				referencedFieldName = manyToMany.referencedColumn();
				chosenFieldName = manyToMany.chosenColumn();
				fkName= "";
				onUpdate= manyToMany.onUpdate();
				onDelete= manyToMany.onDelete();
				// ManyToMany annotate array or List
				// if it is an array it will not be null (getting the array type)
				referencedClass = field.getType().getComponentType();
				if(referencedClass == null) {
					// the filed is a list (getting the list generic type)
					referencedClass = (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
				}
				//setting the indicator
				unique=2;
			}
			
			// start creating the schema for the JoinTable 
			if(referencedClass.isAnnotationPresent(Table.class)) {
				strBuilder.append("Create Table ");
				//get the joinTableName
				//it will consists of two parts referencing class name + referenced class name  
				if(joinTableName.equals("")) {
					joinTableName= GFG.camelToSnake(clazz.getSimpleName() + referencedClass.getSimpleName());
				}
				strBuilder.append(joinTableName +"\n(\n");
				// if the target class column is not specified then we will get the name of the PrimaryKey of the target class (Referenced class)
				if(referencedFieldName.equals("")) {
					referencedFieldName = getPrimaryFieldName(referencedClass);
				}
				// if the class column is not specified then we will get the name of the PrimaryKey of the class (Referencing class)
				if(chosenFieldName.equals("")) {
					chosenFieldName = getPrimaryFieldName(clazz);
				}
				// get the corresponding filed object
				//targetClassField have no meaningful meaning is just a temporary Filed
				Field targetClassField = getJustThatSpecificField(referencedClass, referencedFieldName);
				// res contains-> fieldInfo(1) Datatype ,Size, attributes(ZeroFill, Unsigned), 2) ColumnName, 3) Table Name )
				// res represent the target class 
				res= getForignerFieldInfo(referencedClass,targetClassField);
				// get the corresponding filed object
				targetClassField = getJustThatSpecificField(clazz, chosenFieldName);
				// res1 contains-> fieldInfo(1) Datatype ,Size, attributes(ZeroFill, Unsigned), 2) ColumnName, 3) Table Name )
				// res1 represent the the current  class 
				res1= getForignerFieldInfo(clazz,targetClassField);
				// generate something like -> Student_id INTEGER(10) Unsigned,
				if(unique == 0) {
					// for OneToMany the referenced class is a unique
					strBuilder.append(res1.get(2)+"_"+chosenFieldName + " " + res1.get(0) + ",\n");
					strBuilder.append(res.get(2)+"_"+referencedFieldName + " " + res.get(0)+" UNIQUE" + ",\n");				
				}else if(unique == 1) {
					// for ManyToOne the current class is unique
					strBuilder.append(res1.get(2)+"_"+chosenFieldName + " " + res1.get(0)+" UNIQUE"+ ",\n");
					strBuilder.append(res.get(2)+"_"+referencedFieldName + " " + res.get(0)+ ",\n");
				}else if(unique == 2) {
					//For ManyToMany nothing is unique 
					strBuilder.append(res1.get(2)+"_"+chosenFieldName + " " + res1.get(0) + ",\n");
					strBuilder.append(res.get(2)+"_"+referencedFieldName + " " + res.get(0)+",\n");
				}

				// include the forignerKey constraints 
				StringBuilder forignerKeyStringBuilder = new StringBuilder();
				
				//now the part for generating something like 
				//FOREIGN KEY comment(rating_id) REFERENCES rating(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
				//case the table name are equal we do not need two part of the above a table referencing its 
				//Student must have many student 
				if(res1.get(2).equals(res.get(2))) {
					
						fkName += "fk_1_"+joinTableName;
					
					forignerKeyStringBuilder.append("CONSTRAINT " + fkName+ "\n");
					forignerKeyStringBuilder.append("\tFOREIGN KEY ("+res1.get(2)+"_"+chosenFieldName+"," 
							+res.get(2)+"_"+referencedFieldName+ ") REFERENCES "+ 
							res1.get(2)+"("+res1.get(1)+","+res.get(1)+")"+
									" ON UPDATE " + onUpdate.getType() + " ON DELETE " + onDelete.getType()+ ",\n" );
					
					//case not 
				}else {
					String fkName1= "";
					
						fkName += "fk_1_"+joinTableName;
						fkName1 +="fk_2_"+joinTableName;
					
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
				
				
				// append the primary key part 
				strBuilder.append("PRIMARY KEY ("+res1.get(2)+"_"+chosenFieldName+"," 
						+res.get(2)+"_"+referencedFieldName+ ")\n");
				
				strBuilder.append(");\n");
				
				
				
			}//if the class is annotated with JoinTable then we need to create specific schema because it may include other fields  
			else if(referencedClass.isAnnotationPresent(JoinTable.class)) {
				//we are working on
				if(unique == 2) {
					// if it is ManyToMany no need to pass the parent class (the current class)  
					strBuilder.append(printDeclaredFieldsInfo(referencedClass,null,unique)); 
				}else {
					// if other then we need to pass the parent class (the current class)
					//constructing the end part of the ForignerKey or ForignerKeyPart inside printDeclaredFieldsInfo it is not the same as the normal end part?
					// because the function printDeclaredFieldsInfo is called recursively in case the OneToMany, ManyToOne, and ManyToMany and the referenced classes 
					// were JoinTable so in that case we would have at least two ForignerKeys and if it is OneToMany or ManyToOne we would have one as unique 
					// passing the parent class with unique will help help us to identify the functionprintDeclaredFieldsInfo is called from the createJoinTable 
					// and also to construct the end part of the ForignerKey and ForignerKeyPart correctly 
					strBuilder.append(printDeclaredFieldsInfo(referencedClass,clazz,unique)); 	
				}
				
			}
			// if the class is annotated with JoinTable then we will add it to the JoinTableMap 
			//(because these files will always be at the end of the schema file because it will depends on the classes annotated with the
			// Table annotation)
			if(referencedClass.isAnnotationPresent(JoinTable.class)) {
				joinTablesMap.put(referencedClass.getSimpleName(), strBuilder.toString());
				return "";
			}
			// else we will return the string as it is
			return strBuilder.toString();
		}
		
		
		
		// construct the referenced part of the forignerKey 
		public <T> String constractReferencedClassColumnName(Class<? extends T> clazz, String columnNameFK, Class<?> referencedClass) throws ForignerKeyException {
			String cloumName= columnNameFK + ','; 
			String tableName="";
			List<String> res= new ArrayList<>();
			// loop through the fields of the target class 
			if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
				for(Field field : clazz.getDeclaredFields()) {
					tableName = getForignerFieldInfo(referencedClass,field).get(2);
					//get all the columns involve in the ForignerKey annotated with the forignerKeyPart that is only referencing  our particular class
					if(field.isAnnotationPresent(ForignerKeyPart.class) ) {
						ForignerKeyPart forignerPart=field.getAnnotation(ForignerKeyPart.class);
						//that is only referencing  our particular class
						if(!forignerPart.referencedClass().equals(referencedClass)) {
							continue;
						}
						// res contains-> fieldInfo(1) Datatype ,Size, attributes(ZeroFill, Unsigned), 2) ColumnName, 3) Table Name )
						// res represent the target class 
						res=getForignerKeyPartInfo(field);
						cloumName += res.get(1) + ',';
						tableName = res.get(2);
						
						
					  }
					}
				}
			//remove the last comma 
			char[] charArr = cloumName.toCharArray();
			char[] newArr = new char[charArr.length-1];
			for(int i=0; i< charArr.length -1 ;i++) {
				newArr[i]=charArr[i] ;
			}
			// return the final result as tablename(columA, columnB,columC..)
			return tableName + "(" +new String(newArr) + ")";
		}
		
		
		
		// get the whole correct columns involve in the creation of the ForignerKey ex -> Student(teacher_id, teacher_name)
		public <T> List<String> constractColumnName(Class<? extends T> clazz, String columnNameFK,String className, Class<?> referencedClass) throws ForignerKeyException {
			// get the main part that annotated with ForignerKey
			String cloumName= columnNameFK + ',';
			// get the className (database name)
		    String tableName= className;
		    List<String> res= new ArrayList<>();
		    // loop through all the fields
			if(clazz.isAnnotationPresent(Table.class) || clazz.isAnnotationPresent(JoinTable.class)) {
				for(Field field : clazz.getDeclaredFields()) {
					 // if the filed is annotated with the ForignerKeyPart then get the column name (as database)
					if(field.isAnnotationPresent(ForignerKeyPart.class) ) {
						ForignerKeyPart forignerPart=field.getAnnotation(ForignerKeyPart.class);
						//that is only referencing  our particular class
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
			
			//remove the last comma 
			char[] charArr = cloumName.toCharArray();
			char[] newArr = new char[charArr.length-1];
			for(int i=0; i< charArr.length -1 ;i++) {
				newArr[i]=charArr[i] ;
			}
			//return all the names of the columns separated by commas  
			res.add(new String(newArr));
			// return the final result as tablename(columA, columnB,columC..)
			res.add(tableName + "(" + new String(newArr) + ")");
			return res;
			
		}
		// construct the ForignerKey
		// ex -> CONSTRAINT fk_Student_teacher_teacher_id
		//				FOREIGN KEY Student(teacher_id, teacher_name) REFERENCES teacher(id,name) ON UPDATE CASCADE ON DELETE NO ACTION,
		// current class, forignerKey annotation, endPartKf (weather these is comma or not ),forignerkey field info, table name, column name)
		public <T> String constractTheForignerKey(Class<? extends T> clazz,ForignerKey forigner, String endPartFk, List<String> primaryKeyRes, String className, String columnNameFK ) throws ForignerKeyException {
			StringBuilder forignerKeyStringBuilder = new StringBuilder();
			
			String fkName = forigner.name();
			ReferenceOptions onUpdate = forigner.onUpdate();
			ReferenceOptions onDelete = forigner.onDelete();
			Class<?> referencedClass= forigner.referencedClass();
			
			
			// construct the first part of the ForignerKey 
			//return all the names of the columns separated by commas  
			// return the final result as tableName(columA, columnB,columC..)
			List<String> columnName= constractColumnName(clazz,columnNameFK,className, referencedClass);
			// the referenced part 
			// construct the second part of ForignerKey constraint ex-> REFERENCES teacher(id,name) ON UPDATE CASCADE ON DELETE NO ACTION,
			// return the final result as tablename(columA, columnB,columC..)
			String referencedClassColumnName= constractReferencedClassColumnName(clazz,primaryKeyRes.get(1), referencedClass);
			if(fkName.equals("")) {
				fkName += "fk_"+className+"_"+primaryKeyRes.get(2)+"_"+columnName.get(0).replaceAll(",", "_");
			}
			forignerKeyStringBuilder.append("CONSTRAINT " + fkName+ "\n");
			forignerKeyStringBuilder.append("\tFOREIGN KEY "+columnName.get(1)+" REFERENCES "+ referencedClassColumnName+
					" ON UPDATE " + onUpdate.getType() + " ON DELETE " + onDelete.getType()+ endPartFk+ "\n" );
			return forignerKeyStringBuilder.toString();
		}
		
		
		
		// get the info of the ForignerkeyPart
		public List<String> getForignerKeyPartInfo(Field field) throws ForignerKeyException {
			List<String> res= new ArrayList<>();
			ForignerKeyPart forignerPart = field.getAnnotation(ForignerKeyPart.class);
			Class<?> referencedClass= forignerPart.referencedClass();
			String fieldName= forignerPart.referencedColumn();
			if(fieldName.equals("")) {
				//find the primary field name
				//the class level 
				fieldName= getPrimaryFieldName(referencedClass);
			}
			// get the field object for the filedName 
			Field targetClassField = getThatSpecificField(referencedClass, fieldName,field);
			//get the info of the target field represented as follow
			// res contains-> fieldInfo(1) Datatype ,Size, attributes(ZeroFill, Unsigned), 2) ColumnName, 3) Table Name )
			// res represent the target class 
			res= getForignerFieldInfo(referencedClass,targetClassField);
			return res;
		}
		
		
		
		// get the datatype of the column in the schema, (field, (size, afterDesc) -> used for generating the Size, attributes -> (ZeroFill and Unsigned))
		public String getFieldDatatypeSizeAttr(Field field, int size, int afterDec, String  attributes) {
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
		
		//the function is used to get the field attributes (ZeroFill and Unsigned)
		public String getFieldAttributes(Field field) {
			
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
		
		
		
		// the function is used to construct the Size part used in the schema 
		public String sizeConstraction(int size, int afterDec) {
			String sizeFinal= "";
			// if the size is not specified then get the normal value of 10
			if(size ==0) {
				sizeFinal += "(10,"+afterDec+")";
				
			}else {
				sizeFinal += "("+ size+ ","+ afterDec + ")";
			}
			
			return sizeFinal;
		}
		
		
		
		// the function will be used to construct each Column specific attributes (NotNull, Unique, AutoIncrement, Identity, Primary)
		// the count is representing the order of the field in the schema while fieldSize represents the number of the active field used in the gernerating the schema 
		public String endPartConstraction(int count, int fieldSize, String primary,String notNull,String unique, String autoIncrement, String identity, boolean check  ) {
			String finalEnd= notNull + unique + autoIncrement + identity + primary;
			// if the count is lager than than fieldSize (no need to print the end part) 
			if(count  < fieldSize) {
				finalEnd += ",\n";
			}
			
			return finalEnd;
			
		}
}
