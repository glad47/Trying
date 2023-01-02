package com.example.trying.service;
import com.example.trying.entity.*;
import java.util.List;




public interface RatingService {


	public int insertRating(Rating rating);


	public int updateRating(Rating rating);


	public List<Rating> getAllRatings();


	public Rating getRatingById(Integer id);


	public int deleteRatingById(Integer id);
}
