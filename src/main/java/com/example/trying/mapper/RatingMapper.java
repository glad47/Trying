package com.example.trying.mapper;
import org.apache.ibatis.annotations.*;
import com.example.trying.entity.*;
import java.util.List;




@Mapper
public interface RatingMapper {


	public int insertRating(Rating rating);


	public int updateRating(Rating rating);


	public List<Rating> getAllRatings();


	public Rating getRatingById(Integer id);


	public int deleteRatingById(Integer id);
}
