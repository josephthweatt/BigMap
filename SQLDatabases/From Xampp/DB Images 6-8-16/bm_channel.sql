-- phpMyAdmin SQL Dump
-- version 4.5.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Jun 09, 2016 at 12:33 AM
-- Server version: 10.1.13-MariaDB
-- PHP Version: 5.6.20

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `bm_channel`
--

-- --------------------------------------------------------

--
-- Table structure for table `broadcast_member`
--

CREATE TABLE `broadcast_member` (
  `broadcaster_id` int(10) UNSIGNED NOT NULL,
  `current_lat` float DEFAULT NULL,
  `current_long` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='broadcaster_id needs a member primary key';

-- --------------------------------------------------------

--
-- Table structure for table `channels_broadcasting`
--

CREATE TABLE `channels_broadcasting` (
  `user_id` int(10) UNSIGNED NOT NULL,
  `channel_id` int(10) UNSIGNED NOT NULL,
  `is_broadcasting` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1 = broadcasting'
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='holds channel id''s of all channels that broadcast the user';

-- --------------------------------------------------------

--
-- Table structure for table `channel_info`
--

CREATE TABLE `channel_info` (
  `id` int(10) UNSIGNED NOT NULL,
  `date_created` datetime NOT NULL,
  `host` int(10) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='host requires a member primary key';

-- --------------------------------------------------------

--
-- Table structure for table `location_history`
--

CREATE TABLE `location_history` (
  `broadcaster_id` int(10) UNSIGNED NOT NULL,
  `channel_id` int(10) UNSIGNED DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `latitude` float DEFAULT NULL,
  `longitude` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='broadcaster_id needs a member key';

--
-- Indexes for dumped tables
--

--
-- Indexes for table `channel_info`
--
ALTER TABLE `channel_info`
  ADD PRIMARY KEY (`id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
