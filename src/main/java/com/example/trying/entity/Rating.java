package com.example.trying.entity;

import java.util.List;

import com.example.trying.annotation.AutoGenerate;
import com.example.trying.annotation.Database.ForignerKey;
import com.example.trying.annotation.Database.JoinTable;
import com.example.trying.annotation.Database.OneToMany;
import com.example.trying.annotation.Database.PrimaryKey;

@JoinTable
@AutoGenerate
public class Rating {
	@PrimaryKey
    private int id;
	@ForignerKey(referencedClass = Student.class)
	private int student_id;
	
	@ForignerKey(referencedClass = Teacher.class)
	private long teacher_id;
	
	
	
	private String name;
	
	
	@OneToMany
	private List<Course> courses;
	
	@OneToMany
	private List<Comment> comment;

}
