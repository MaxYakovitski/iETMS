package com.mayak.iet.domain.contract;

import com.mayak.iet.company.dto.CompanyDto;
import com.mayak.iet.lane.dto.LaneViewDto;
import com.mayak.iet.request.dto.enums.TransportTypeDto;
import com.mayak.iet.ui.crm.form.ContractFormState;

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
