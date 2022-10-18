package com.example.demo.controller;

//import com.sun.org.apache.xpath.internal.operations.Bool;
import com.fasterxml.jackson.databind.util.JSONPObject;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.sql.*;
import java.util.List;

@RestController
public class HelloWorld {
    private int Expire_Time=300;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    HttpSession request;
    @RequestMapping(value = "/")
    public String hello(){
        try{
            String name=request.getAttribute("name").toString();
            return "Welcome, "+name+"!";
        }
        catch (Exception e){
            return "Welcome, new student!";
        }
    }
    @RequestMapping("/setPassword")
    public String setPassword(@RequestBody String password){
        try{
            int id=(int)request.getAttribute("stu_id");
            String sql="UPDATE stu_account SET Password=? WHERE stu_id=?";
            try{
                jdbcTemplate.update(sql,new Object[]{password,id},new int[]{Types.VARCHAR,Types.BIGINT});
                return "修改密码成功!"+"您的新密码为: "+password;
            }
            catch (Exception e){
                return "修改密码失败!\n原因为: "+e.toString();
            }
        }
        catch (Exception e){
            return "您尚未登录!";
        }
    }
    @RequestMapping("/getinfo")
    public String getinfo(){
        try{
            String name=request.getAttribute("name").toString();
        }
        catch (Exception e){
            return "您尚未登录!";
        }
        String res="Hello, "+request.getAttribute("name")+"!";
        String  gender=new String();
        if(!(Boolean)request.getAttribute("gender"))
            gender="男";
        else
            gender="女";
        return res+"\n您的个人信息如下: "+
                "\n学号: "+ request.getAttribute("student_no")+
                "\n性别: "+gender+
                "\n邀请码: "+request.getAttribute("invitation_code");
    }

    @RequestMapping("/loginjson")
    public String getjson(HttpServletRequest re,@RequestBody account a){
        if(a==null)
            return "用户名或密码不能为空!";
        String username;
        String password;

        try{
            username=a.getUsername();
            password=a.getPassword();
            System.out.println(username);
            System.out.println(password);
        }
        catch (Exception e){
            return "用户名或密码不能为空!";
        }
        String sql="SELECT stu_id FROM stu_account WHERE user_name=? AND Password=?";
        Object[] args=new Object[]{username,password};
        int[] argtpyes=new int[]{Types.VARCHAR,Types.VARCHAR};
        try{
            int id=jdbcTemplate.queryForObject(sql,args,argtpyes,int.class);//只能用于查询单个字段，且根据java基本类型封装该字段值

            sql="SELECT student_no,name,gender,invitation_code FROM student WHERE stu_id=?";
            try{
                args=new Object[]{id};
                argtpyes=new int[]{Types.BIGINT};
                Map<String,Object> m=jdbcTemplate.queryForMap(sql,args,argtpyes);//只能用于查询一行记录，map键值为数据库表中查询到的个字段名
                String res="登录成功!\n";
                HttpSession session=re.getSession();
                session.setMaxInactiveInterval(Expire_Time);//设置最大过期时长
                session.setAttribute("username",username);
                session.setAttribute("name",m.get("name"));
                session.setAttribute("stu_id",id);
                session.setAttribute("student_no",m.get("student_no"));
                session.setAttribute("gender",m.get("gender"));
                session.setAttribute("invitation_code",m.get("invitation_code"));
//                for (Map.Entry<String, Object> entry : m.entrySet()) {
//                    res+=(entry.getKey() + " : " + entry.getValue()+'\n');
//                }
                String g=new String();
                if(!(Boolean)m.get("gender"))
                    g="男";
                else
                    g="女";
                return res+"姓名: "+session.getAttribute("name")+
                        "\n学号: "+ session.getAttribute("student_no")+
                        "\n性别: "+g+
                        "\n邀请码: "+session.getAttribute("invitation_code");
                //return res;
            }
            catch (Exception e){
                return "您不具有选宿舍权限!";
            }
        }
        catch (Exception E){
            return "用户名或密码错误!";
        }
    }

