package com.mayak.ietms.features.shipment.application.access;

import com.mayak.ietms.features.shipment.domain.model.Shipment;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.shared.exception.business.AuthenticationException;
import com.mayak.ietms.shared.exception.business.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class ShipmentAccessService {

    public void requireCanCancelShipment(User user, Shipment shipment) {

        if (user == null) {
            throw new AuthenticationException("User is not authenticated");
        }

        if (shipment == null) {
            throw new UnauthorizedException("Shipment is null");
        }

        Long userId = user.getId();
        if (userId == null) {
            throw new UnauthorizedException("User id is missing");
        }

        if (!shipment.isOwnedBy(userId)) {
            throw new UnauthorizedException(
                    "Only request author can cancel shipment"
            );
        }
    }
}
