package com.mayak.ietms.request.validator;

import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;
import com.mayak.ietms.request.dto.bid.BidCreateDto;

import java.math.BigDecimal;

public class BidContractValidator implements Validator<BidCreateDto> {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999.99");

    @Override
    public ValidationResult isValid(BidCreateDto object) {
        var result = new ValidationResult();

        if (object == null) {
            result.add("bid", "Bid data is missing");
            return  result;
        }

        if (object.amount() == null) {
            result.add("amount", "Amount is required");
        } else if (object.amount().compareTo(BigDecimal.ZERO) < 0) {
            result.add("amount", "Amount must not be negative");
        } else if (object.amount().compareTo(MAX_AMOUNT) > 0) {
            result.add("amount", "Amount must not exceed 99 999.99");
        }
        return  result;
    }
}
