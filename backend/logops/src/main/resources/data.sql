-- Naval Units
INSERT INTO naval_units (name, status) VALUES ('Frégate Aquitaine', 'Opérationnel'), ('Porte-avions Charles de Gaulle', 'En maintenance'), ('Sous-marin Le Triomphant', 'Opérationnel'), ('Frégate La Fayette', 'En réparation'), ('Bâtiment de projection et de commandement Tonnerre', 'Inactif');

-- Resources
INSERT INTO resources (name, level, status) VALUES ('Carburant', 78, 'OK'), ('Munitions', 65, 'OK'), ('Pièces détachées', 42, 'Warning'), ('Vivres', 91, 'OK');

-- Alerts
INSERT INTO alerts (title, description, priority, created_at) VALUES ('Stock critique - Pièces moteur F-22', 'Le stock de pièces moteur F-22 est inférieur au seuil critique.', 'Haute', '2025-10-31 22:30:00'), ('Maintenance retardée - Frégate Aquitaine', 'La maintenance préventive de la frégate Aquitaine est en retard de 48 heures.', 'Moyenne', '2025-10-31 20:30:00'), ('Livraison en attente - Base de Toulon', 'Une livraison de vivres est en attente à la base de Toulon depuis 12 heures.', 'Basse', '2025-10-31 18:30:00');

-- KPIs
INSERT INTO kpis (name, `value`, change_description) VALUES ('Délai moyen d''approvisionnement', '3.2 jours', '-8% vs mois précédent'), ('Taux de disponibilité matérielle', '89%', '+2% vs mois précédent'), ('Coûts logistiques', '4.2M €', '+5% vs budget prévu');

-- Logistic Requests
INSERT INTO logistic_requests (request_id_str, description, unit_id, priority, status, due_date) VALUES ('DL-2025-042', 'Ravitaillement carburant', 1, 'Urgente', 'En cours', '2025-04-25');
