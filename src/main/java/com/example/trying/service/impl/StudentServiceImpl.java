package com.example.trying.service.impl;
import com.example.trying.mapper.StudentMapper;
import com.example.trying.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.trying.entity.*;
import java.util.List;




@Service
public class StudentServiceImpl implements StudentService {


	 @Autowired
	 private StudentMapper studentMapper;




	 @Override
	 public int insertStudent(Student student){
		 return studentMapper.insertStudent(student);
	 }




	 @Override
	 public int updateStudent(Student student){
		 return studentMapper.updateStudent(student);
	 }




	 @Override
	 public List<Student> getAllStudents(){
		 return studentMapper.getAllStudents();
	 }




	 @Override
	 public Student getStudentById(Integer id){
		 return studentMapper.getStudentById(id);
	 }




	 @Override
	 public int deleteStudentById(Integer id){
		 return studentMapper.deleteStudentById(id);
	 }
}
