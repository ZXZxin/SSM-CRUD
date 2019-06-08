package com.zxin.crud.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zxin.crud.bean.Employee;
import com.zxin.crud.service.EmployeeService;
import com.zxin.crud.utils.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @RequestMapping("emps")
    @ResponseBody //要保证 @ResponseBody正常工作， 需要导入jackson包，可以将PageInfo自动转换成json
    public ResponseResult getEmpsWithJson(@RequestParam(value = "pn", defaultValue = "1") Integer pn,
                                          Model model) {
        PageHelper.startPage(pn, 5);
        List<Employee> emps = employeeService.getAll();
        PageInfo page = new PageInfo(emps, 5);
        return ResponseResult.success().add("pageInfo", page);
    }

    /**
     * 这是一开始没有使用ajax请求的时的方法(在index.jsp页面直接转发到/emps)
     * 查询员工数据（分页查询）
     *
     * @return
     */
//     @RequestMapping("/emps") // 注释，使用新的ajax返回json的方法
    public String getEmps(
            @RequestParam(value = "pn", defaultValue = "1") Integer pn,
            Model model) {
        // 这不是一个分页查询；
        // 引入PageHelper分页插件
        // 在查询之前只需要调用，传入页码，以及每页的大小

        PageHelper.startPage(pn, 5);
        // startPage后面紧跟的这个查询就是一个分页查询
        List<Employee> emps = employeeService.getAll();

        // 使用pageInfo包装查询后的结果，只需要将pageInfo交给页面就行了。
        // 封装了详细的分页信息,包括有我们查询出来的数据，传入连续显示的页数
        PageInfo page = new PageInfo(emps, 5);
        model.addAttribute("pageInfo", page);
        return "list"; // 返回到 web-inf/view/下
    }

    /**
     * 员工保存
     * 1、支持JSR303校验 (@Valid校验)
     * 2、导入Hibernate-Validator
     *
     * @return
     */
    @PostMapping("/emp")
    @ResponseBody
    public ResponseResult saveEmp(@Valid Employee employee, BindingResult result) {
        if (result.hasErrors()) {
            //校验失败，应该返回失败，在模态框中显示校验失败的错误信息
            Map<String, Object> map = new HashMap<>();
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError fieldError : errors) {
                System.out.println("错误的字段名：" + fieldError.getField());
                System.out.println("错误信息：" + fieldError.getDefaultMessage());
                map.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            return ResponseResult.fail().add("errorFields", map);
        } else {
            employeeService.saveEmp(employee);
            return ResponseResult.success();
        }
    }

    /**
     * 检查用户名是否可用
     */
    @ResponseBody
    @RequestMapping("/checkuser")
    public ResponseResult checkuser(@RequestParam("empName") String empName) {
        //先判断用户名是否是合法的表达式;
        String regx = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\u2E80-\u9FFF]{2,5})";
        if (!empName.matches(regx)) {
            return ResponseResult.fail().add("va_msg", "用户名必须是6-16位数字和字母的组合或者2-5位中文");
        }
        //数据库用户名重复校验
        return employeeService.checkUser(empName) ? ResponseResult.success()
                : ResponseResult.fail().add("va_msg", "用户名不可用");
    }


    /**
     * 根据id查询员工
     */
    @GetMapping("/emp/{id}")
    @ResponseBody
    public ResponseResult getEmp(@PathVariable("id") Integer id) {

        Employee employee = employeeService.getEmp(id);
        return ResponseResult.success().add("emp", employee);
    }

    /**
     * 如果直接发送ajax=PUT形式的请求
     * 封装的数据
     * Employee
     * [empId=1014, empName=null, gender=null, email=null, dId=null]
     * 问题：
     * 请求体中有数据；
     * 但是Employee对象封装不上；
     * update tbl_emp  where emp_id = 1014;
     * 原因：
     * Tomcat：
     *      1、将请求体中的数据，封装一个map。
     *      2、request.getParameter("empName")就会从这个map中取值。
     *      3、SpringMVC封装POJO对象的时候。
     * 会把POJO中每个属性的值，request.getParamter("email");
     * AJAX发送PUT请求引发的血案：
     * PUT请求，请求体中的数据，request.getParameter("empName")拿不到
     * Tomcat一看是PUT不会封装请求体中的数据为map，只有POST形式的请求才封装请求体为map
     * org.apache.catalina.connector.Request--parseParameters() (3111);
     * protected String parseBodyMethods = "POST";
     * if( !getConnector().isParseBodyMethod(getMethod()) ) {
     *     success = true;
     *     return;
     * }
     * 解决方案；
     * 我们要能支持直接发送PUT之类的请求还要封装请求体中的数据
     *    1、 配置上HttpPutFormContentFilter；
     *    2、 他的作用；将请求体中的数据解析包装成一个map。
     *    3、 request被重新包装，request.getParameter()被重写，就会从自己封装的map中取数据
     */
    @ResponseBody
    @PutMapping("/emp/{empId}")
    public ResponseResult saveEmp(Employee employee, HttpServletRequest request) {
        System.out.println("请求体中的值：" + request.getParameter("gender"));
        System.out.println("将要更新的员工数据：" + employee);
        employeeService.updateEmp(employee);
        return ResponseResult.success();
    }


    /**
     * 单个批量二合一
     * 批量删除：1-2-3
     * 单个删除：1
     */
    @ResponseBody
    @DeleteMapping("/emp/{ids}")
    public ResponseResult deleteEmp(@PathVariable("ids")String ids){
        //批量删除
        if(ids.contains("-")){
            List<Integer> del_ids = new ArrayList<>();
            String[] str_ids = ids.split("-");
            //组装id的集合
            for (String string : str_ids) {
                del_ids.add(Integer.parseInt(string));
            }
            employeeService.deleteBatch(del_ids);
        }else{
            Integer id = Integer.parseInt(ids);
            employeeService.deleteEmp(id);
        }
        return ResponseResult.success();
    }


}
