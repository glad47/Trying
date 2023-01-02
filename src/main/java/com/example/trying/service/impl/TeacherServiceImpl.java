package com.example.trying.service.impl;
import com.example.trying.mapper.TeacherMapper;
import com.example.trying.service.TeacherService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.trying.entity.*;
import java.util.List;




@Service
public class TeacherServiceImpl implements TeacherService {


	 @Autowired
	 private TeacherMapper teacherMapper;




	 @Override
	 public int insertTeacher(Teacher teacher){
		 return teacherMapper.insertTeacher(teacher);
	 }




	 @Override
	 public int updateTeacher(Teacher teacher){
		 return teacherMapper.updateTeacher(teacher);
	 }




	 @Override
	 public List<Teacher> getAllTeachers(){
		 return teacherMapper.getAllTeachers();
	 }




	 @Override
	 public Teacher getTeacherById(Integer id){
		 return teacherMapper.getTeacherById(id);
	 }




	 @Override
	 public int deleteTeacherById(Integer id){
		 return teacherMapper.deleteTeacherById(id);
	 }
}
