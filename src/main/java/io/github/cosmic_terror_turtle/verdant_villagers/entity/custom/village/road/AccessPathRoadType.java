package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.VerticalBlockColumn;

public class AccessPathRoadType {

    public double radius;
    public VerticalBlockColumn column;

    public AccessPathRoadType(double radius, VerticalBlockColumn column) {
        this.radius = radius;
        this.column = column;
    }
}
