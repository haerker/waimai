package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    List<Integer> getSetmealsByIds(List<Long> ids);

    void deleteBatch(List<Long> ids);

    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    List<Setmeal> list(Setmeal setmeal);

    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
    Integer countByMap(Map map);
}
