package com.example.trying.entity;

import java.sql.Blob;

import com.example.trying.Constant.ReferenceOptions;
import com.example.trying.annotation.AutoGenerate;
import com.example.trying.annotation.Database.AutoIncrement;
import com.example.trying.annotation.Database.Column;
import com.example.trying.annotation.Database.ForignerKey;
import com.example.trying.annotation.Database.Identity;
import com.example.trying.annotation.Database.ManyToMany;
import com.example.trying.annotation.Database.PrimaryKey;
import com.example.trying.annotation.Database.Table;
import com.example.trying.annotation.Database.Unsigned;

@AutoGenerate
@Table
public class Teacher {
	@PrimaryKey
	@Unsigned
    @Column(size= 8)
	@AutoIncrement
	private long id;
	
	@Column(size= 500)
	private String name;
	
	
	@ManyToMany(mapBy="firstTry")
	private Student[] students;
	
	
	@ForignerKey(referencedClass=Student.class, onUpdate=ReferenceOptions.CASCADE)
	private int student_id;
	
}
