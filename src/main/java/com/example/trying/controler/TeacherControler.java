package com.example.trying.controler;
import com.example.trying.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.trying.entity.*;
import java.util.List;




@RestController
@RequestMapping("/teacherApi")
public class TeacherControler {


	 @Autowired
	 private TeacherService teacherService;




	 @PostMapping
	 public int insertTeacher(@RequestBody Teacher teacher){
		 return teacherService.insertTeacher(teacher);
	 }




	 @PutMapping
	 public int updateTeacher(@RequestBody Teacher teacher){
		 return teacherService.updateTeacher(teacher);
	 }




	 @GetMapping
	 public List<Teacher> getAllTeachers(){
		 return teacherService.getAllTeachers();
	 }




	 @GetMapping("/{id}")
	 public Teacher getTeacherById(@PathVariable("id") Integer id){
		 return teacherService.getTeacherById(id);
	 }




	 @DeleteMapping("/{id}")
	 public int deleteTeacherById(@PathVariable("id") Integer id){
		 return teacherService.deleteTeacherById(id);
	 }
}
