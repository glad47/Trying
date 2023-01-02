package com.example.trying.controler;
import com.example.trying.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.trying.entity.*;
import java.util.List;




@RestController
@RequestMapping("/studentApi")
public class StudentControler {


	 @Autowired
	 private StudentService studentService;




	 @PostMapping
	 public int insertStudent(@RequestBody Student student){
		 return studentService.insertStudent(student);
	 }




	 @PutMapping
	 public int updateStudent(@RequestBody Student student){
		 return studentService.updateStudent(student);
	 }




	 @GetMapping
	 public List<Student> getAllStudents(){
		 return studentService.getAllStudents();
	 }




	 @GetMapping("/{id}")
	 public Student getStudentById(@PathVariable("id") Integer id){
		 return studentService.getStudentById(id);
	 }




	 @DeleteMapping("/{id}")
	 public int deleteStudentById(@PathVariable("id") Integer id){
		 return studentService.deleteStudentById(id);
	 }
}
