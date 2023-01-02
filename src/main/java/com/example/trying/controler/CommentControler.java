package com.example.trying.controler;
import com.example.trying.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.trying.entity.*;
import java.util.List;




@RestController
@RequestMapping("/commentApi")
public class CommentControler {


	 @Autowired
	 private CommentService commentService;




	 @PostMapping
	 public int insertComment(@RequestBody Comment comment){
		 return commentService.insertComment(comment);
	 }




	 @PutMapping
	 public int updateComment(@RequestBody Comment comment){
		 return commentService.updateComment(comment);
	 }




	 @GetMapping
	 public List<Comment> getAllComments(){
		 return commentService.getAllComments();
	 }




	 @GetMapping("/{id}")
	 public Comment getCommentById(@PathVariable("id") Integer id){
		 return commentService.getCommentById(id);
	 }




	 @DeleteMapping("/{id}")
	 public int deleteCommentById(@PathVariable("id") Integer id){
		 return commentService.deleteCommentById(id);
	 }
}
