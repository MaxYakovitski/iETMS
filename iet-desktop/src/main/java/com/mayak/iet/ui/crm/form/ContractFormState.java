package com.mayak.iet.ui.crm.form;

import com.mayak.iet.company.dto.CompanyDto;
import com.mayak.iet.lane.dto.LaneViewDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractFormState {

    private CompanyDto selectedCompany;

    private LaneViewDto selectedLane;
    private LaneViewDto editingLane;

    private Mode mode = Mode.CREATE;

    public enum Mode {
        CREATE,
        EDIT
    }

    public boolean isEditMode() {
        return mode == Mode.EDIT;
    }

    public void switchToCreate() {
        mode = Mode.CREATE;
        editingLane = null;
    }

    public void switchToEdit(LaneViewDto lane) {
        mode = Mode.EDIT;
        editingLane = lane;
    }
}