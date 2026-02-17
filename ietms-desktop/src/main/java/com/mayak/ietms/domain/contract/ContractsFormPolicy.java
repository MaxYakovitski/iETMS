package com.mayak.ietms.domain.contract;

import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.lane.dto.LaneViewDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import com.mayak.ietms.ui.crm.form.ContractFormState;

public class ContractsFormPolicy {
    public boolean isTemperatureEnabled(TransportTypeDto transportType) {
        return transportType == TransportTypeDto.REF;
    }

    public void onCompanySelected(ContractFormState state, CompanyDto company) {
        state.setSelectedCompany(company);
        state.setSelectedLane(null);
        state.switchToCreate();
    }

    public void onEditRequested(ContractFormState state, LaneViewDto lane) {
        state.switchToEdit(lane);
    }

    public void onDelete(ContractFormState state, LaneViewDto lane) {
        if (state.isEditMode() && lane.equals(state.getEditingLane())) {
            state.switchToCreate();
        }
    }
}
