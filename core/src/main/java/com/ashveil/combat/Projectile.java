package com.ashveil.combat;

import com.ashveil.Config;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

public class Projectile {
    private float x;
    private float y;

    private final float velocityX;
    private final float velocityY;

    private final int damage;

    private final Rectangle collisionBounds;

    private float lifetime;
    private boolean active;

    public Projectile(float x, float y, float dirX, float dirY, float speed, int damage, float lifetime) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.lifetime = lifetime;

        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (length > 0) {
            dirX /= length;
            dirY /= length;
        }
        this.velocityX = dirX * speed;
        this.velocityY = dirY * speed;

        float size = Config.TILE_SIZE / 2f;
        this.collisionBounds = new Rectangle(x, y, size, size);
        this.active = true;
    }

    private Projectile(float x, float y, float velocityX, float velocityY, int damage, float lifetime){
        this.x = x;
        this.y = y;

        this.velocityX = velocityX;
        this.velocityY = velocityY;

        this.damage = damage;
        this.lifetime = lifetime;

        float size = Config.TILE_SIZE / 2f;
        this.collisionBounds = new Rectangle(x, y, size, size);
        this.active = true;
    }

    public static Projectile fromVelocity(float x, float y, float velocityX, float velocityY, int damage, float lifetime){
        if (damage <= 0) throw new IllegalArgumentException("Projectile damage must be positive.");
        if (lifetime <= 0) throw new IllegalArgumentException("Projectile lifetime must be positive.");
        return new Projectile(x, y, velocityX, velocityY, damage, lifetime);
    }

    public void update(float delta){
        x += velocityX * delta;
        y += velocityY * delta;

        collisionBounds.setPosition(x, y);
        lifetime -= delta;
        if (lifetime <= 0){
            active = false;
        }
    }

    public void deactivate(){active = false;} //imamo jer ce on kasnije mozda udariti u drvo i sl.
    public float getX() {return x;}
    public float getY() {return y;}
    public int getDamage() {return damage;}
    public Rectangle getCollisionBounds() {return collisionBounds;}
    public boolean isActive() {return active;}
    public float getVelocityX() {return velocityX;}
    public float getVelocityY() {return velocityY;}
    public float getLifetime() {return lifetime;}
}





















