package com.mayak.ietms.ui.workspace.request.base;

import com.mayak.ietms.request.dto.filter.RequestFilterDto;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.request.dto.view.RequestListItemDto;
import com.mayak.ietms.ui.home.HomeController;
import javafx.collections.ObservableList;

import java.util.Optional;

public interface RequestsParent {

    ParentType getParentType();
    ObservableList<RequestListItemDto> getRequestItems();
    default void invalidateRequest(Long requestId) {}

    void setHomeController(HomeController homeController);
    HomeController getHomeController();

    default void onShow () {}
    default void onHide () {}

    default void applyFilter(RequestFilterDto filter) {}
    default void clearFilter() {}
    default Optional<RequestFilterDto> getLastAppliedFilter() {return Optional.empty();}

    default void applySearch(String query) {}

    default void fillFormWithRequest(RequestDetailsDto request) {}
    default void pauseUpdates() {}
    default void resumeUpdates() {}
    default void safeUpdate(Runnable resetLogic) {}
}