    @RequestMapping("/login")
    public String getstudent(HttpServletRequest request, @RequestParam("username") String username, @RequestParam("password") String password){
        if(username==null||password==null)
            return "用户名或密码不能为空!";
        String sql="SELECT stu_id FROM stu_account WHERE user_name=? AND Password=?";
        Object[] args=new Object[]{username,password};
        int[] argtpyes=new int[]{Types.VARCHAR,Types.VARCHAR};
        try{
            int id=jdbcTemplate.queryForObject(sql,args,argtpyes,int.class);//只能用于查询单个字段，且根据java基本类型封装该字段值

            sql="SELECT student_no,name,gender,invitation_code FROM student WHERE stu_id=?";
            try{
                args=new Object[]{id};
                argtpyes=new int[]{Types.BIGINT};
                Map<String,Object> m=jdbcTemplate.queryForMap(sql,args,argtpyes);//只能用于查询一行记录，map键值为数据库表中查询到的个字段名
                String res="登录成功!\n";
                HttpSession session=request.getSession();
                session.setMaxInactiveInterval(Expire_Time);//设置最大过期时长
                session.setAttribute("username",username);
                session.setAttribute("name",m.get("name"));
                session.setAttribute("stu_id",id);
                session.setAttribute("student_no",m.get("student_no"));
                session.setAttribute("gender",m.get("gender"));
                session.setAttribute("invitation_code",m.get("invitation_code"));
//                for (Map.Entry<String, Object> entry : m.entrySet()) {
//                    res+=(entry.getKey() + " : " + entry.getValue()+'\n');
//                }
                String g=new String();
                if(!(Boolean)m.get("gender"))
                    g="男";
                else
                    g="女";
                return res+"姓名: "+session.getAttribute("name")+
                        "\n学号: "+ session.getAttribute("student_no")+
                        "\n性别: "+g+
                        "\n邀请码: "+session.getAttribute("invitation_code");
                //return res;
            }
            catch (Exception e){
                return "您不具有选宿舍权限!";
            }
        }
        catch (Exception E){
            return "用户名或密码错误!";
        }
    }
    @RequestMapping("getbuilding_list")
    public String getBuilding_list(){
        try{
            request.getAttribute("name").toString();
        }
        catch (Exception e){
            return "您尚未登录!";
        }
        Boolean gender=(Boolean)request.getAttribute("gender");
        String sql="SELECT DISTINCT building_id FROM room WHERE gender=?";
        List<Integer> l=jdbcTemplate.queryForList(sql,new Object[]{gender},new int[]{Types.BOOLEAN},Integer.class);
        String res=request.getAttribute("name")+",您当前可选择的宿舍楼号为: \n";
        for(int id:l){
            res+=id+"号楼\n";
        }
        return res;
    }

    @RequestMapping("/getremain_beds")
    public String getRemain_beds(Integer building_no){
        try{
            Boolean gender=(Boolean) request.getAttribute("gender");
            String g=new String();
            if(!gender)
                g="男";
            else
                g="女";
            String res="";
            try{
                String sql="SELECT SUM(remain_beds) FROM room WHERE gender=? AND building_id=?";
                if(building_no!=null){
                    int cnt=jdbcTemplate.queryForObject(sql,new Object[]{gender,building_no},new int[]{Types.BOOLEAN,Types.INTEGER},int.class);
                    return building_no+"号楼中"+g+"生剩余床位数量为: "+cnt;
                }
                String getbuildings="SELECT DISTINCT building_id FROM room";
                List<Integer> buildings=jdbcTemplate.queryForList(getbuildings,Integer.class);
                for(int no:buildings){
                    int cnt=jdbcTemplate.queryForObject(sql,new Object[]{gender,no},new int[]{Types.BOOLEAN,Types.INTEGER},int.class);
                    res+=no+"号楼中"+g+"生剩余床位数量为: "+cnt+'\n';
                }
                return res;
            }
            catch (Exception e){
                System.out.println(e);
                return "对应楼中无满足条件的床位!";
            }
        }
        catch (Exception e){
            return "您尚未登录!";
        }
    }

    @RequestMapping(value="/default")
    public List<String> test() {
        String sql = "SELECT * FROM room WHERE remain_beds>=1 AND gender=0";
        //ResultSet res=jdbcTemplate.query(sql);
        List<room> r = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(room.class));


        List<String> result=new ArrayList<String>();
        for(room t_r:r) {
            result.add(result.size(), t_r.toString());
        }

        return result;
    }

    @RequestMapping("/select")
    public List<Map<String, Object>> select(){
        String sql="SELECT * FROM room WHERE is_empty=1";
        return jdbcTemplate.queryForList(sql);
    }
    @RequestMapping("/add")
    public List<Map<String,Object>> insert(){
        String sql="INSERT INTO room values (?,?,?,?)";
        int block=13;
        int suite=3111;
        String sex="female";
        boolean is_empty=true;
        String res="SELECT * FROM room WHERE block=13";
        jdbcTemplate.update(sql,block,suite,sex,is_empty);
        return jdbcTemplate.queryForList(res);
    }

    @RequestMapping("/del")
    public List<String> delete(){
        String sql="DELETE FROM room WHERE block=?";
        int block=13;
        jdbcTemplate.update(sql,block);
        String get_all="SELECT * FROM room";
        List<room> res=jdbcTemplate.query(get_all,new BeanPropertyRowMapper<>(room.class));
        List<String> result=new ArrayList<>();
        for(room r:res)
            result.add(result.size(), r.toString());
        return result;
    }


//    ResultSet test(){
//        try{
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection con=DriverManager.getConnection(
//                    "jdbc:mysql://139.224.80.108:3306/dormitory","student","123456");
//            Statement stmt=con.createStatement();
//            ResultSet rs=stmt.executeQuery("SELECT * FROM room WHERE block=5");
//            con.close();
//            return rs;
//        }catch(Exception e){
//            System.out.println(e);
//            return null;
//        }
//    }
}
