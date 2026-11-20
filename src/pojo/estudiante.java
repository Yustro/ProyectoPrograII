
package pojo;

import java.time.LocalDate;

/**
 * Clase que representa a un estudiante de la UTN. Hereda la clase persona que es abstracta.
 * 
 */
public class estudiante extends persona{
    private String carnet;
    private LocalDate fechIngreso;
    private LocalDate fechEgreso;
    private int idCarrera;

    public estudiante(String carnet, LocalDate fechIngreso, LocalDate fechEgreso, int idCarrera, int cedula, String nombre, LocalDate fechNac, String direccion, String telefono, String email) {
        super(cedula, nombre, fechNac, direccion, telefono, email);
        this.carnet = carnet;
        this.fechIngreso = fechIngreso;
        this.fechEgreso = fechEgreso;
        this.idCarrera = idCarrera;
    }
    
        public estudiante() {
        super();
        this.carnet = "";
        this.fechIngreso = null;
        this.fechEgreso = null;
        this.idCarrera = 0;
    }

    public String getCarnet() {
        return carnet;
    }

    public void setCarnet(String carnet) {
        this.carnet = carnet;
    }

    public LocalDate getFechIngreso() {
        return fechIngreso;
    }

    public void setFechIngreso(LocalDate fechIngreso) {
        this.fechIngreso = fechIngreso;
    }

    public LocalDate getFechEgreso() {
        return fechEgreso;
    }

    public void setFechEgreso(LocalDate fechEgreso) {
        this.fechEgreso = fechEgreso;
    }

    public int getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }

  
    
}
