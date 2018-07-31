package com.valensas.kyc_android.identitysigniture

import com.valensas.kyc_android.identitysigniture.IdentitySignitureSpinnerCircle.Direction.DOWN
import com.valensas.kyc_android.identitysigniture.IdentitySignitureSpinnerCircle.Direction.UP

/**
 * Created by Zahit on 31-Jul-18.
 */
class IdentitySignitureSpinnerCircle(
        val radius: Float = 0F,
        var x: Float = 0F,
        var y: Float = 0F,
        private var direction: Direction,
        private var bottom: Float = 0F
) {
    enum class Direction {
        UP,
        DOWN
    }

    fun update() {
        when (direction) {
            UP -> {
                if (this.y - COLLISION_RADIUS - speed <= 0F) {
                    this.direction = DOWN
                } else {
                    this.y -= speed
                }
            }
            DOWN -> {
                if (this.y + COLLISION_RADIUS + speed >= bottom) {
                    this.direction = UP
                } else {
                    this.y += speed
                }
            }
        }
    }

    companion object {
        val speed = 1F
        var COLLISION_RADIUS = 0F
    }
}