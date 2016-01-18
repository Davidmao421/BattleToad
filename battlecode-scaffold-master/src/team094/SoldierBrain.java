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
		int myAttackRange = rc.getType().attackRadiusSquared;

		if (rc.getType().canAttack() && myAttackRange > 0) {
			RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, rc.getTeam().opponent());
			RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(myAttackRange, Team.ZOMBIE);
			RobotInfo away = CompareStuff.moveAwayFrom(zombiesWithinRange, rc.getLocation());
			if (away != null) {
				if (rc.isCoreReady() && rc.canMove(away.location.directionTo(rc.getLocation())))
					Statics.moveTo(CompareStuff.moveAwayFrom(zombiesWithinRange, rc.getLocation()).location
							.directionTo(rc.getLocation()), rc); // TODO:
			} else {
				RobotInfo target = CompareStuff.soldierCompare(enemiesWithinRange, zombiesWithinRange);
				if (target != null) {
					if (rc.isWeaponReady()) {
						rc.attackLocation(target.location);
					}
					return true;
				}
			}
		}
		return false;
	}

	public static void move(RobotController rc) throws GameActionException {
		RobotInfo[] nearby = rc.senseNearbyRobots();
		ArrayList<MapLocation> arcLoc = new ArrayList<MapLocation>();
		for (RobotInfo r : nearby) {
			if (r.type.equals(RobotType.ARCHON)) {
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
				Statics.moveTo(dir.opposite(), rc);
			} else {
				for (int i = 0; i < 8; i++) {
					if (rc.senseRubble(
							rc.getLocation().add(Statics.directions[i])) > GameConstants.RUBBLE_SLOW_THRESH) {
						rc.clearRubble(Statics.directions[i]);
						return;
					}
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

	public static void processSignals(RobotController rc) throws GameActionException {
		Signal[] received = rc.emptySignalQueue();

		for (Signal s : received) {
			switch (SignalEncoder.getPacketType(s)) {
			case ATTACK_ENEMY:
				if (priority < 2)
					// TODO:
					break;
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
				Panic p = SignalEncoder.decodePanic(s);
				target = Statics.closestLoc(rc.getLocation(), p.locs);
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
		if (target != null && priority > 2) {
			
		}
		if (!attack(rc) && rc.isCoreReady()){
			move(rc);
		}

	}

}