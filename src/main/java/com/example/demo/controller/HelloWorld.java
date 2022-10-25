package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.sql.*;
import java.util.List;

@RestController
public class HelloWorld {
    private int Expire_Time=300;

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    HttpServletRequest re;
    private String getToken(String id,Map<String,Object> m){
        String token=tokenGenerator.sign(id,m);
        return "token: "+token;
    }

    @RequestMapping(value = "/")
    public String Hello(){
        try{
            String token=re.getHeader("token");
            String name=tokenGenerator.getName(token);
            return "Welcome, "+name+"!";
        }
        catch (Exception e){
            return "Welcome, new student!";
        }
    }

    @RequestMapping("/login")
    public String Login(HttpServletResponse resp, @RequestBody Account a){
        String username;
        String password;
        try{
            username=a.getUsername();
            password=a.getPassword();
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

                //Use Token
                String token=getToken(""+id,m);
                resp.addHeader("token",token);
                return res+m.get("name").toString();
            }
            catch (Exception e){
                System.out.println(e.toString());
                return "您不具有选宿舍权限!";
            }
        }
        catch (Exception E){
            return "用户名或密码错误!";
        }
    }

    @RequestMapping("/setPassword")
    public String setPassword(@RequestBody Map<String,Object> js){
        try{
            String token=re.getHeader("token");
            int id=Integer.parseInt(tokenGenerator.getId(token));
            String oldPW=js.get("oldPW").toString();
            String newPW=js.get("newPW").toString();
            System.out.println("旧密码: "+oldPW);
            System.out.println("新密码: "+newPW);
            String sql="UPDATE stu_account SET Password=? WHERE stu_id=? AND Password=?";
            try{
                if(oldPW==newPW)
                    throw new Exception("新旧密码不能相同!");
                int state=jdbcTemplate.update(sql,new Object[]{newPW,id,oldPW},new int[]{Types.VARCHAR,Types.BIGINT,Types.VARCHAR});
                if(state==0)
                    throw new Exception("无法找到对应用户,修改失败");
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
    @RequestMapping("/order")
    public String getOrder(@RequestBody Map<String,Object> js){
        int state=0;
        Order o=new Order();
        try{
            o.setMembers(js.get("members").toString());
            o.setGender((Boolean) js.get("gender"));
            o.setBuilding_id((Integer)js.get("building_id"));
            if(js.get("invitation_code")==null)
                throw new Exception("无邀请码输入!");
        }
        catch(Exception e){
            System.out.println(e);
            return "传入参数错误!";
        }
        String []invitation_code=js.get("invitation_code").toString().split("\n");
        String []members=o.getMembers().split("\n");
        String sql="INSERT INTO order_list values (null,?,?,?,?,?)";
        int []ids=new int[members.length];
        if(members.length>5)
            return "队人数过多!";
        if(members.length!=invitation_code.length)
            return "成员与邀请码数量不符!";
        try{
            for(int i=0;i<members.length;++i){
                String m=members[i];
                String c=invitation_code[i];
                String check="SELECT stu_id FROM student WHERE student_no=? AND invitation_code=?";
                int id=jdbcTemplate.queryForObject(check,new Object[]{m,c},new int[]{Types.VARCHAR,Types.VARCHAR},int.class);
                String checkgender="SELECT gender FROM student WHERE stu_id=?";
                boolean g=jdbcTemplate.queryForObject(checkgender,new Object[]{id},new int[]{Types.BIGINT},boolean.class);
                if(g^o.isGender()){
                    throw new Exception("性别不符!");
                }
                String checks="SELECT room_id FROM allocation WHERE stu_id=?";
                String room_id;
                try{
                    room_id=jdbcTemplate.queryForObject(checks,new Object[]{id},new int[]{Types.BIGINT},String.class);
                }
                catch (EmptyResultDataAccessException e){
                    room_id=null;
                }
                if(room_id!=null)
                    throw new Exception(id+"学生已被分配宿舍");
                ids[i]=id;
            }
            o.setTeam_size(ids.length);
            System.out.println("team_size: "+o.getTeam_size());
            for(int i:ids)
                System.out.println(i);
            try{
                String getroom="SELECT * FROM room WHERE building_id=? AND gender=? AND remain_beds>=?";
                List<Room> rooms=jdbcTemplate.query(getroom,new Object[]{o.getBuilding_id(),o.isGender(),o.getTeam_size()},
                        new int[]{Types.INTEGER,Types.BOOLEAN,Types.TINYINT},new BeanPropertyRowMapper<>(Room.class));
                Room target=rooms.get(0);

                System.out.println("room_id: "+target.getRoom_id()+" room_size: "+target.getRemain_beds());

                String up="UPDATE room SET remain_beds=? WHERE room_id=?";
                int newrb=target.getRemain_beds()-o.getTeam_size();
                System.out.println("new beds: "+newrb);

                jdbcTemplate.update(up,new Object[]{newrb,target.getRoom_id()},new int[]{Types.TINYINT,Types.VARCHAR});
                String inst="INSERT INTO allocation VALUES (?,?)";
                for(int i:ids){
                    jdbcTemplate.update(inst,new Object[]{i,target.getRoom_id()},new int[]{Types.BIGINT,Types.VARCHAR});
                }
                state=0;
                o.setState(state);
            }
            catch(Exception e){
                System.err.println(e);
                System.err.println("无满足要求的房间!");
                state=1;
                o.setState(state);
            }
        }
        catch(EmptyResultDataAccessException e){
            System.err.println(e);
            System.err.println("成员邀请码错误!");
            state=-1;
            o.setState(state);
        }
        catch (Exception e){
            state=-1;
            o.setState(state);
            System.err.println(e);
        }
        finally {
            int res= jdbcTemplate.update(sql,new Object[]{o.getTeam_size(),o.getMembers(),o.getBuilding_id(),o.isGender(),o.getState()},
                    new int[]{Types.TINYINT,Types.VARCHAR,Types.TINYINT,Types.BOOLEAN,Types.TINYINT});
            return "订单状态为: "+o.getState();
        }
    }
    @RequestMapping("/getinfo")
    public String getinfo(){
        String token=re.getHeader("token");
        System.out.println("token:"+token);

        String res="Hello, "+ tokenGenerator.getName(token)+"!";
        String  gender=new String();
        if(!(Boolean) tokenGenerator.getGender(token))
            gender="男";
        else
            gender="女";
        return res+"\n您的个人信息如下: "+
                "\n学号: "+ tokenGenerator.getStu_no(token)+
                "\n性别: "+gender+
                "\n邀请码: "+ tokenGenerator.getCode(token);
    }

    @RequestMapping("getbuilding_list")
    public String getBuilding_list(){
        String token=re.getHeader("token");
        Boolean gender=tokenGenerator.getGender(token);
        String sql="SELECT DISTINCT building_id FROM room WHERE gender=?";
        List<Integer> l=jdbcTemplate.queryForList(sql,new Object[]{gender},new int[]{Types.BOOLEAN},Integer.class);//用于查询列表(多个匹配)
        String res= tokenGenerator.getName(token)+",您当前可选择的宿舍楼号为: \n";
        for(int id:l){
            res+=id+"号楼\n";
        }
        return res;
    }

    @RequestMapping("/getremain_beds")
    public String getRemain_beds(Integer building_no){
        String token=re.getHeader("token");
        Boolean gender=tokenGenerator.getGender(token);
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
    @Deprecated
    @RequestMapping(value="/default")
    public List<String> test() {
        String sql = "SELECT * FROM room WHERE remain_beds>=1 AND gender=0";
        List<Room> r = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Room.class));//返回自定义对象列表
        List<String> result=new ArrayList<String>();
        for(Room t_r:r) {
            result.add(result.size(), t_r.toString());
        }
        return result;
    }
    @Deprecated
    @RequestMapping("/add")
    public List<Map<String,Object>> insert(){
        String sql="INSERT INTO room values (?,?,?,?)";
        int building_id=13;
        int room_id=3111;
        boolean gender=true;
        int remain_beds=2;
        String res="SELECT * FROM room WHERE block=13";
        jdbcTemplate.update(sql,room_id,building_id,remain_beds,gender);
        return jdbcTemplate.queryForList(res);
    }
    @Deprecated
    @RequestMapping("/del")
    public List<String> delete(){
        String get_all="SELECT * FROM room";
        List<Room> res=jdbcTemplate.query(get_all,new BeanPropertyRowMapper<>(Room.class));
        List<String> result=new ArrayList<>();
        for(Room r:res)
            result.add(result.size(), r.toString());
        return result;
    }

}
