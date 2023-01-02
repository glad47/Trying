package com.example.trying.service;
import com.example.trying.entity.*;
import java.util.List;




public interface StudentService {


	public int insertStudent(Student student);


	public int updateStudent(Student student);


	public List<Student> getAllStudents();


	public Student getStudentById(Integer id);


	public int deleteStudentById(Integer id);
}
