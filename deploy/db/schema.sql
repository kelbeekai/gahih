
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `admin_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_log` (
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `target_id` bigint DEFAULT NULL,
  `target_community_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `action` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_community_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `after_snapshot` longtext COLLATE utf8mb4_unicode_ci,
  `before_snapshot` longtext COLLATE utf8mb4_unicode_ci,
  `reason` longtext COLLATE utf8mb4_unicode_ci,
  `target_type` enum('ATTACHMENT','COMMENT','MEMBER','POST') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl1e2k824hb3h82ewjak0rwvrl` (`member_id`),
  CONSTRAINT `FKl1e2k824hb3h82ewjak0rwvrl` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `admin_write_only` bit(1) NOT NULL,
  `comment_allowed` bit(1) NOT NULL,
  `display_order` int NOT NULL,
  `reaction_allowed` bit(1) NOT NULL,
  `secret_post_allowed` bit(1) NOT NULL,
  `visible_in_header` bit(1) NOT NULL,
  `country_community_id` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `code` enum('FREE','HOUSING','INQUIRY','JOB','LOCAL_LIFE','MARKET','NOTICE','STUDY_IMMIGRATION','TRAVEL') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_community_code` (`country_community_id`,`code`),
  CONSTRAINT `FKp6lpiy31uad577cly1ndcq1vm` FOREIGN KEY (`country_community_id`) REFERENCES `country_community` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `user_deleted_before_admin_action` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `dislike_count` bigint NOT NULL,
  `edited_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `like_count` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` tinytext COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','ADMIN_BLINDED','ADMIN_DELETED','USER_DELETED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmrrrpi513ssu63i2783jyiv9m` (`member_id`),
  KEY `FKs1slvnkuemjsq2kj4h3vhx7i1` (`post_id`),
  CONSTRAINT `FKmrrrpi513ssu63i2783jyiv9m` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `FKs1slvnkuemjsq2kj4h3vhx7i1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `comment_mention`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_mention` (
  `mention_end_index_exclusive` int NOT NULL,
  `mention_start_index` int NOT NULL,
  `comment_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mentioned_member_id` bigint NOT NULL,
  `mentioned_nickname_snapshot` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_mention_comment_member` (`comment_id`,`mentioned_member_id`),
  KEY `FKpwe9rdi6p1j0062yo9isjqyv0` (`mentioned_member_id`),
  CONSTRAINT `FKlaak1ehrr2j1fofh2qvk96dqh` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`),
  CONSTRAINT `FKpwe9rdi6p1j0062yo9isjqyv0` FOREIGN KEY (`mentioned_member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `comment_reaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_reaction` (
  `comment_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `reaction_type` enum('DISLIKE','LIKE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_reaction_member_comment` (`member_id`,`comment_id`),
  KEY `FKrkwlnq2025sav3ixnd4ue3nk0` (`comment_id`),
  CONSTRAINT `FK9brm5ytdyismpoesyhrxkv8a8` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `FKrkwlnq2025sav3ixnd4ue3nk0` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `country_community`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `country_community` (
  `display_order` int NOT NULL,
  `enabled` bit(1) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `continent` enum('AFRICA','ASIA','EUROPE','NORTH_AMERICA','OCEANIA','SOUTH_AMERICA') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_country_community_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `email_auth_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_auth_request` (
  `attempt_count` int NOT NULL,
  `request_count` int NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_requested_at` datetime(6) NOT NULL,
  `target_member_id` bigint DEFAULT NULL,
  `verified_at` datetime(6) DEFAULT NULL,
  `target_username` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `purpose` enum('EMAIL_CHANGE_VERIFY','PASSWORD_RESET_VERIFY','SIGNUP_VERIFY','USERNAME_RECOVERY') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_email_auth_email_purpose_created_at` (`email`,`purpose`,`created_at`),
  KEY `idx_email_auth_target_member_id` (`target_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `password_version` int NOT NULL,
  `suspension_count` int NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `finalized_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nickname_changed_at` datetime(6) DEFAULT NULL,
  `password_changed_at` datetime(6) DEFAULT NULL,
  `suspended_at` datetime(6) DEFAULT NULL,
  `suspended_until` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `withdraw_expire_at` datetime(6) DEFAULT NULL,
  `withdrawn_at` datetime(6) DEFAULT NULL,
  `nickname` varchar(12) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ADMIN','USER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','DELETED','SUSPENDED','WITHDRAWN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `suspension_reason` longtext COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhh9kg6jti4n1eoiertn2k6qsc` (`nickname`),
  UNIQUE KEY `UKgc3jmn7c2abyo3wf6syln5t2i` (`username`),
  UNIQUE KEY `UKmbmcqelty0fbrvxp1q58dn57t` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `member_daily_visit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_daily_visit` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `visit_date` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_daily_visit_member_date` (`member_id`,`visit_date`),
  KEY `idx_member_daily_visit_visit_date` (`visit_date`),
  KEY `idx_member_daily_visit_member_id` (`member_id`),
  CONSTRAINT `fk_member_daily_visit_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `nickname_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nickname_history` (
  `changed_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `change_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `new_nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `previous_nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm96ye193n26atldv5a936mmrm` (`member_id`),
  CONSTRAINT `FKm96ye193n26atldv5a936mmrm` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `nickname_reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nickname_reservation` (
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reason_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `password_reset_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_session` (
  `created_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reset_token_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_password_reset_member_id` (`member_id`),
  KEY `idx_password_reset_email_created_at` (`email`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `pinned` bit(1) NOT NULL,
  `secret` bit(1) NOT NULL,
  `user_deleted_before_admin_action` bit(1) NOT NULL,
  `view_count` int NOT NULL,
  `category_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `dislike_count` bigint NOT NULL,
  `edited_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `like_count` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `pinned_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `zip_download_count` bigint NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` tinytext COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','ADMIN_BLINDED','ADMIN_DELETED','USER_DELETED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg6l1ydp1pwkmyj166teiuov1b` (`category_id`),
  KEY `FK83s99f4kx8oiqm3ro0sasmpww` (`member_id`),
  CONSTRAINT `FK83s99f4kx8oiqm3ro0sasmpww` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `FKg6l1ydp1pwkmyj166teiuov1b` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_attachment` (
  `created_at` datetime(6) DEFAULT NULL,
  `download_count` bigint NOT NULL,
  `file_size` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `content_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `original_file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `stored_file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','ADMIN_DELETED','USER_DELETED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9h8r5b07ql2vctxfyy21jogy0` (`stored_file_name`),
  KEY `FKmof1y73w0oea4caub8rpkhlmi` (`post_id`),
  CONSTRAINT `FKmof1y73w0oea4caub8rpkhlmi` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post_reaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_reaction` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `reaction_type` enum('DISLIKE','LIKE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_reaction_member_post` (`member_id`,`post_id`),
  KEY `FKd7eopt2vpb38ybx3xhaelnhne` (`post_id`),
  CONSTRAINT `FK8du5iv2lcxrbvobu9kphu3q2q` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `FKd7eopt2vpb38ybx3xhaelnhne` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post_trade_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_trade_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `status` enum('CLOSED','OPEN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('GIVE','SELL','WANTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_trade_info_post` (`post_id`),
  CONSTRAINT `FK37bgo67xchq22uv9jp9xqvsww` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report` (
  `report_cycle` int NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `handled_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `community_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `community_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `detail` longtext COLLATE utf8mb4_unicode_ci,
  `reason_type` enum('ABUSE_OR_HARASSMENT','HATE_OR_DISCRIMINATION','IMPERSONATION_OR_FRAUD','INAPPROPRIATE_ATTACHMENT','INAPPROPRIATE_NICKNAME','OTHER','PRIVACY_EXPOSURE','SEXUAL_OR_OBSCENE','SPAM','VIOLENCE_OR_ILLEGAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTION_TAKEN','NO_ACTION_NEEDED','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_type` enum('ATTACHMENT','COMMENT','MEMBER','POST') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1uivt2jamt7slp3banldgnsef` (`reporter_id`),
  CONSTRAINT `FK1uivt2jamt7slp3banldgnsef` FOREIGN KEY (`reporter_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `reported_target`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reported_target` (
  `current_cycle` int NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_handled_at` datetime(6) DEFAULT NULL,
  `last_handled_by_member_id` bigint DEFAULT NULL,
  `last_reported_at` datetime(6) DEFAULT NULL,
  `parent_post_id` bigint DEFAULT NULL,
  `pending_report_count` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `total_report_count` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `writer_member_id` bigint DEFAULT NULL,
  `community_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `community_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `writer_nickname_snapshot` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `parent_post_title_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_name_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_decision_reason` longtext COLLATE utf8mb4_unicode_ci,
  `status` enum('ACTION_TAKEN','NO_ACTION_NEEDED','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_type` enum('ATTACHMENT','COMMENT','MEMBER','POST') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reported_target_type_id` (`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `visitor_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visitor_statistics` (
  `visit_date` date NOT NULL,
  `daily_count` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_visitor_statistics_visit_date` (`visit_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

