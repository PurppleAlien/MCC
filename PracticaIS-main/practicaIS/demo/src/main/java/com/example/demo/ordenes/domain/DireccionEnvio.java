package com.example.demo.ordenes.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public final class DireccionEnvio {

    private final String nombreDestinatario;
    private final String calle;
    private final String ciudad;
    private final String estado;
    private final String codigoPostal;
    private final String pais;
    private final String telefono;
    private final String instrucciones;

    protected DireccionEnvio() {
        this.nombreDestinatario = null;
        this.calle = null;
        this.ciudad = null;
        this.estado = null;
        this.codigoPostal = null;
        this.pais = null;
        this.telefono = null;
        this.instrucciones = null;
    }

    @JsonCreator
    public DireccionEnvio(
            @JsonProperty("nombreDestinatario") String nombreDestinatario,
            @JsonProperty("calle") String calle,
            @JsonProperty("ciudad") String ciudad,
            @JsonProperty("estado") String estado,
            @JsonProperty("codigoPostal") String codigoPostal,
            @JsonProperty("pais") String pais,
            @JsonProperty("telefono") String telefono,
            @JsonProperty("instrucciones") String instrucciones) {

        validarNoNuloOVC(nombreDestinatario, "Nombre del destinatario");
        validarNoNuloOVC(calle, "Calle");
        validarNoNuloOVC(ciudad, "Ciudad");
        validarNoNuloOVC(estado, "Estado");
        validarNoNuloOVC(codigoPostal, "Código Postal");
        validarNoNuloOVC(pais, "País");

        if (!pais.equalsIgnoreCase("México") && !pais.equalsIgnoreCase("Mexico")) {
            throw new IllegalArgumentException("Por ahora solo se permiten envíos nacionales (México)");
        }

        if (codigoPostal == null || !codigoPostal.matches("\\d{5}")) {
            throw new IllegalArgumentException("El código postal debe tener exactamente 5 dígitos");
        }

        if (telefono == null || !telefono.matches("\\d{10}")) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 10 dígitos");
        }

        this.nombreDestinatario = nombreDestinatario;
        this.calle = calle;
        this.ciudad = ciudad;
        this.estado = estado;
        this.codigoPostal = codigoPostal;
        this.pais = pais;
        this.telefono = telefono;
        this.instrucciones = instrucciones;
    }

    private void validarNoNuloOVC(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío");
        }
    }

    public String formatear() {
        StringBuilder sb = new StringBuilder();
        sb.append(nombreDestinatario).append("\n");
        sb.append(calle).append("\n");
        sb.append(codigoPostal).append(" ").append(ciudad).append(", ").append(estado).append("\n");
        sb.append(pais);
        if (telefono != null && !telefono.isEmpty()) {
            sb.append("\nTel: ").append(telefono);
        }
        if (instrucciones != null && !instrucciones.isEmpty()) {
            sb.append("\nObs: ").append(instrucciones);
        }
        return sb.toString();
    }

    public String getNombreDestinatario() { return nombreDestinatario; }
    public String getCalle() { return calle; }
    public String getCiudad() { return ciudad; }
    public String getEstado() { return estado; }
    public String getCodigoPostal() { return codigoPostal; }
    public String getPais() { return pais; }
    public String getTelefono() { return telefono; }
    public String getInstrucciones() { return instrucciones; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DireccionEnvio that = (DireccionEnvio) o;
        return Objects.equals(nombreDestinatario, that.nombreDestinatario) &&
                Objects.equals(calle, that.calle) &&
                Objects.equals(ciudad, that.ciudad) &&
                Objects.equals(estado, that.estado) &&
                Objects.equals(codigoPostal, that.codigoPostal) &&
                Objects.equals(pais, that.pais) &&
                Objects.equals(telefono, that.telefono) &&
                Objects.equals(instrucciones, that.instrucciones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombreDestinatario, calle, ciudad, estado, codigoPostal, pais, telefono, instrucciones);
    }

    @Override
    public String toString() {
        return "DireccionEnvio{" +
                "destinatario='" + nombreDestinatario + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", pais='" + pais + '\'' +
                '}';
    }
}