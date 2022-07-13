package com.example.mymission;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class MotoboyAccount implements Serializable {
    String id;
    String nome;
    String token;
    String email;
    String telefone;
    String icon_foto;
    String modelo;
    String placa;
    String cor;
    Boolean online;
    Boolean permissao;

   MotoboyAccount(){}

   public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getToken() {
        return token;
    }

    public String getModelo() {
        return modelo;
    }

    public String getPlaca() {
        return placa;
    }

    public String getCor() {
        return cor;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getIcon_foto() {
        return icon_foto;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
   
    public void setToken(String token) {
        this.token = token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setIcon_foto(String icon_foto) {
        this.icon_foto = icon_foto;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Boolean getPermissao() {
        return permissao;
    }

    public void setPermissao(Boolean permissao) {
        this.permissao = permissao;
    }
}
