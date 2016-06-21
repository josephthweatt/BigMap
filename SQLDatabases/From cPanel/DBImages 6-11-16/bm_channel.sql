-- phpMyAdmin SQL Dump
-- version 4.0.10.7
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 11, 2016 at 01:19 PM
-- Server version: 5.5.45-cll-lve
-- PHP Version: 5.4.31

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `bm_channel`
--

-- --------------------------------------------------------

--
-- Table structure for table `broadcast_member`
--

CREATE TABLE IF NOT EXISTS `broadcast_member` (
  `broadcaster_id` int(10) unsigned NOT NULL,
  `current_lat` double DEFAULT NULL,
  `current_long` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='broadcaster_id needs a member primary key';

-- --------------------------------------------------------

--
-- Table structure for table `channels_broadcasting`
--

CREATE TABLE IF NOT EXISTS `channels_broadcasting` (
  `user_id` int(10) unsigned NOT NULL,
  `channel_id` int(10) unsigned NOT NULL,
  `is_broadcasting` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1 = broadcasting'
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='holds channel id''s of all channels that broadcast the user';

-- --------------------------------------------------------

--
-- Table structure for table `channel_info`
--

CREATE TABLE IF NOT EXISTS `channel_info` (
  `id` int(10) unsigned NOT NULL,
  `date_created` datetime NOT NULL,
  `host` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='host requires a member primary key';

-- --------------------------------------------------------

--
-- Table structure for table `location_history`
--

CREATE TABLE IF NOT EXISTS `location_history` (
  `broadcaster_id` int(10) unsigned NOT NULL,
  `channel_id` int(10) unsigned DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `latitude` float DEFAULT NULL,
  `longitude` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='broadcaster_id needs a member key';

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
