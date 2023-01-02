package com.example.trying.service;
import com.example.trying.entity.*;
import java.util.List;




public interface TeacherService {


	public int insertTeacher(Teacher teacher);


	public int updateTeacher(Teacher teacher);


	public List<Teacher> getAllTeachers();


	public Teacher getTeacherById(Integer id);


	public int deleteTeacherById(Integer id);
}
