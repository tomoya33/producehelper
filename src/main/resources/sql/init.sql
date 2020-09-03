-- 供应商商品表
CREATE TABLE `b_supplier_goods_temp_init` (
    `pkno` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `mtime` datetime DEFAULT NULL COMMENT '修改时间',
    `cuser` varchar(20) NOT NULL COMMENT '创建人',
    `muser` varchar(20) DEFAULT NULL COMMENT '修改人',
    `supplier_id` varchar(20) NOT NULL COMMENT '供应商编码',
    `goods_id` varchar(50) NOT NULL COMMENT '商品编码',
    `goods_bar_code` varchar(50) NOT NULL COMMENT '商品条形码',
    `goods_order_price` decimal(15,4) NOT NULL COMMENT '订货价格',
    `min_order_count` int(11) DEFAULT NULL COMMENT '最小起订量',
    `delivery_type` varchar(20) DEFAULT NULL COMMENT '配送方式',
    `active_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    `append_file_url` varchar(300) DEFAULT NULL COMMENT '上传附件的地址，供应商送货的文档，excel文件或其他',
    `purchase_id` varchar(20) DEFAULT NULL COMMENT '采购方编码',
    `purchase_name` varchar(255) DEFAULT NULL COMMENT '采购方名称',
    `spec` int(11) DEFAULT NULL COMMENT '叫货规格',
    `delivery_day` int(11) DEFAULT NULL COMMENT '配送周期(天)',
    `goods_sale_price` decimal(9,2) DEFAULT NULL COMMENT '建议零售价',
    PRIMARY KEY (`pkno`) USING BTREE,
    KEY `index_1` (`goods_id`),
    KEY `index_2` (`supplier_id`),
    KEY `index_3` (`purchase_id`),
    KEY `index_5` (`goods_id`,`purchase_id`) USING BTREE,
    KEY `index_4` (`goods_id`,`supplier_id`,`purchase_id`,`active_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商商品信息表';

-- 插入供应商商品数据

--
-- 删除临时表
-- DROP TABLE IF EXISTS b_supplier_goods_temp_init;

-- 删除定时器
-- DROP EVENT IF EXISTS goods_stock_daily_event;