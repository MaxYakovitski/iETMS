package com.mayak.iet.features.request.domain.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("SPOT")
public class SpotRequest extends Request {

}