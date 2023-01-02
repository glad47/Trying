package com.example.trying.controler;
import com.example.trying.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.trying.entity.*;
import java.util.List;




@RestController
@RequestMapping("/courseApi")
public class CourseControler {


	 @Autowired
	 private CourseService courseService;




	 @PostMapping
	 public int insertCourse(@RequestBody Course course){
		 return courseService.insertCourse(course);
	 }




	 @PutMapping
	 public int updateCourse(@RequestBody Course course){
		 return courseService.updateCourse(course);
	 }




	 @GetMapping
	 public List<Course> getAllCourses(){
		 return courseService.getAllCourses();
	 }




	 @GetMapping("/{id}")
	 public Course getCourseById(@PathVariable("id") Integer id){
		 return courseService.getCourseById(id);
	 }




	 @DeleteMapping("/{id}")
	 public int deleteCourseById(@PathVariable("id") Integer id){
		 return courseService.deleteCourseById(id);
	 }
}
