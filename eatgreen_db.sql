-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : jeu. 05 mars 2026 à 20:01
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `eatgreen_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `password_resets`
--

CREATE TABLE `password_resets` (
  `id` int(11) NOT NULL,
  `email` varchar(100) NOT NULL,
  `token` varchar(64) NOT NULL,
  `expires_at` datetime NOT NULL,
  `used` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `restaurants`
--

CREATE TABLE `restaurants` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `nom_restaurant` varchar(100) NOT NULL,
  `siret` varchar(14) NOT NULL,
  `adresse` varchar(255) NOT NULL,
  `code_postal` varchar(5) NOT NULL,
  `commune` varchar(100) NOT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `restaurants`
--

INSERT INTO `restaurants` (`id`, `user_id`, `nom_restaurant`, `siret`, `adresse`, `code_postal`, `commune`, `date_creation`) VALUES
(1, 4, 'coco', '12345678912345', '6fhislambroisepae', '94000', 'creteil', '2026-03-05 15:36:54');

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `telephone` varchar(15) NOT NULL,
  `email` varchar(100) NOT NULL,
  `role` enum('etudiant','admin','restaurateur') NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `date_inscription` timestamp NOT NULL DEFAULT current_timestamp(),
  `date_validation` timestamp NULL DEFAULT NULL,
  `mode_paiement` varchar(20) DEFAULT NULL,
  `demande_suppression` tinyint(1) DEFAULT 0,
  `cb_numero` varchar(20) DEFAULT NULL,
  `cb_date` varchar(5) DEFAULT NULL,
  `cb_cvv` varchar(4) DEFAULT NULL,
  `paypal_email` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id`, `nom`, `prenom`, `telephone`, `email`, `role`, `mot_de_passe`, `date_inscription`, `date_validation`, `mode_paiement`, `demande_suppression`, `cb_numero`, `cb_date`, `cb_cvv`, `paypal_email`) VALUES
(1, 'sarachh', 'benzri', '0764183531', 'sarah.benizri@etu.unilim.fr', 'etudiant', '$2y$10$DbSLxG28DMr9RflFwicC9uKzd9Rg97F1FiHPxSe1QIvNv.7mENKRO', '2026-03-05 09:07:02', '2026-03-05 12:46:19', 'Paypal', 0, NULL, NULL, NULL, 'sarahbenizri2004@gmail.com'),
(2, 'david', 'benizri', '0764183531', 'davidbenizri@unilim.fr', 'admin', '$2y$10$xzlp6j1OujPvF7lyKzZiYO5U0h44j7iwv6/eBjxLjbKKCE3CISPEC', '2026-03-05 13:04:54', '2026-03-05 15:27:19', NULL, 0, NULL, NULL, NULL, NULL),
(4, 'Rm', 'Benziri', '0611713588', 'rmbenizri@unilim.fr', 'restaurateur', '$2y$10$kGA0N2mQZ/ovp1XGeRQ9GeGZ9paYk9H6kjNJxJEz/rELfH7DuBNYO', '2026-03-05 15:36:08', '2026-03-05 16:39:25', NULL, 0, NULL, NULL, NULL, NULL);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `password_resets`
--
ALTER TABLE `password_resets`
  ADD PRIMARY KEY (`id`),
  ADD KEY `email` (`email`),
  ADD KEY `token` (`token`);

--
-- Index pour la table `restaurants`
--
ALTER TABLE `restaurants`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `siret` (`siret`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `email_2` (`email`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `password_resets`
--
ALTER TABLE `password_resets`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `restaurants`
--
ALTER TABLE `restaurants`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `password_resets`
--
ALTER TABLE `password_resets`
  ADD CONSTRAINT `password_resets_ibfk_1` FOREIGN KEY (`email`) REFERENCES `users` (`email`) ON DELETE CASCADE;

--
-- Contraintes pour la table `restaurants`
--
ALTER TABLE `restaurants`
  ADD CONSTRAINT `restaurants_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
