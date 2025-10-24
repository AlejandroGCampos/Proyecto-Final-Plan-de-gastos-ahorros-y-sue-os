package com.example.expoporgra2prompt;

public class MensajeChat {
    private String texto;
    private boolean esUsuario;

    public MensajeChat(String texto, boolean esUsuario) {
        this.texto = texto;
        this.esUsuario = esUsuario;
    }

    public String getTexto() {
        return texto;
    }

    public boolean esUsuario() {
        return esUsuario;
    }
}

