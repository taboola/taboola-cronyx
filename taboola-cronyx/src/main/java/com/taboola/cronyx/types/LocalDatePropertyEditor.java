package com.taboola.cronyx.types;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDatePropertyEditor extends PropertyEditorSupport {
    private DateTimeFormatter formatter;

    public LocalDatePropertyEditor(String dateFormat) {
        formatter = DateTimeFormatter.ofPattern(dateFormat);
    }

    @Override
    public String getAsText() {
        return formatter.format((LocalDate) this.getValue());
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text != null && !text.isEmpty()) {
            this.setValue(LocalDate.parse(text, formatter));
        }
    }
}