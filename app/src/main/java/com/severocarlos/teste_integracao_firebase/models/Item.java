package com.severocarlos.teste_integracao_firebase.models;

import androidx.annotation.NonNull;

public class Item {
    private float medida;
    private String uuid;

    public String toString(){
        return String.valueOf(medida);
    }

    public Item (String uuid, float medida){
        this.uuid = uuid;
        this.medida = medida;
    }

    public Item (float medida){
        this.medida = medida;
    }

    public float getMedida() {
        return medida;
    }

    public void setMedida(float medida) {
        this.medida = medida;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
