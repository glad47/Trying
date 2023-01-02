package com.example.trying.service.impl;
import com.example.trying.mapper.CommentMapper;
import com.example.trying.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.trying.entity.*;
import java.util.List;




@Service
public class CommentServiceImpl implements CommentService {


	 @Autowired
	 private CommentMapper commentMapper;




	 @Override
	 public int insertComment(Comment comment){
		 return commentMapper.insertComment(comment);
	 }




	 @Override
	 public int updateComment(Comment comment){
		 return commentMapper.updateComment(comment);
	 }




	 @Override
	 public List<Comment> getAllComments(){
		 return commentMapper.getAllComments();
	 }




	 @Override
	 public Comment getCommentById(Integer id){
		 return commentMapper.getCommentById(id);
	 }




	 @Override
	 public int deleteCommentById(Integer id){
		 return commentMapper.deleteCommentById(id);
	 }
}
