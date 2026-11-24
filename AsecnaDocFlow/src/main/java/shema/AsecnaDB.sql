--Création de la database (IF NOT EXIST)

-- Création de la table product
CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    num_facture VARCHAR(100) NOT NULL,
    designation VARCHAR(255) NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire NUMERIC(10, 2) NOT NULL,
    prix_total NUMERIC(10, 2) NOT NULL,
    fournisseur VARCHAR(255),
    lieu_reception VARCHAR(255),
    date_reception VARCHAR(50)
);