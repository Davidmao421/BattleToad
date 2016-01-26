package corners;

import java.util.LinkedList;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class RobotPlayer {

	public static boolean moveToCorner(MapLocation teamCom, MapLocation enemyCom, RobotController rc)
			throws GameActionException {

		if (!rc.isCoreReady())
			return true;

		Direction d = enemyCom.directionTo(teamCom);
		if (!d.isDiagonal()) {
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

	public static void archon(RobotController rc) {
		MapLocation enemyCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam().opponent()));
		MapLocation teamCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));

		try {
			while (moveToCorner(teamCom, enemyCom, rc))
				Clock.yield();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static Signal[] filterBasic(Signal[] signals) {
		LinkedList<Signal> filtered = new LinkedList<Signal>();
		for (Signal s : signals)
			if (s.getMessage() == null)
				filtered.add(s);
		return filtered.toArray(new Signal[0]);
	}

	static MapLocation getHome(Signal[] signals) {
		for (Signal s : signals)
			if (s.getMessage() != null && SignalEncoder.getPacketType(s) == PacketType.HOME)
				return s.getLocation();
		return null;
	}

	static boolean guardClear = false;

	public static void guard(RobotController rc) throws GameActionException {
		if (!rc.isCoreReady())
			return;

		Signal[] q = rc.emptySignalQueue();
		guardClear = filterBasic(q).length > 10;
		if (!guardClear) {
			MapLocation home = getHome(q);
			if (home==null){
				System.out.println("NO HOME");
				return;
			}
			
			MapLocation target = rc.getLocation().add(home.directionTo(rc.getLocation()));
			if (home.distanceSquaredTo(target) < RobotType.GUARD.sensorRadiusSquared
					&& rc.canMove(rc.getLocation().directionTo(target))) {
				rc.move(rc.getLocation().directionTo(target));
				return;
			}
		}
	}

	public static void run(RobotController rc) {

		switch (rc.getType()) {
		case ARCHON:
			archon(rc);
			break;
		case BIGZOMBIE:
			break;
		case FASTZOMBIE:
			break;
		case GUARD:
			break;
		case RANGEDZOMBIE:
			break;
		case SCOUT:
			break;
		case SOLDIER:
			break;
		case STANDARDZOMBIE:
			break;
		case TTM:
			break;
		case TURRET:
			break;
		case VIPER:
			break;
		case ZOMBIEDEN:
			break;
		default:
			break;

		}

	}
}
