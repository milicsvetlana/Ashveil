package com.ashveil.objects;

public class ResourceObject extends WorldObject{

    ResourceType type;

    public ResourceObject(float x, float y, ResourceType type) {
        super(x, y, type.hp);
        this.type = type;
    }

    public ResourceType getType() {return type;}
}
