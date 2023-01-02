package com.example.trying.mapper;
import org.apache.ibatis.annotations.*;
import com.example.trying.entity.*;
import java.util.List;




@Mapper
public interface TeacherMapper {


	public int insertTeacher(Teacher teacher);


	public int updateTeacher(Teacher teacher);


	public List<Teacher> getAllTeachers();


	public Teacher getTeacherById(Integer id);


	public int deleteTeacherById(Integer id);
}
