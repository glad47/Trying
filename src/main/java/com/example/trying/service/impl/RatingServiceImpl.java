package com.example.trying.service.impl;
import com.example.trying.mapper.RatingMapper;
import com.example.trying.service.RatingService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.trying.entity.*;
import java.util.List;




@Service
public class RatingServiceImpl implements RatingService {


	 @Autowired
	 private RatingMapper ratingMapper;




	 @Override
	 public int insertRating(Rating rating){
		 return ratingMapper.insertRating(rating);
	 }




	 @Override
	 public int updateRating(Rating rating){
		 return ratingMapper.updateRating(rating);
	 }




	 @Override
	 public List<Rating> getAllRatings(){
		 return ratingMapper.getAllRatings();
	 }




	 @Override
	 public Rating getRatingById(Integer id){
		 return ratingMapper.getRatingById(id);
	 }




	 @Override
	 public int deleteRatingById(Integer id){
		 return ratingMapper.deleteRatingById(id);
	 }
}
