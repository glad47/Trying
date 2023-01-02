package com.example.trying.service;
import com.example.trying.entity.*;
import java.util.List;




public interface CourseService {


	public int insertCourse(Course course);


	public int updateCourse(Course course);


	public List<Course> getAllCourses();


	public Course getCourseById(Integer id);


	public int deleteCourseById(Integer id);
}
