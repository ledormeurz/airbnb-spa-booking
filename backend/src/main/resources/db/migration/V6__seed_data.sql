-- Price rules
INSERT INTO price_rules (name, booking_type, day_type, price, active) VALUES
('Nuitée semaine', 'NIGHT_STAY', 'WEEKDAY', 120.00, true),
('Nuitée week-end', 'NIGHT_STAY', 'WEEKEND', 150.00, true),
('Séjour semaine', 'EXTENDED_STAY', 'WEEKDAY', 100.00, true),
('Séjour week-end', 'EXTENDED_STAY', 'WEEKEND', 130.00, true),
('Séance spa', 'SPA_SESSION', 'WEEKDAY', 60.00, true),
('Séance spa week-end', 'SPA_SESSION', 'WEEKEND', 80.00, true);

-- Equipment
INSERT INTO equipment (name, description, icon, active) VALUES
('Jacuzzi 6 places', 'Jacuzzi extérieur avec jets hydromassants et éclairage LED', 'hot_tub', true),
('Sauna finlandais', 'Sauna sec en bois de cèdre, capacité 4 personnes', 'sauna', true),
('Douche extérieure', 'Douche chaude extérieure avec vue sur la nature', 'shower', true),
('Espace détente', 'Salon de jardin, transats et hamac', 'deck', true),
('Wi-Fi haut débit', 'Fibre optique 1 Gb/s', 'wifi', true),
('Télévision 4K', 'Smart TV 55" avec Netflix et Prime Video', 'tv', true),
('Cuisine équipée', 'Réfrigérateur, plaque induction, four, micro-ondes, cafetière', 'kitchen', true),
('Parking privé', 'Place de parking sécurisée', 'parking', true);

-- Availability blocks (future maintenance period)
INSERT INTO availability_blocks (start_date, end_date, reason) VALUES
('2026-12-20', '2026-12-27', 'Maintenance annuelle');