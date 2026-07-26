package com.tcc.sspsp.utils;

public final class NormalizaCampos {

    private NormalizaCampos(){

    }

    public static String normalizaRegiao(String regiao){
        if(regiao == null){
            return null;
        }

        return regiao.toLowerCase().replace("zona", "").trim();
    }
}
