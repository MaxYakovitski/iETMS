package com.mayak.ietms.support.enums;

import lombok.Getter;

@Getter
public enum View {
    LOGIN("/fxml/login.fxml"),
    HOME("/fxml/home.fxml"),
    DASHBOARD("/fxml/dashboard.fxml"),

    WORKSPACE_POPUP("/fxml/workspace_popup.fxml"),
    REQUESTS_CLIENT("/fxml/requests_client.fxml"),
    REQUESTS_TRANSPORT("/fxml/requests_transport.fxml"),
    PLANNER("/fxml/planner.fxml"),
    SHIPMENT_ITEM("/fxml/shipment_item.fxml"),

    CRM_POPUP("/fxml/crm_popup.fxml"),
    CRM_COMPANIES("/fxml/crm_company.fxml"),
    CRM_CONTRACTS("/fxml/crm_contracts.fxml"),

    ANALYTICS_POPUP("/fxml/analytics_popup.fxml"),
    STATISTICS_DEPARTMENT("/fxml/statistics_department.fxml"),
    STATISTICS_EMPLOYEES("/fxml/statistics_employees.fxml"),
    STATISTICS_COMPANIES("/fxml/statistics_companies.fxml"),
    STATISTICS_REPORT("/fxml/statistics_report.fxml"),

    ADMINISTRATION_POPUP("/fxml/administration_popup.fxml"),
    SETTINGS_DEPARTMENTS("/fxml/settings_department.fxml"),
    SETTINGS_USERS("/fxml/settings_user.fxml"),
    SETTINGS_LOCATION("/fxml/settings_location.fxml"),

    USER("/fxml/user.fxml"),

    ABOUT("/fxml/about.fxml"),

    REQUEST_ITEM("/fxml/request_item.fxml"),
    REQUEST_COMMENT("/fxml/request_item_comment.fxml"),
    ADD_BID("/fxml/request_item_add_bid.fxml"),
    BID_ITEM("/fxml/bid_item.fxml"),
    BID_HISTORY("/fxml/bid_history.fxml"),
    REQUEST_MORE("/fxml/request_item_more.fxml"),
    REFUSE_REASON("/fxml/request_item_refuse_reason.fxml"),
    LANES("/fxml/lanes_selector.fxml"),
    FINAL_PRICE("/fxml/request_item_final_price.fxml"),

    FILTER("/fxml/requests_filter.fxml"),
    SEARCH("/fxml/requests_search.fxml"),

    UPDATE("/fxml/update.fxml");

    private final String path;

    View(String path) {
        this.path = path;
    }
}