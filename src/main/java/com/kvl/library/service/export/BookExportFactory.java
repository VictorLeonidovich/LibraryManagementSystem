package com.kvl.library.service.export;

import com.kvl.library.enums.ExportFormat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BookExportFactory {

    private final Map<ExportFormat, BookExportService> exportServices;

    public BookExportFactory(List<BookExportService> services) {
        this.exportServices = services.stream()
                .collect(Collectors.toMap(BookExportService::getSupportedFormat, Function.identity()));
    }

    public BookExportService getService(ExportFormat format) {
        return Optional.ofNullable(exportServices.get(format))
                .orElseThrow(() -> new IllegalArgumentException("Формат экспорта не поддерживается: " + format));
    }
}