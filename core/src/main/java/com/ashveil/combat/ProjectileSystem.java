package com.ashveil.combat;

import com.ashveil.collision.CollidableObject;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.collision.MovementType;
import com.ashveil.entities.Player;
import com.ashveil.objects.DestructibleObject;
import com.ashveil.objects.DestructibleObjectType;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class ProjectileSystem {
    private final List<Projectile> projectiles;
    private final Player player;
    private final CollisionSystem collisionSystem;

    public ProjectileSystem(Player player, CollisionSystem collisionSystem) {
        this.player = player;
        this.collisionSystem = collisionSystem;
        projectiles = new ArrayList<>();
    }

    public void update(float delta){
        if (projectiles.isEmpty()) return;
        for (Projectile projectile : projectiles){
            if (!projectile.isActive()) continue;
            projectile.update(delta);
            if (!projectile.isActive()) continue;
            handleCollision(projectile);
        }
        projectiles.removeIf(projectile -> !projectile.isActive());
    }

    public void spawnProjectile(float x, float y, float dirX, float dirY, float speed, int damage, float lifetime){
        projectiles.add(new Projectile(x, y, dirX, dirY, speed, damage, lifetime));
    }

    private void handleCollision(Projectile projectile){
        if (projectile.getCollisionBounds().overlaps(player.getCollisionBounds())){
            player.takeDamage(projectile.getDamage());
            projectile.deactivate();
            return;
        }

        Rectangle bounds = projectile.getCollisionBounds();
        CollidableObject object = collisionSystem.getBlockingObject(bounds.x, bounds.y, bounds.width, bounds.height, MovementType.GROUND);

        if (object != null) {
            if (object instanceof DestructibleObject destructibleObject){
                if (destructibleObject.getType() == DestructibleObjectType.FENCE){
                    destructibleObject.receiveHit(projectile.getDamage());
                }
            }
            projectile.deactivate();
            return;
        }

        if (collisionSystem.isBlocked(bounds.x, bounds.y, bounds.width, bounds.height, MovementType.GROUND)) projectile.deactivate();
    }

    public List<Projectile> getProjectiles() {return projectiles;}
}












