package com.example.trying.controler;
import com.example.trying.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.trying.entity.*;
import java.util.List;




@RestController
@RequestMapping("/ratingApi")
public class RatingControler {


	 @Autowired
	 private RatingService ratingService;




	 @PostMapping
	 public int insertRating(@RequestBody Rating rating){
		 return ratingService.insertRating(rating);
	 }




	 @PutMapping
	 public int updateRating(@RequestBody Rating rating){
		 return ratingService.updateRating(rating);
	 }




	 @GetMapping
	 public List<Rating> getAllRatings(){
		 return ratingService.getAllRatings();
	 }




	 @GetMapping("/{id}")
	 public Rating getRatingById(@PathVariable("id") Integer id){
		 return ratingService.getRatingById(id);
	 }




	 @DeleteMapping("/{id}")
	 public int deleteRatingById(@PathVariable("id") Integer id){
		 return ratingService.deleteRatingById(id);
	 }
}
