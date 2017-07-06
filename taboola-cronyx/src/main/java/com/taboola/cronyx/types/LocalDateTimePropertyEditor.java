package com.taboola.cronyx.types;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimePropertyEditor extends PropertyEditorSupport {
    private DateTimeFormatter formatter;

    public LocalDateTimePropertyEditor(String dateFormat) {
        formatter = DateTimeFormatter.ofPattern(dateFormat);
    }

    @Override
    public String getAsText() {
        return formatter.format((LocalDateTime) this.getValue());
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text != null && !text.isEmpty()) {
            this.setValue(LocalDateTime.parse(text, formatter));
        }
    }
}