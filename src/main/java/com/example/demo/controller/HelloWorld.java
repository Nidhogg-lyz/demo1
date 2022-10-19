package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.sql.*;
import java.util.List;

@RestController
public class HelloWorld {
    private int Expire_Time=300;
    @Autowired
    JdbcTemplate jdbcTemplate;
//    @Autowired
//    HttpSession session;
    @Autowired
    HttpServletRequest re;
    @RequestMapping(value = "/")
    public String hello(){
        try{
            HttpSession session=re.getSession(false);
            String name= session.getAttribute("name").toString();
            return "Welcome, "+name+"!";
        }
        catch (Exception e){
            return "Welcome, new student!";
        }
    }
    @RequestMapping("/setPassword")
    public String setPassword(@RequestBody Map<String,Object> js){
        try{
            HttpSession session=re.getSession(false);
            int id=(int) session.getAttribute("stu_id");
            String oldPW=js.get("oldPW").toString();
            String newPW=js.get("newPW").toString();
            System.out.println("旧密码: "+oldPW);
            System.out.println("新密码: "+newPW);
            String sql="UPDATE stu_account SET Password=? WHERE stu_id=? AND Password=?";
            try{
                jdbcTemplate.update(sql,new Object[]{newPW,id,oldPW},new int[]{Types.VARCHAR,Types.BIGINT,Types.VARCHAR});
                return "修改密码成功!"+"您的新密码为: "+newPW;
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
        Cookie[] cookies=re.getCookies();
        if(cookies!=null){
            for(Cookie c:cookies)
                System.out.println(c.getValue());
        }
        HttpSession session=re.getSession(false);
        try{
            String name= session.getAttribute("name").toString();
        }
        catch (Exception e){
            return "您尚未登录!";
        }
        String res="Hello, "+ session.getAttribute("name")+"!";
        String  gender=new String();
        if(!(Boolean) session.getAttribute("gender"))
            gender="男";
        else
            gender="女";
        return res+"\n您的个人信息如下: "+
                "\n学号: "+ session.getAttribute("student_no")+
                "\n性别: "+gender+
                "\n邀请码: "+ session.getAttribute("invitation_code");
    }

    @RequestMapping("/login")
    public String getjson(HttpServletResponse resp, @RequestBody account a){
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
                System.out.println(res);
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
                Cookie cookie1=new Cookie("sessionid",session.getId());
                Cookie cookie2=new Cookie("stu_id",session.getAttribute("stu_id").toString());
                resp.addCookie(cookie1);resp.addCookie(cookie2);
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
            HttpSession temp=re.getSession(false);
            if(temp!=null)
                temp.invalidate();
            return "用户名或密码错误!";
        }
    }

    @RequestMapping("getbuilding_list")
    public String getBuilding_list(){
        HttpSession session=re.getSession(false);
        try{
            session.getAttribute("name").toString();
        }
        catch (Exception e){
            return "您尚未登录!";
        }
        Boolean gender=(Boolean) session.getAttribute("gender");
        String sql="SELECT DISTINCT building_id FROM room WHERE gender=?";
        List<Integer> l=jdbcTemplate.queryForList(sql,new Object[]{gender},new int[]{Types.BOOLEAN},Integer.class);
        String res= session.getAttribute("name")+",您当前可选择的宿舍楼号为: \n";
        for(int id:l){
            res+=id+"号楼\n";
        }
        return res;
    }

    @RequestMapping("/getremain_beds")
    public String getRemain_beds(Integer building_no){
        HttpSession session=re.getSession(false);
        try{
            Boolean gender=(Boolean) session.getAttribute("gender");
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
