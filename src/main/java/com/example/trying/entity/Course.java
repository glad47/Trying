package com.example.trying.entity;

import com.example.trying.annotation.AutoGenerate;
import com.example.trying.annotation.Database.Column;
import com.example.trying.annotation.Database.ManyToOne;
import com.example.trying.annotation.Database.OneToMany;
import com.example.trying.annotation.Database.PrimaryKey;
import com.example.trying.annotation.Database.Table;

@Table
@AutoGenerate
public class Course {
	@PrimaryKey
	@Column(name="courseId",size=20)
	private int id;
	@Column(name="courseNAmes",size= 500)
	private String name;
	
	
	@ManyToOne
	private Student student;
}
