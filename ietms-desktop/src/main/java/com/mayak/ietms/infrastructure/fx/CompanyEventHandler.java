package com.mayak.ietms.infrastructure.fx;

import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.company.event.CompanyEvent;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
public class CompanyEventHandler {
    public static void apply(CompanyEvent<CompanyDto> event, Set<String> suggestions) {
        if (event == null || event.getType() == null) return;

        switch (event.getType()) {
            case CREATED, UPDATED -> {
                if (event.getPayload() != null && event.getPayload().name() != null) {
                    suggestions.add(event.getPayload().name());
                }
            }
            case DELETED -> {
                if (event.getPayload() != null && event.getPayload().name() != null) {
                    suggestions.remove(event.getPayload().name());
                }
            }
        }
    }
}