package com.seckill.miaosha.util;

import java.util.UUID;

/**
 * Created by syw on 2018/6/23.
 */
public class UUIDUtill {
    public static String uuid(){
        /*原生的UUid中有横杠，这里去掉横杠*/
        return UUID.randomUUID().toString().replace("-","");
    }
}
