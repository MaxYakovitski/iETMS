package com.mayak.ietms.ws;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class WsDestinations {
    public static final String TOPIC_REQUESTS = "/topic/requests";
    public static final String TOPIC_COMPANIES = "/topic/companies";
    public static final String QUEUE_REQUESTS = "/queue/requests";
    public static final String QUEUE_SHIPMENTS = "/queue/shipments";
    public static final String QUEUE_EXTENSION = "/queue/extension";
}