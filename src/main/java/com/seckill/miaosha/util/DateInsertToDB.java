package com.seckill.miaosha.util;


import com.seckill.miaosha.domain.MiaoshaUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateInsertToDB {
    		//插入数据库
            public static void main(String[] args)  throws  Exception{
                int count = 5000;
                List<MiaoshaUser> users = new ArrayList<MiaoshaUser>(count);
                //生成用户
                for(int i=0;i<count;i++) {
                    MiaoshaUser user = new MiaoshaUser();
                    user.setId(13000000000L+i);
                    user.setLoginCount(1);
                    user.setNickname("user"+i);
                    user.setRegisterDate(new Date());
                    user.setSalt("1a2b3c");
                    user.setPassword(Md5Util.inputPassToDBPass("123456", user.getSalt()));
                    users.add(user);
                }
                Connection conn = DBUtil.getConn();
                String sql = "insert into miaosha_user(login_count, nickname, register_date, salt, password, id)values(?,?,?,?,?,?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                for(int i=0;i<users.size();i++) {
                    MiaoshaUser user = users.get(i);
                    pstmt.setInt(1, user.getLoginCount());
                    pstmt.setString(2, user.getNickname());
                    pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
                    pstmt.setString(4, user.getSalt());
                    pstmt.setString(5, user.getPassword());
                    pstmt.setLong(6, user.getId());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                pstmt.close();
                conn.close();
                System.out.println("insert to db");
            }
}
