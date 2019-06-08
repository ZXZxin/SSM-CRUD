package com.zxin.crud.controller;


import com.zxin.crud.bean.Department;
import com.zxin.crud.service.DepartmentService;
import com.zxin.crud.utils.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class DepartmentController {

    @Autowired
    DepartmentService departmentService;

    @RequestMapping("depts")
    @ResponseBody
    public ResponseResult getDepts(){
        List<Department> depts = departmentService.getDepts();
        return ResponseResult.success().add("depts", depts);
    }
}
