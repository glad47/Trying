package com.example.trying.entity;

import java.sql.Blob;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import com.example.trying.Constant.ReferenceOptions;
import com.example.trying.annotation.AutoGenerate;
import com.example.trying.annotation.Database.AutoIncrement;

import com.example.trying.annotation.Database.Column;
import com.example.trying.annotation.Database.DefaultTimeStamp;
import com.example.trying.annotation.Database.ForignerKey;
import com.example.trying.annotation.Database.ForignerKeyPart;
import com.example.trying.annotation.Database.IgnoreField;
import com.example.trying.annotation.Database.ManyToMany;
import com.example.trying.annotation.Database.ManyToOne;
import com.example.trying.annotation.Database.NotNull;
import com.example.trying.annotation.Database.OneToMany;
import com.example.trying.annotation.Database.PrimaryKey;
import com.example.trying.annotation.Database.PrimaryKeyPart;
import com.example.trying.annotation.Database.Table;
import com.example.trying.annotation.Database.Unique;
import com.example.trying.annotation.Database.Unsigned;

@AutoGenerate
@Table(name="Student")
public class Student {
	@PrimaryKey
	@AutoIncrement
	@Unsigned
	private Integer id;
	@PrimaryKeyPart
	@Column(name="StudentName", size= 100)
	private String name;
	@Unique
	@Unsigned
	@NotNull
	private Date dateOfBrith;
	@DefaultTimeStamp
	private Timestamp updateTime;
	@Column(size=555 )
	private Blob image;
	
	@ForignerKey(referencedClass=Teacher.class, onUpdate=ReferenceOptions.CASCADE)
	private long teacher_id;
	
	
	
//	@ForignerKey(referencedClass=Course.class, referencedColumn="id", onUpdate=ReferenceOptions.CASCADE, onDelete=ReferenceOptions.RESTRICT)
//	private Integer courseId;
	
	@OneToMany
	private List<Course> course;
	
	
	@OneToMany
	private List<Rating> rating;
	
	@ManyToMany(id="firstTry")
	private List<Teacher> teachers;
	
	
	
//	@OneToMany(targetClass=Teacher.class)
//	private Teacher[] teachers;
	

}
