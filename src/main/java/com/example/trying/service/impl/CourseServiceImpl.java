package com.example.trying.service.impl;
import com.example.trying.mapper.CourseMapper;
import com.example.trying.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.trying.entity.*;
import java.util.List;




@Service
public class CourseServiceImpl implements CourseService {


	 @Autowired
	 private CourseMapper courseMapper;




	 @Override
	 public int insertCourse(Course course){
		 return courseMapper.insertCourse(course);
	 }




	 @Override
	 public int updateCourse(Course course){
		 return courseMapper.updateCourse(course);
	 }




	 @Override
	 public List<Course> getAllCourses(){
		 return courseMapper.getAllCourses();
	 }




	 @Override
	 public Course getCourseById(Integer id){
		 return courseMapper.getCourseById(id);
	 }




	 @Override
	 public int deleteCourseById(Integer id){
		 return courseMapper.deleteCourseById(id);
	 }
}
