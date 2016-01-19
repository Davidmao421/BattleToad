package team094;

import java.util.ArrayList;

import battlecode.common.*;
import scala.Int;

public class SoldierBrain implements Brain {

	private static MapLocation lastArcLoc;
	RobotController rc;

	static MapLocation target;
	static int priority;

	@Override
	public void run(RobotController inRc) {

		rc = inRc;
		initialize(rc);
		while (true) {
			try {
				runTurn(rc);
				Clock.yield();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void initialize(RobotController rc) {
		target = null;
		priority = -1;
	}

	public static boolean attack(RobotController rc) throws GameActionException {
		RobotInfo[] hostiles = rc.senseHostileRobots(rc.getLocation(), -1);
		RobotInfo target = CompareStuff.moveAwayFrom(hostiles, rc.getLocation());
		return target == null ? false : attack(rc, target.location);
	}

	public static boolean attack(RobotController rc, MapLocation target) throws GameActionException {
		if (!rc.isCoreReady() || !rc.isWeaponReady())
			return false;
		if (rc.canAttackLocation(target)) {
			rc.attackLocation(target);
			return true;
		}
		return Statics.moveTo(rc.getLocation().directionTo(target), rc);
	}

	public static void move(RobotController rc) throws GameActionException {
		RobotInfo[] nearby = rc.senseNearbyRobots();
		ArrayList<MapLocation> arcLoc = new ArrayList<MapLocation>();
		for (RobotInfo r : nearby) {
			if (r.team == rc.getTeam() && r.type == RobotType.ARCHON) {
				arcLoc.add(r.location);
			}
		}
		int nearestArc = 0;
		int shortestDistance = Int.MaxValue();
		if (arcLoc.size() != 0) {
			for (int i = 0; i < arcLoc.size(); i++) {
				int distance = rc.getLocation().distanceSquaredTo(arcLoc.get(i));
				if (distance <= shortestDistance) {
					shortestDistance = distance;
					nearestArc = i;
				}
			}
			lastArcLoc = arcLoc.get(nearestArc);
			Direction dir = arcLoc.get(nearestArc).directionTo(rc.getLocation());
			if (shortestDistance < 4) {
				Statics.moveTo(dir, rc);
			} else if (shortestDistance >= 9) {
				boolean t = Statics.moveTo(dir.opposite(), rc);
			} else {
				if (digToWin(rc)) {
					return;
				}
				if (digNearby(rc)) {
					return;
				}
				boolean left = rc.canMove(dir.rotateLeft().rotateLeft());
				boolean right = rc.canMove(dir.rotateRight().rotateRight());
				if (left && right) {
					if (Math.random() < 0.5d) {
						rc.move(dir.rotateLeft().rotateLeft());
					} else {
						rc.move(dir.rotateRight().rotateRight());
					}
				}
				if (left && !right) {
					rc.move(dir.rotateLeft().rotateLeft());
				}
				if (right && !left) {
					rc.move(dir.rotateRight().rotateRight());
				}

			}
		} else { // No Archons Seen
			if (lastArcLoc != null) {
				Direction dir = rc.getLocation().directionTo(lastArcLoc);
				Statics.moveTo(dir, rc);
				if (rc.getLocation().equals(lastArcLoc)) {
					lastArcLoc = null;
				}
			} else {
				nearby = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
				if (nearby.length != 0) {
					MapLocation[] locs = new MapLocation[nearby.length];
					int index = 0;
					for (RobotInfo r : nearby) {
						locs[index] = r.location;
						index++;
					}
					Direction dir = rc.getLocation().directionTo(Statics.com(locs));
					Statics.moveTo(dir, rc);
				}
			}
		}
	}

	private static boolean digNearby(RobotController rc) throws GameActionException {
		int k = (int) (Math.random() * 8);
		for (int i = 0; i < 8; i++) {
			MapLocation loc = rc.getLocation().add(Statics.directions[(i + k) % 8]);
			if (rc.senseRubble(loc) > GameConstants.RUBBLE_SLOW_THRESH && rc.onTheMap(loc)) {
				rc.clearRubble(Statics.directions[i]);
				return true;
			}
		}
		return false;
	}

	private static boolean digToWin(RobotController rc) throws GameActionException {
		MapLocation[] parts = rc.sensePartLocations(-1);
		RobotInfo[] neutrals = rc.senseNearbyRobots(-1, Team.NEUTRAL);

		if (neutrals.length != 0) {
			MapLocation[] locs = new MapLocation[neutrals.length];
			for (int i = 0; i < neutrals.length; i++) {
				locs[i] = neutrals[i].location;
			}
			MapLocation loc = Statics.closestLoc(rc.getLocation(), locs);
			Direction dir = rc.getLocation().directionTo(loc);
			for (int i : new int[] { 0, 1, -1 }) { // check 3 directions
				Direction candidateDir = Statics.directions[(dir.ordinal() + 8 + i) % 8];
				if (rc.senseRubble(rc.getLocation().add(candidateDir)) > GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
					rc.clearRubble(candidateDir);
					return true;
				}
			}
			Statics.moveTo(dir, rc);
			return true;
		}
		if (parts.length != 0) { // are there parts
			MapLocation loc = Statics.closestLoc(rc.getLocation(), parts);
			if (rc.senseRubble(loc) > GameConstants.RUBBLE_OBSTRUCTION_THRESH) { // obstructed
				Direction dir = rc.getLocation().directionTo(loc);
				for (int i : new int[] { 0, 1, -1 }) { // check 3 directions
					Direction candidateDir = Statics.directions[(dir.ordinal() + 8 + i) % 8];
					MapLocation digLoc = rc.getLocation().add(candidateDir);
					if (rc.senseRubble(digLoc) > GameConstants.RUBBLE_OBSTRUCTION_THRESH && rc.onTheMap(digLoc)) {
						rc.clearRubble(candidateDir);
						return true;
					}
				}
				Statics.moveTo(dir, rc);
				return true;
			}
		}
		return false;
	}

	public static void processSignals(RobotController rc) throws GameActionException {
		Signal[] received = rc.emptySignalQueue();

		for (Signal s : received) {
			if (s.getMessage() == null){
				//What should we do with basic packets?
				continue;
			}
			switch (SignalEncoder.getPacketType(s)) {
			case ATTACK_ENEMY:
				if (priority < 2)
					priority = 2;
				Signal e = SignalEncoder.decodeAttackEnemy(s);
				target = e.getLocation();
				break;
			case CHANGE_SCHEME:
				// TODO:
				break;
			case DEAD:
				// Only archon cares
				break;
			case ECHO:
				// This is stupid
				break;
			case LOCAL_ATTACK:
				if (priority <= 1) {
					priority = 1;
					// TODO:
				}
				break;
			case NEUTRAL_ROBOT:
				// Only archon cares
				break;
			case NEW_ROBOT:
				// Only archon cares
				break;
			case OTHER:
				break;
			case PANIC:
				if (priority > 3)
					break;
				MapLocation[] locs = SignalEncoder.decodePanic(s);
				target = Statics.closestLoc(rc.getLocation(), locs);
				priority = 3;
				break;
			case PANIC_OVER:
				target = null;
				priority = -1;
				break;
			case PARTS_CACHE:
				// Only archon cares
				break;
			default:
				break;
			}
		}
	}

	public static void runTurn(RobotController rc) throws GameActionException {
		processSignals(rc);
		if (target != null && priority > 2) {
			attack(rc, target);
		} else if (!attack(rc) && rc.isCoreReady()) {
			move(rc);
		}

	}

}