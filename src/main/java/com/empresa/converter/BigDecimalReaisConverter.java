package com.empresa.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@FacesConverter("bigDecimalReaisConverter")
public class BigDecimalReaisConverter implements Converter<BigDecimal> {
    @Override
    public BigDecimal getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String valorAjustado = value.replace(".", "").replace(",", ".");
        return new BigDecimal(valorAjustado.trim());
    }
    @Override
    public String getAsString(FacesContext context, UIComponent component, BigDecimal value) {
        if (value == null) return "";
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(value);
    }
}
