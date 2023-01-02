package com.example.trying.service;
import com.example.trying.entity.*;
import java.util.List;




public interface CommentService {


	public int insertComment(Comment comment);


	public int updateComment(Comment comment);


	public List<Comment> getAllComments();


	public Comment getCommentById(Integer id);


	public int deleteCommentById(Integer id);
}
