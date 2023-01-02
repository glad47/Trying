package com.example.trying;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import com.example.trying.annotation.Database.ForignerKey;
import com.example.trying.annotation.Database.JoinTable;
import com.example.trying.annotation.Database.ManyToMany;
import com.example.trying.annotation.Database.ManyToOne;
import com.example.trying.annotation.Database.OneToMany;
import com.example.trying.annotation.Database.PrimaryKey;
import com.example.trying.annotation.Database.Table;
import com.example.trying.exception.JoinTableException;
import com.example.trying.exception.PrimaryKeyException;
import com.example.trying.exception.UncompatibleAnnotationException;
/*
 * use for checking weather the classes are annotated correctly 
 * 
 */
public class CheckAnnotationRuleshandler {
	/*
	 * check weather the fields of the classes annotated with Table is annotated correctly  
	 * */
	public static <T> void checkAnnotationFieldsRules(Class<? extends T> clazz) throws PrimaryKeyException, UncompatibleAnnotationException, JoinTableException, NoSuchFieldException, SecurityException {
		int count=0;
		// only classes annotated with Table annotation
		if(clazz.isAnnotationPresent(Table.class)) {
			// for each field of the current class
			for(Field field : clazz.getDeclaredFields()) {
				// increment the PrimaryKey annotated field counter 
				if(field.isAnnotationPresent(PrimaryKey.class)) {
					count++;
				}
				// see weather the field is annotated with the relational annotation
				if(field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class) ||
						field.isAnnotationPresent(ManyToMany.class)) {
					
					// here we have three cases one for each annotation type mentioned above with the if statement 
					//case OneToMany
					if(field.isAnnotationPresent(OneToMany.class)) {
						// the field annotated with OneToMany must be an array or list in case not throw an exception 
						if(!(field.getType().isArray() || field.getType().equals(List.class) ) ) {
							throw new JoinTableException("filed \"" + field.getType() +"\" used with @OneToMany must be an array or list");
						}
						 
						OneToMany oneToMany = field.getAnnotation(OneToMany.class);
						// 1) case is array
						if(field.getType().isArray() ) {
							Class<?> referencedClass = field.getType();	
							Class<?> arrayType = referencedClass.getComponentType();
							// check weather the referenced class (array type ) is annotated with either Table or JoinTable
							if(!(arrayType.isAnnotationPresent(JoinTable.class) || arrayType.isAnnotationPresent(Table.class))) {
								throw new UncompatibleAnnotationException("Entity class " + clazz.getName() + " feild type " + arrayType.getName()+
										" must be annotated either with @Table or @JoinTable ");
							}
							// check weather the referencedCloumn is not annotated with any relational annotation (not belong to the table of the database)
							if(!oneToMany.referencedColumn().equals("")) {
								Field feildReferd = referencedClass.getDeclaredField(oneToMany.referencedColumn());
								if((feildReferd.isAnnotationPresent(OneToMany.class) || feildReferd.isAnnotationPresent(ManyToOne.class) 
									    || feildReferd.isAnnotationPresent(ManyToMany.class))) {
									
									throw new JoinTableException("referencedColumn of the @OneToMany annotation of field \""+feildReferd.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +arrayType.getName() );
										
									}
							}
							// check weather the choosenCloumn is not annotated with any relational annotation (not belong to the table of the database)
							if(!oneToMany.chosenColumn().equals("")) {
								Field feildChosen = referencedClass.getDeclaredField(oneToMany.chosenColumn());
								if((feildChosen.isAnnotationPresent(OneToMany.class) ||
								    feildChosen.isAnnotationPresent(ManyToOne.class)  || feildChosen.isAnnotationPresent(ManyToMany.class) )) {
									throw new JoinTableException("chosenColumn of the @OneToMany annotation of field \""+feildChosen.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +arrayType.getName() );
									
								}
							}
							// 2)  list case
						}else if(field.getType().equals(List.class) ) {
							// check weather the generic type of the list type is annotated with Table or JoinTable 
							Class<?> referencedClass = (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];	
							if(!(referencedClass.isAnnotationPresent(JoinTable.class) || referencedClass.isAnnotationPresent(Table.class))) {
								throw new UncompatibleAnnotationException("Entity class " + clazz.getName() + " feild type " + referencedClass.getName()+
										" must be annotated either with @Table or @JoinTable ");
							}
							// check weather the referencedCloumn is not annotated with any relational annotation (not belong to the table of the database)
							if(!oneToMany.referencedColumn().equals("")) {
								Field feildReferd = referencedClass.getDeclaredField(oneToMany.referencedColumn());
								if((feildReferd.isAnnotationPresent(OneToMany.class) || feildReferd.isAnnotationPresent(ManyToOne.class) 
									    || feildReferd.isAnnotationPresent(ManyToMany.class))) {
									throw new JoinTableException("referencedColumn of the @OneToMany annotation of field \""+feildReferd.getName()
											+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +referencedClass.getName() );
										
									}
							}
							// check weather the choosenCloumn is not annotated with any relational annotation (not belong to the table of the database)
							if(!oneToMany.chosenColumn().equals("")) {
								Field feildChosen = referencedClass.getDeclaredField(oneToMany.chosenColumn());
								if((feildChosen.isAnnotationPresent(OneToMany.class) ||
								    feildChosen.isAnnotationPresent(ManyToOne.class)  || feildChosen.isAnnotationPresent(ManyToMany.class) )) {
									throw new JoinTableException("chosenColumn of the @OneToMany annotation of field \""+feildChosen.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +clazz.getName() );
									
								}
							}
							
							
						}
						// check weather there is another annotation annotating the same already annotated field with the OneToMany 
						if(field.getAnnotations().length > 1) {
							throw new UncompatibleAnnotationException("Entity class "+ clazz.getName()+" Feild " + field.getName()
							+ " cannot be annotated with other annotations because it already have been annotated with @OneToMany");
						}
						
					}
					
					//case ManyToOne annotation 
					if(field.isAnnotationPresent(ManyToOne.class)) {
						// check weather the field annotated is type of an array or list in case yes throw an exception it must be a single item 
						if((field.getType().isArray() || field.getType().equals(List.class) ) ) {
							throw new JoinTableException("filed \"" + field.getType() +"\" used with @ManyToOne cannot be an array or list");
						}
						
						// check weather the referenced class is annotated with either Table or JoinTable
						Class<?> referencedClass = field.getType();	
						if(!(referencedClass.isAnnotationPresent(JoinTable.class) || referencedClass.isAnnotationPresent(Table.class)) ) {
							throw new UncompatibleAnnotationException("Entity class " + clazz.getName() + " feild type " + referencedClass.getName()+
									" must be annotated either with @Table or @JoinTable ");
						}
						// check weather the referencedCloumn is not annotated with any relational annotation (not belong to the table of the database)
						ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
						if(!manyToOne.referencedColumn().equals("")) {
							Field feildReferd = referencedClass.getDeclaredField(manyToOne.referencedColumn());
							if((feildReferd.isAnnotationPresent(OneToMany.class) || feildReferd.isAnnotationPresent(ManyToOne.class) 
								    || feildReferd.isAnnotationPresent(ManyToMany.class))) {
									throw new JoinTableException("referencedColumn of the @ManyToOne annotation of field \""+feildReferd.getName()
											+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +referencedClass.getName() );
									
								}
						}
						// check weather the choosenCloumn is not annotated with any relational annotation (not belong to the table of the database)
						if(!manyToOne.chosenColumn().equals("")) {
							Field feildChosen = referencedClass.getDeclaredField(manyToOne.chosenColumn());
							if((feildChosen.isAnnotationPresent(OneToMany.class) ||
							    feildChosen.isAnnotationPresent(ManyToOne.class)  || feildChosen.isAnnotationPresent(ManyToMany.class) )) {
								throw new JoinTableException("chosenColumn of the @ManyToOne annotation of field \""+feildChosen.getName()
								+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +clazz.getName() );
								
								
							}
						}
						// check weather there is another annotation annotating the same already annotated field with the ManyToOne
						if(field.getAnnotations().length > 1) {
							throw new UncompatibleAnnotationException("Entity class "+ clazz.getName()+" Feild " + field.getName()
							+ " cannot be annotated with other annotations because it already have been annotated with @ManyToOne");
						}
						
						
					}
					
					// case ManyToMany
					if(field.isAnnotationPresent(ManyToMany.class)) {
						// the field annotated with ManyToMany must be an array or list in case not throw an exception 
						if(!(field.getType().isArray() || field.getType().equals(List.class) ) ) {
							throw new JoinTableException("filed \"" + field.getType() +"\" used with @OneToMany must be an array or list");
						}
						ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
						// check weather ManyToMany is to defined mapping (not involving in generating the database schema) or generating the database
						//id -> involve in generating database schema , mapBy involve in identify mapping relationship
						// check weather ManyToMany define id or mapBy if not throw exception  
						// case no join table 
						if(!((!manyToMany.id().equals("") && manyToMany.mapBy().equals("")) || (manyToMany.id().equals("") && !manyToMany.mapBy().equals("")) ) ) {
							// both not 
							if(manyToMany.id().equals("") && manyToMany.mapBy().equals("") ) {
								throw new JoinTableException("@ManyToMany annotation must defined an Id or mapBy in Entity Class" + clazz.getName() );
							} 
							// both available 
							if(!manyToMany.id().equals("") && !manyToMany.mapBy().equals("")) {
								throw new JoinTableException("@ManyToMany annotation cannot defined an Id and mapBy both at the same time in Entity Class" + clazz.getName() );
							}
						}
						// case there is JoinTable 
						
						// 1) case is array
						if(field.getType().isArray() ) {
							Class<?> referencedClass = field.getType();	
							Class<?> arrayType = referencedClass.getComponentType();
							// check weather the referenced class (array type) is annotated with either Table or JoinTable
							if(!(arrayType.isAnnotationPresent(JoinTable.class) || arrayType.isAnnotationPresent(Table.class))) {
								throw new UncompatibleAnnotationException("Entity class " + clazz.getName() + " feild type " + arrayType.getName()+
										" must be annotated either with @Table or @JoinTable ");
							}
							// check weather the referencedCloumn is not annotated with any relational annotation (not belong to the table of the database)
							if(!manyToMany.referencedColumn().equals("")) {
								Field feildReferd = referencedClass.getDeclaredField(manyToMany.referencedColumn());
								if((feildReferd.isAnnotationPresent(OneToMany.class) || feildReferd.isAnnotationPresent(ManyToOne.class) 
									    || feildReferd.isAnnotationPresent(ManyToMany.class))) {
									
									throw new JoinTableException("referencedColumn of the @ManyToMany annotation of field \""+feildReferd.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +arrayType.getName() );
										
									}
							}
							// check weather the choosenCloumn is not annotated with any relational annotation (not belong to the table of the database)
							if(!manyToMany.chosenColumn().equals("")) {
								Field feildChosen = referencedClass.getDeclaredField(manyToMany.chosenColumn());
								if((feildChosen.isAnnotationPresent(OneToMany.class) ||
								    feildChosen.isAnnotationPresent(ManyToOne.class)  || feildChosen.isAnnotationPresent(ManyToMany.class) )) {
									throw new JoinTableException("chosenColumn of the @ManyToMany annotation of field \""+feildChosen.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +arrayType.getName() );
									
								}
							}
							// 2)  list case
						}else if(field.getType().equals(List.class) ) {
							// check weather the generic type of the list type is annotated with Table or JoinTable 
							Class<?> referencedClass = (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];	
							if(!(referencedClass.isAnnotationPresent(JoinTable.class) || referencedClass.isAnnotationPresent(Table.class))) {
								throw new UncompatibleAnnotationException("Entity class " + clazz.getName() + " feild type " + referencedClass.getName()+
										" must be annotated either with @Table or @JoinTable ");
							}
							// check weather the referencedCloumn is not annotated with any relational annotation (not belong to the table of the database)
							if(!manyToMany.referencedColumn().equals("")) {
								Field feildReferd = referencedClass.getDeclaredField(manyToMany.referencedColumn());
								if((feildReferd.isAnnotationPresent(OneToMany.class) || feildReferd.isAnnotationPresent(ManyToOne.class) 
									    || feildReferd.isAnnotationPresent(ManyToMany.class))) {
									throw new JoinTableException("referencedColumn of the @ManyToMany annotation of field \""+feildReferd.getName()
											+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +referencedClass.getName() );
										
									}
							}
							// check weather the choosenCloumn is not annotated with any relational annotation (not belong to the table of the database)
							if(!manyToMany.chosenColumn().equals("")) {
								Field feildChosen = referencedClass.getDeclaredField(manyToMany.chosenColumn());
								if((feildChosen.isAnnotationPresent(OneToMany.class) ||
								    feildChosen.isAnnotationPresent(ManyToOne.class)  || feildChosen.isAnnotationPresent(ManyToMany.class) )) {
									throw new JoinTableException("chosenColumn of the @ManyToMany annotation of field \""+feildChosen.getName()
									+"\" cannot be annotated with @OneToMany, @ManyToMany or @ ManyToOne in Entity\"" +clazz.getName() );
									
								}
							}
						}
						// check weather there is another annotation annotating the same already annotated field with the ManyToMany
						if(field.getAnnotations().length > 1) {
							throw new UncompatibleAnnotationException("Entity class "+ clazz.getName()+" Feild " + field.getName()
							+ " cannot be annotated with other annotations because it already have been annotated with @ManyToMany");
						}
					}
					
					
				}
				
			}
			// there is no PrimaryKeyfild
			if(count == 0) {
				throw new PrimaryKeyException("Entity class "+ clazz.getName()+" should a have a primary key");
			// there is more than one PrimaryKey 
			}else if(count > 1) {
				throw new PrimaryKeyException("Entity class "+ clazz.getName()+" cannot have more than one primary key");
				
			}
		}
		
	}
	/*
	 * check weather the fields of the classes annotated with JoinTable is annotated correctly  
	 * */
	public static <T> void checkAnnotationFieldsRulesJoinTable(Class<? extends T> clazz) throws PrimaryKeyException, UncompatibleAnnotationException, JoinTableException {
		int count=0;
		int fkCount=0;
		// check weather the class is annotated with JoinTable annotation 
		if( clazz.isAnnotationPresent(JoinTable.class)) {
			// get all the fields of the classes and loop into them
			for(Field field : clazz.getDeclaredFields()) {
				// weather the field is annotated with the PrimaryKey in case yes increment the counter 
				if(field.isAnnotationPresent(PrimaryKey.class)) {
					count++;
				}
				// weather the field is annotated with the ForignerKey in case yes increment the counter 
				if(field.isAnnotationPresent(ForignerKey.class)) {
				
					fkCount++;
				}
				// check weather there is another annotation annotating the same already annotated field with the OneToMany or ManyToOne or ManyToMany
				if(field.isAnnotationPresent(OneToMany.class)) {
					if(field.getAnnotations().length > 1) {
						throw new UncompatibleAnnotationException("Entity class "+ clazz.getName()+" Feild " + field.getName()
						+ " cannot be annotated with other annotations because it already have been annotated with @OneToMany");
					}
				}else if(field.isAnnotationPresent(ManyToOne.class)) {
					if(field.getAnnotations().length > 1) {
						throw new UncompatibleAnnotationException("Entity class "+ clazz.getName()+" Feild " + field.getName()
						+ " cannot be annotated with other annotations because it already have been annotated with @ManyToOne");
					}
				}else if(field.isAnnotationPresent(ManyToMany.class)) {
					if(field.getAnnotations().length > 1) {
						throw new UncompatibleAnnotationException("Entity class "+ clazz.getName()+" Feild " + field.getName()
						+ " cannot be annotated with other annotations because it already have been annotated with @ManyToMany");
					}
				}
				
				
				
			}
			// there is no primary key -> throw an exception  
			if(count == 0) {
				throw new PrimaryKeyException("Join Table "+ clazz.getName()+" should a have a primary key");
			// there is more than one primary key -> throw an exception 	
			}else if(count > 1) {
				throw new PrimaryKeyException("Join Table "+ clazz.getName()+" cannot have more than one primary key");
			// there are less than two forigner key -> throw an exception 	
			}else if(fkCount < 2) {
				throw new JoinTableException("Join Table " + clazz.getName()+" must have at least two forigner keys refererncing the tables that"
						+ "the relations is based on");
			}
		}
		
	}
}
