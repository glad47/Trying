package com.example.trying.entity;

import java.sql.Date;
import java.sql.Timestamp;

import com.example.trying.annotation.AutoGenerate;
import com.example.trying.annotation.Database.AutoIncrement;
import com.example.trying.annotation.Database.Column;
import com.example.trying.annotation.Database.NotNull;
import com.example.trying.annotation.Database.OneToMany;
import com.example.trying.annotation.Database.Primary;
import com.example.trying.annotation.Database.Table;

@AutoGenerate
@Table(name="Student")
public class Student {
	@Primary
	@AutoIncrement
	private int id;
	@Column(name="StudentName", size= 10)
	private String name;
	@NotNull
	private Date dateOfBrith;
	private Timestamp updateTime;
	private byte[] image;
	@OneToMany(targetClass=Teacher.class)
	private Teacher[] teachers;
	

}
