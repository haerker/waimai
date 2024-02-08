package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> getSetmealIdByDishIds(List<Long> ids);

    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteBatch(List<Long> setmealIds);
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getSetmealIdBySetmealIds(Long setmealId);

    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteById(Long setmealId);
}
