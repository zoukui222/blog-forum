/*
 Navicat Premium Dump SQL

 Source Server         : 本地数据库
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : blog-xs

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 03/02/2026 15:08:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动名称',
  `descr` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动简介',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动内容',
  `start` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '开始时间',
  `end` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结束时间',
  `form` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动形式',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动地址',
  `host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主办方',
  `read_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity
-- ----------------------------
INSERT INTO `activity` VALUES (1, '测试活动信息', '这是一场酣畅淋漓的活动牙牙', '<p>真的不错了哦</p>', '2026-02-17', '2026-02-19', '线上', '北京路转盘', '拱墅家族', 34, 'http://localhost:9090/files/1770081214167-1720445280170-2.jpg');

-- ----------------------------
-- Table structure for activity_sign
-- ----------------------------
DROP TABLE IF EXISTS `activity_sign`;
CREATE TABLE `activity_sign`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `activity_id` int NOT NULL,
  `activity_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` int NOT NULL,
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sign_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity_sign
-- ----------------------------
INSERT INTO `activity_sign` VALUES (2, 1, '测试活动信息', 2, 'light', '2026-02-03 14:27:23');

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色标识',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', 'admin', '管理员', 'http://localhost:9090/files/1697438073596-avatar.png', 'ADMIN', '13677889922', 'admin@xm.com');

-- ----------------------------
-- Table structure for blog
-- ----------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `descr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简介',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签',
  `user_id` int NULL DEFAULT NULL COMMENT '发布人ID',
  `date` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布日期',
  `read_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `category_id` int NULL DEFAULT NULL COMMENT '分类ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '博客信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of blog
-- ----------------------------
INSERT INTO `blog` VALUES (11, '测试测试', '<p>成都市S 是</p>', '测试简介', 'http://localhost:9090/files/1770020204107-1720445645922-d1.png', '[\"大数据\",\"前端\"]', 1, '2026-02-02', 15, 6);
INSERT INTO `blog` VALUES (12, '解密ECharts甘特图：Vue项目中的高级数据可视化技巧', '<h2>vue实现甘特图效果</h2><h4><a></a><a></a>🏗️ 架构设计</h4><p>在开始编码之前，我们需要先设计好整体架构。一个好的甘特图组件应该包含以下几个核心部分：</p><h5><a></a>1. 数据层设计</h5><p>首先定义清晰的<a href=\"https://so.csdn.net/so/search?q=%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84&amp;spm=1001.2101.3001.7020\" target=\"_blank\">数据结构</a>，这是整个组件的基础：</p><p>// 一级任务数据结构（父任务）<br/>parentTask: {<br/>  taskId: String,           // 任务ID<br/>  taskName: String,         // 任务名称<br/>  projectId: String,        // 所属项目ID<br/>  projectName: String,      // 所属项目名称<br/>  createTimeMills: Number,  // 创建时间戳<br/>  deadlineMills: Number,    // 截止时间戳<br/>  status: Number,           // 任务状态 (1:进行中, 2:已完成, 3:已取消)<br/>  progress: Number,         // 整体进度百分比<br/>  isParent: true,           // 标识为父任务<br/>  children: Array&lt;Task&gt;,    // 子任务列表<br/>  expanded: Boolean         // 是否展开显示子任务<br/>}<br/><br/>// 二级任务数据结构（子任务）<br/>childTask: {<br/>  taskId: String,           // 任务ID<br/>  taskName: String,         // 任务名称<br/>  parentTaskId: String,     // 父任务ID<br/>  documentName: String,     // 文档名称<br/>  createTimeMills: Number,  // 创建时间戳<br/>  deadlineMills: Number,    // 截止时间戳<br/>  status: Number,           // 任务状态 (1:进行中, 2:已完成, 3:已取消)<br/>  stageName: String,        // 阶段名称<br/>  progress: Number,         // 进度百分比<br/>  assignee: String,         // 负责人<br/>  isParent: false,          // 标识为子任务<br/>  level: 2                  // 任务层级<br/>}<br/><br/>// 层级任务树结构<br/>taskTree: {<br/>  projectId: String,<br/>  projectName: String,<br/>  tasks: Array&lt;ParentTask&gt;  // 一级任务列表（包含子任务）<br/>}<br/></p><p>2. 时间轴算法<br/>时间轴是甘特图的核心，我们需要实现灵活的时间范围计算：<br/><br/>🕐 多时间周期支持<br/>甘特图支持三种时间周期，满足不同的查看需求（也可自行修改为时间范围）：<br/><br/>本周视图: 从周一到周日，适合查看短期任务<br/>本月视图: 从月初到月末，适合月度规划<br/>本季视图: 从季度初到季度末，适合长期项目管理<br/></p>', '解密ECharts甘特图：Vue项目中的高级数据可视化技巧\n在项目管理、生产调度等需要直观展示时间进度的场景中，甘特图（Gantt Chart）凭借其清晰的视觉呈现能力成为不可或缺的工具。本文将深入探讨如何在Vue项目中利用ECharts实现高度定制化的甘特图，从基础搭建到高级功能实现，为开发者提供一套完整的解决方案。', 'http://localhost:9090/files/1770100594803-image1.png', '[\"后端\",\"Java\",\"Vue\",\"前端\"]', 1, '2026-02-03', 14, 2);
INSERT INTO `blog` VALUES (13, '111', '<p>dsdACDSsD撒CC都是第三次</p>', '2121', 'http://localhost:9090/files/1770101338983-1769389921217_.jpg', '[\"后端\",\"Java\",\"面试\"]', 1, '2026-02-03', 1, 1);

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '博客分类' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '后端');
INSERT INTO `category` VALUES (2, '前端');
INSERT INTO `category` VALUES (4, '大数据');
INSERT INTO `category` VALUES (5, '代码人生');
INSERT INTO `category` VALUES (6, '算法');
INSERT INTO `category` VALUES (7, '人工智能');
INSERT INTO `category` VALUES (8, '开发工具');
INSERT INTO `category` VALUES (9, '职场经验');
INSERT INTO `category` VALUES (10, '面试');

-- ----------------------------
-- Table structure for collect
-- ----------------------------
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `fid` int NULL DEFAULT NULL COMMENT '关联ID',
  `user_id` int NULL DEFAULT NULL COMMENT '点赞人ID',
  `module` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of collect
-- ----------------------------
INSERT INTO `collect` VALUES (24, 11, 2, '博客');
INSERT INTO `collect` VALUES (26, 1, 2, '活动');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '内容',
  `user_id` int NULL DEFAULT NULL COMMENT '评论人',
  `pid` int NULL DEFAULT NULL COMMENT '父级ID',
  `root_id` int NULL DEFAULT NULL COMMENT '根节点ID',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评论时间',
  `fid` int NULL DEFAULT NULL COMMENT '关联ID',
  `module` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (50, '这活动真爽啊', 2, NULL, 50, '2026-02-03 09:20:40', 1, '活动');
INSERT INTO `comment` VALUES (51, '真的太爽了', 2, NULL, 51, '2026-02-03 14:28:03', 11, '博客');
INSERT INTO `comment` VALUES (52, '好看', 2, NULL, 52, '2026-02-03 14:38:52', 12, '博客');

-- ----------------------------
-- Table structure for likes
-- ----------------------------
DROP TABLE IF EXISTS `likes`;
CREATE TABLE `likes`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `fid` int NULL DEFAULT NULL COMMENT '关联ID',
  `user_id` int NULL DEFAULT NULL COMMENT '点赞人ID',
  `module` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '点赞' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of likes
-- ----------------------------
INSERT INTO `likes` VALUES (1, 11, 2, '博客');
INSERT INTO `likes` VALUES (2, 1, 2, '活动');
INSERT INTO `likes` VALUES (3, 12, 2, '博客');

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '内容',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建时间',
  `user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (1, '今天系统正式上线，开始内测', '今天系统正式上线，开始内测', '2023-09-05', 'admin');
INSERT INTO `notice` VALUES (2, '所有功能都已完成，可以正常使用', '所有功能都已完成，可以正常使用', '2023-09-05', 'admin');
INSERT INTO `notice` VALUES (3, '今天天气很不错，可以出去一起玩了', '今天天气很不错，可以出去一起玩了', '2023-09-05', 'admin');
INSERT INTO `notice` VALUES (4, '社区发文规范已更新', '请勿发布违规内容，优质原创博客将优先推荐到首页榜单。', '2026-02-03', '管理员');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色标识',
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简介',
  `birth` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生日',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '123456', '管理员', 'http://localhost:9090/files/1770019142365-1697438073596-avatar.png', 'ADMIN', '女', '18912345678', 'demo@example.com', '哈哈哈', '2026-02-24');
INSERT INTO `user` VALUES (2, 'light', '123456', 'light', 'http://localhost:9090/files/default-user.png', 'USER', NULL, '13800000000', 'demo@example.com', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
