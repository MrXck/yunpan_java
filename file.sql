/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50644
Source Host           : localhost:3306
Source Database       : file

Target Server Type    : MYSQL
Target Server Version : 50644
File Encoding         : 65001

Date: 2022-07-15 09:26:12
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for web_file
-- ----------------------------
DROP TABLE IF EXISTS `web_file`;
CREATE TABLE `web_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `filename` varchar(256) NOT NULL,
  `file_hash_name` varchar(128) NOT NULL,
  `filetype` int(11) NOT NULL,
  `filepath` varchar(256) NOT NULL,
  `file_hash` varchar(256) NOT NULL,
  `status` int(11) NOT NULL,
  `create_time` datetime(6) NOT NULL,
  `is_delete` int(11) NOT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `web_file_parent_id_cfe40a4d_fk_web_file_id` (`parent_id`),
  KEY `web_file_user_id_b5e386de_fk_web_user_id` (`user_id`),
  CONSTRAINT `web_file_parent_id_cfe40a4d_fk_web_file_id` FOREIGN KEY (`parent_id`) REFERENCES `web_file` (`id`),
  CONSTRAINT `web_file_user_id_b5e386de_fk_web_user_id` FOREIGN KEY (`user_id`) REFERENCES `web_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for web_share
-- ----------------------------
DROP TABLE IF EXISTS `web_share`;
CREATE TABLE `web_share` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NOT NULL,
  `period` int(11) NOT NULL,
  `password` varchar(4) NOT NULL,
  `creator_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `web_share_creator_id_d950c7bc_fk_web_user_id` (`creator_id`),
  CONSTRAINT `web_share_creator_id_d950c7bc_fk_web_user_id` FOREIGN KEY (`creator_id`) REFERENCES `web_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for web_share_files
-- ----------------------------
DROP TABLE IF EXISTS `web_share_files`;
CREATE TABLE `web_share_files` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `share_id` int(11) NOT NULL,
  `file_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `web_share_files_share_id_file_id_c3cbaa8a_uniq` (`share_id`,`file_id`),
  KEY `web_share_files_file_id_54790644_fk_web_file_id` (`file_id`),
  CONSTRAINT `web_share_files_file_id_54790644_fk_web_file_id` FOREIGN KEY (`file_id`) REFERENCES `web_file` (`id`),
  CONSTRAINT `web_share_files_share_id_a3f607e9_fk_web_share_id` FOREIGN KEY (`share_id`) REFERENCES `web_share` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for web_user
-- ----------------------------
DROP TABLE IF EXISTS `web_user`;
CREATE TABLE `web_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(16) NOT NULL,
  `password` varchar(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
