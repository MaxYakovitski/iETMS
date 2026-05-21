package com.mayak.ietms.request.dto.filter;

import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class RequestFilterDto {
    private List<RequestStatusDto> statuses =  new ArrayList<>();
    private List<String> requestTypes = new ArrayList<>();

    private String fromCountry;
    private String fromZipCode;
    private String fromPlace;

    private String toCountry;
    private String toZipCode;
    private String toPlace;

    private Long companyId;
    private String companyName;

    private DatesFilterOption datesFilterOption;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<ShipmentTypeDto> shipmentTypes = new ArrayList<>();
    private List<TransportTypeDto> transportTypes  = new ArrayList<>();

    private DangerousFilterOption dangerous;

    private Double minWeight;
    private Double maxWeight;

    private Double minLdm;
    private Double maxLdm;

    private List<Long> authorIds = new ArrayList<>();
    private List<Long> competitorIds = new ArrayList<>();
    private List<Long> dispatchersIds = new ArrayList<>();

    private RequestSortField sortBy = RequestSortField.START_DATE;
    private boolean asc = true;
}