package com.seckill.miaosha.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seckill.miaosha.domain.MiaoshaUser;
import com.seckill.miaosha.util.Md5Util;

/** 用户 工具类 用于生成压测 用户对象数据
 * 1. 连接数据库 将生成5000个用户 密码都为 123456 的用户 插入数据库，盐值都为固定
 * 2.调用do_logon 接口模仿 登陆功能 生成对应5000个用户的token 也就是（cookie） 用于访问 对应秒杀页面 作为post 数据
 * */
public class UserUtil {

    private static void createUser(int count) throws Exception{
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
        System.out.println("create user");
//		//插入数据库
//		Connection conn = DBUtil.getConn();
//		String sql = "insert into miaosha_user(login_count, nickname, register_date, salt, password, id)values(?,?,?,?,?,?)";
//		PreparedStatement pstmt = conn.prepareStatement(sql);
//		for(int i=0;i<users.size();i++) {
//			MiaoshaUser user = users.get(i);
//			pstmt.setInt(1, user.getLoginCount());
//			pstmt.setString(2, user.getNickname());
//			pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
//			pstmt.setString(4, user.getSalt());
//			pstmt.setString(5, user.getPassword());
//			pstmt.setLong(6, user.getId());
//			pstmt.addBatch();
//		}
//		pstmt.executeBatch();
//		pstmt.close();
//		conn.close();
//		System.out.println("insert to db");
        //登录，生成token
        /*创建 需要 访问的 url String 对象 */
        String urlString = "http://localhost:8080/login/do_Login";
        //建立 要写入的文件的对象
        File file = new File("D:/tokens.txt");
        if(file.exists()) {
            file.delete(); //如果文件存在文件就删掉。
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw"); //以读写模式 创建文件访问对象
        file.createNewFile();
        raf.seek(0);//文件对象 指针 放在文件开头
        for(int i=0;i<users.size();i++) {
            MiaoshaUser user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection)url.openConnection(); //创建 url 连接
            co.setRequestMethod("POST");//设置连接 请求方式
            co.setDoOutput(true);// 输出
            OutputStream out = co.getOutputStream();//取出 输出流 向输出流中写入 参数
            String params = "mobile="+user.getId()+"&password="+Md5Util.inputPassToFormPass("123456");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int len = 0;
            while((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0 ,len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            //返回是一个json 串
            JSONObject jo = JSON.parseObject(response);
            String token = jo.getString("data");//拿到 data key 的数据
            System.out.println("create token : " + user.getId());

            String row = user.getId()+","+token; //
            raf.seek(raf.length());//找到当前文件的 长度
            raf.write(row.getBytes());//将 userId 和 对应token 写入
            raf.write("\r\n".getBytes());
            System.out.println("write to file : " + user.getId());
        }
        raf.close();
        System.out.println("over");
    }

    public static void main(String[] args)throws Exception {
        createUser(5000);
    }
}
