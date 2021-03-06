package corners;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class RobotPlayer {

	public static boolean moveToCorner(MapLocation teamCom, MapLocation enemyCom, RobotController rc)
			throws GameActionException {

		if (!rc.isCoreReady())
			return true;

		Direction d = enemyCom.directionTo(teamCom);
		if (!d.isDiagonal()){
			Direction rl = d.rotateLeft();
			Direction rr = d.rotateRight();
			if (enemyCom.distanceSquaredTo(teamCom.add(rl)) > enemyCom.distanceSquaredTo(teamCom.add(rr)))
				d = rl;
			else
				d = rr;
		}
		
		MapLocation target = rc.getLocation().add(d);
		if (rc.canMove(d)) {
			rc.move(d);
			System.out.println("\n First move worked");
			return true;
		}

		RobotInfo[] info = rc.senseNearbyRobots(1);
		for (RobotInfo i : info)
			if (i.location.equals(target)) {
				System.out.println("\t Something blocking");
				return true;
			}

		Direction rl = d.rotateLeft();
		Direction rr = d.rotateRight();
		if (rc.canMove(rl))
			d = rl;
		else
			d = rr;
		
		target = rc.getLocation().add(d);
		System.out.println("\t New direction determined");
		
		if (rc.canMove(d)) {
			rc.move(d);
			return true;
		}
		
		for (RobotInfo i : info)
			if (i.location.equals(target)) {
				return true;
			}
		
		
		System.out.println("\t Reached corner");
		return false;
	}

	public static void run(RobotController rc) {

		MapLocation enemyCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam().opponent()));
		MapLocation teamCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));

		try {
			while (moveToCorner(teamCom, enemyCom, rc))
				Clock.yield();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
