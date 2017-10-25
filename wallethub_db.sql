-- phpMyAdmin SQL Dump
-- version 4.7.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 25, 2017 at 04:17 PM
-- Server version: 10.1.25-MariaDB
-- PHP Version: 7.1.7

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `wallethub_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `access_log_table`
--

CREATE TABLE `access_log_table` (
  `DATE` datetime(3) NOT NULL,
  `IP` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `REQUEST` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `STATUS` varchar(3) COLLATE utf8_unicode_ci NOT NULL,
  `USER_AGENT` varchar(500) COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `blocked_log_table`
--

CREATE TABLE `blocked_log_table` (
  `ID` int(11) NOT NULL,
  `IP` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `START_DATE` datetime NOT NULL,
  `DURATION` int(11) NOT NULL,
  `TOTAL_REQUESTS` int(11) NOT NULL,
  `THRESHOLD` int(11) NOT NULL,
  `STATUS` varchar(10) COLLATE utf8_unicode_ci NOT NULL,
  `COMMENTS` varchar(200) COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `blocked_log_table`
--
ALTER TABLE `blocked_log_table`
  ADD PRIMARY KEY (`ID`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `blocked_log_table`
--
ALTER TABLE `blocked_log_table`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT;COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
