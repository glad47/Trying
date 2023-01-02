package com.example.trying.mapper;
import org.apache.ibatis.annotations.*;
import com.example.trying.entity.*;
import java.util.List;




@Mapper
public interface CommentMapper {


	public int insertComment(Comment comment);


	public int updateComment(Comment comment);


	public List<Comment> getAllComments();


	public Comment getCommentById(Integer id);


	public int deleteCommentById(Integer id);
}
