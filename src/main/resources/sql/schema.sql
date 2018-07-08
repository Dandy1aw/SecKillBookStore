/*用户表*/
CREATE TABLE `miaosha_user` (
  `id` BIGINT(20) NOT NULL COMMENT '用户id,手机号码',
  `nickname` VARCHAR(255) NOT NULL COMMENT '昵称',
  `password` VARCHAR(32) DEFAULT NULL COMMENT 'MD5(pass明文+固定salt)+salt',
  `salt` VARCHAR(10) DEFAULT NULL COMMENT '盐值',
  `head` VARCHAR(128) DEFAULT NULL COMMENT '头像，云存储ID',
  `register_date` DATETIME DEFAULT NULL  COMMENT '注册时间',
  `last_login_time` DATETIME DEFAULT NULL  COMMENT '上次登录时间',
  `login_count` INT(11) DEFAULT 0 COMMENT '登录次数',
  PRIMARY KEY (`id`)
)ENGINE =innodb DEFAULT CHARSET =utf8mb4;

/*商品表*/
CREATE  TABLE `good`(
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `goods_name` varchar(16) DEFAULT  NULL  COMMENT '商品名称',
  `goods_title` VARCHAR(64) DEFAULT NULL COMMENT '商品标题',
  `goods_img` VARCHAR(64)  DEFAULT NULL COMMENT '商品的图片',
  `goods_detail` LONGTEXT COMMENT '商品详情',
  `goods_price` DECIMAL(10,2) DEFAULT '0.00' COMMENT '商品单价',
  `goods_stock` INT(11) DEFAULT 0 COMMENT '商品库存  -1表示 没有限制',
  PRIMARY KEY (`id`)
)ENGINE = innodb AUTO_INCREMENT=3  DEFAULT CHARSET=utf8mb4;



INSERT INTO `good` VALUES (1,'iphonex','苹果手机','/img/iphonex.png','iphonx 苹果手机',8700.00,100);
INSERT INTO `good` VALUES (2,'华为mate9','华为手机','/img/mate.png','华为手机 华为mate9',3400.00,-1);

/*秒杀商品表*/

CREATE TABLE `miaosha_goods`(
  `id` BIGINT(20) NOT NULL  AUTO_INCREMENT COMMENT '秒杀商品id',
  `goods_id` BIGINT(20) DEFAULT NULL COMMENT '商品id',
  `miaosha_price` DECIMAL(10,2) DEFAULT '0.00' COMMENT '秒杀价',
  `stock_count` INT(11) DEFAULT NULL COMMENT '库存数量',
  `start_date` DATETIME DEFAULT NULL COMMENT '秒杀开始时间',
  `end_date` DATETIME DEFAULT NULL  COMMENT '秒杀结束时间',
  PRIMARY KEY (`id`)
)ENGINE =innodb AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;


/*订单表*/
CREATE TABLE `order_info`(
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户id',
  `goods_id` BIGINT(20) DEFAULT NULL COMMENT '商品id',
  `delivery_addr_id` BIGINT(20) DEFAULT NULL COMMENT '收货地址',
  `goods_name` VARCHAR(16) DEFAULT NULL  COMMENT '冗余多来的商品名称',
  `goods_count` INT(11) DEFAULT '0' COMMENT '商品数量',
  `goods_price` DECIMAL(10,2) DEFAULT '0.00' COMMENT '商品单价',
  `order_channel` TINYINT(4) DEFAULT 0 COMMENT '1pc 2 android 3ios',
  `status` TINYINT(4) DEFAULT 0 COMMENT '订单状态 0 新建未支付 1 已支付 2 已发货 3 已收货 4 已退款 5 已完成',
  `create_date` DATETIME DEFAULT NULL COMMENT '订单创建时间',
  `pay_date` DATETIME DEFAULT NULL  COMMENT '订单支付时间',
  PRIMARY KEY (`id`)
)ENGINE =innodb AUTO_INCREMENT=12 DEFAULT CHARSET =utf8mb4;

/*秒杀订单表*/
CREATE  TABLE `miaosha_order` (
  `id` BIGINT(20) not null auto_increment,
  `user_id` BIGINT(20) DEFAULT NULL ,
  `order_id` BIGINT(20) DEFAULT NULL ,
  `goods_id` BIGINT(20) DEFAULT NULL ,
  PRIMARY KEY (`id`)
)ENGINE =innodb AUTO_INCREMENT=3 DEFAULT CHARSET =utf8mb4;
















