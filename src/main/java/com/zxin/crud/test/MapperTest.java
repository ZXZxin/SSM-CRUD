package com.zxin.crud.test;

import com.zxin.crud.bean.Department;
import com.zxin.crud.bean.Employee;
import com.zxin.crud.dao.DepartmentMapper;
import com.zxin.crud.dao.EmployeeMapper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-config.xml"})
public class MapperTest {

    @Autowired
    DepartmentMapper departmentMapper;

    @Autowired
    EmployeeMapper employeeMapper;

    @Autowired
    SqlSession sqlSession;

    /**
     * 测试DepartmentMapper
     */
    @Test
    public void testCRUD() {

	/*	//1、创建SpringIOC容器
		ApplicationContext ioc = new ClassPathXmlApplicationContext("spring-config.xml");
		//2、从容器中获取mapper
		DepartmentMapper bean = ioc.getBean(DepartmentMapper.class);*/
        System.out.println(departmentMapper);

        //1、插入几个部门
		departmentMapper.insertSelective(new Department(null, "开发部"));
		departmentMapper.insertSelective(new Department(null, "测试部"));

        //2、生成员工数据，测试员工插入
//        System.out.println(employeeMapper);
//        employeeMapper.insertSelective(new Employee(null, "Jerry", "M", "Jerry@zxin.com", 1));
    }

    @Test
    public void testBatchCRUD(){
        //3、批量插入多个员工；批量，使用可以执行批量操作的sqlSession。(需要在spring-conf.xml中配置)
        EmployeeMapper batchMapper = sqlSession.getMapper(EmployeeMapper.class);
        for (int i = 0; i < 20; i++) {
            String uid = UUID.randomUUID().toString().substring(0, 5) + i;
            batchMapper.insertSelective(new Employee(null, uid, "M", uid + "@zxin.com", 1));
        }
        System.out.println("批量完成");
    }

}
