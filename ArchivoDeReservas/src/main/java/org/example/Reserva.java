package org.example;

public class Reserva {
    String numReserva;
    String destino;
    String origen;
    Person passenger;

    public Reserva(String num, String des, String ori, Person pas) {
        this.numReserva = num;
        this.destino = des;
        this.origen = ori;
        this.passenger = pas;
    }
}
