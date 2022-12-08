package com.example.trying.entity;

import com.example.trying.annotation.AutoGenerate;
import com.example.trying.annotation.Database.ForignerKey;
import com.example.trying.annotation.Database.JoinTable;
import com.example.trying.annotation.Database.PrimaryKey;

@JoinTable
@AutoGenerate
public class Comment {
	
	
	@PrimaryKey
	private int id;
	
	
	@ForignerKey(referencedClass = Student.class)
	private int studentId;
	
	@ForignerKey(referencedClass = Rating.class)
	private int ratingId;

}
