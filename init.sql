CREATE DATABASE IF NOT EXISTS sistema_reservas;
USE sistema_reservas;

CREATE TABLE IF NOT EXISTS USUARIO (
    idUsuario VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS RESERVA (
    idReserva VARCHAR(10) PRIMARY KEY,
    pasajero VARCHAR(100),
    aeropuertoOrigen VARCHAR(10),
    aeropuertoDestino VARCHAR(10),
    fecha DATE
);

CREATE TABLE IF NOT EXISTS HISTORIAL (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora DATETIME,
    usuario VARCHAR(50),
    accion TEXT
);

INSERT INTO USUARIO (idUsuario, password) 
VALUES ('admin', '1234')
ON DUPLICATE KEY UPDATE password = VALUES(password);