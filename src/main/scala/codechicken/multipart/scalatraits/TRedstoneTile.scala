package codechicken.multipart.scalatraits

import codechicken.lib.vec.Rotation._
import codechicken.multipart.PartMap._
import codechicken.multipart.RedstoneInteractions._
import codechicken.multipart._

/**
 * Mixin trait implementation for IRedstonePart
 * Provides and overrides various redstone functions.
 */
trait TRedstoneTile extends TileMultipart with IRedstoneTile {
    /**
     * Returns the strong (indirect) power being emitted fr
     */
    override def strongPowerLevel(side: Int): Int = {
        var max = 0
        for (p@(_p: IRedstonePart) <- partList.iterator) {
            val l = p.strongPowerLevel(side)
            if (l > max) max = l
        }
        max
    }

    def openConnections(side: Int): Int = {
        var m = 0x10
        var i = 0
        while (i < 4) {
            if (redstoneConductionE(edgeBetween(side, rotateSide(side & 6, i)))) {
                m |= 1 << i
            }
            i += 1
        }
        m &= redstoneConductionF(side)
        m
    }

    def redstoneConductionF(i: Int) = partMap(i) match {
        case null => 0x1F
        case p => p.asInstanceOf[TFacePart].redstoneConductionMap
    }

    def redstoneConductionE(i: Int) = partMap(i) match {
        case null => true
        case p => p.asInstanceOf[TEdgePart].conductsRedstone
    }

    override def weakPowerLevel(side: Int): Int =
        weakPowerLevel(side, otherConnectionMask(getWorld, getPos, side, true))

    override def canConnectRedstone(side: Int): Boolean =
        (getConnectionMask(side) & otherConnectionMask(getWorld, getPos, side, false)) > 0

    def getConnectionMask(side: Int): Int = {
        val mask = openConnections(side)
        var res = 0
        partList.foreach(p => res |= connectionMask(p, side) & mask)
        res
    }

    def weakPowerLevel(side: Int, mask: Int): Int = {
        val tmask = openConnections(side) & mask
        var max = 0
        partList.foreach(p =>
            if ((connectionMask(p, side) & tmask) > 0) {
                val l = p.asInstanceOf[IRedstonePart].weakPowerLevel(side)
                if (l > max) max = l
            })
        max
    }
}

