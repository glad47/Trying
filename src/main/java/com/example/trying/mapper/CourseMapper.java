package com.example.trying.mapper;
import org.apache.ibatis.annotations.*;
import com.example.trying.entity.*;
import java.util.List;




@Mapper
public interface CourseMapper {


	public int insertCourse(Course course);


	public int updateCourse(Course course);


	public List<Course> getAllCourses();


	public Course getCourseById(Integer id);


	public int deleteCourseById(Integer id);
}
