package com.example.trying.mapper;
import org.apache.ibatis.annotations.*;
import com.example.trying.entity.*;
import java.util.List;




@Mapper
public interface StudentMapper {


	public int insertStudent(Student student);


	public int updateStudent(Student student);


	public List<Student> getAllStudents();


	public Student getStudentById(Integer id);


	public int deleteStudentById(Integer id);
}
