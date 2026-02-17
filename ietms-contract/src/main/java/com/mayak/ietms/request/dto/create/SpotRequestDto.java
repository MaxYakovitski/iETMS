package com.mayak.ietms.request.dto.create;

import com.mayak.ietms.request.dto.enums.SpotReasonCodeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpotRequestDto extends BaseRequestDto {
    SpotReasonCodeDto reasonCode;
}