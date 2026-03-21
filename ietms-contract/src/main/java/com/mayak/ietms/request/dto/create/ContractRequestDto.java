package com.mayak.ietms.request.dto.create;

import com.mayak.ietms.request.dto.enums.ContractReasonCodeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContractRequestDto extends BaseRequestDto {
    Long laneId;
    ContractReasonCodeDto reasonCode;
    LocalDate laneValidFrom;
    LocalDate laneValidTo;
}