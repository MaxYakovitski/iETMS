package com.mayak.ietms.infrastructure.window;

/**
 * Identifies a detached window for deduplication in {@link WindowService}.
 *
 * <p>Two keys are equal when both {@code viewId} and {@code discriminator}
 * are equal — {@code record} equality semantics apply.
 * Pass {@code null} as the key to {@code openDetachedWindow} to skip tracking
 * entirely (a new window will always be opened).
 *
 * <p>Example — one window per transport request type:
 * <pre>{@code
 * new WindowKey(TransportRequestController.class.getSimpleName(), requestType)
 * }</pre>
 *
 * @param viewId      logical name of the view (typically the simple class name of the controller)
 * @param discriminator additional qualifier when multiple instances of the same view are allowed
 *                      (e.g. a {@link com.mayak.ietms.request.dto.enums.RequestTypeDto});
 *                      may be {@code null} for singleton views
 */
public record WindowKey(String viewId, Object discriminator) {}