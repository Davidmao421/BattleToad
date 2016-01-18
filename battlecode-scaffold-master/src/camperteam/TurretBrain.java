package camperteam;

import battlecode.common.*;

public class TurretBrain implements Brain {

	public void initialize(RobotController rc) {

	}

	public void attack(RobotController rc, MapLocation hostile) throws GameActionException {
		if (rc.getType() == RobotType.TTM)
			rc.unpack();
		
		if (hostile == null)
			return;
		if (rc.canAttackLocation(hostile) && rc.isCoreReady())
			rc.attackLocation(hostile);
	}

	public void attack(RobotController rc, RobotInfo hostile) throws GameActionException {
		attack(rc, hostile.location);
		return;
	}

	public boolean attackNearby(RobotController rc) throws GameActionException {
		RobotInfo[] hostiles = rc.senseHostileRobots(rc.getLocation(), -1);
		RobotInfo target = Statics.closestRobot(rc.getLocation(), hostiles, GameConstants.TURRET_MINIMUM_RANGE);
		// This if statement will need to get much more difficult
		if (target != null) {
			attack(rc, target);
			return true;
		}
		return false;
	}

	public void move(RobotController rc, Direction d) throws GameActionException {
		if (rc.getType() == RobotType.TURRET){
			rc.pack();
			return;
		}
		
		if (rc.canMove(d) && rc.isCoreReady())
			rc.move(d);
	}

	public void attackSpecific(RobotController rc, MapLocation target) throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(target);
		if (dist < rc.getType().attackRadiusSquared) {
			if (dist > GameConstants.TURRET_MINIMUM_RANGE) {
				if (rc.canAttackLocation(target) && rc.isCoreReady()) {
					rc.attackLocation(target);
					return;
				}
			} else {
				move(rc, target.directionTo(rc.getLocation()));
			}
		} else {
			move(rc, rc.getLocation().directionTo(target));
		}
	}

	public void runTurn(RobotController rc) throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();

		MapLocation target = null;
		int priority = 0;

		for (Signal s : signals) {
			switch (SignalEncoder.getPacketType(s)) {
			case ATTACK_ENEMY:
				break;
			case CHANGE_SCHEME:
				break;
			case DEAD:
				break;
			case ECHO:
				break;
			case LOCAL_ATTACK:
				break;
			case NEUTRAL_ROBOT:
				break;
			case NEW_ROBOT:
				break;
			case OTHER:
				break;
			case PANIC:
				break;
			case PANIC_OVER:
				break;
			case PARTS_CACHE:
				break;
			default:
				break;
			}
		}

		if (priority < 2) {
			if (attackNearby(rc)) {
			} else
				attackSpecific(rc, target);
		} else {
			attackSpecific(rc, target);
		}

	}

	public void run(RobotController rc) {
		try {
			initialize(rc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			Clock.yield();
			if (!rc.isCoreReady())
				continue;
			try {
				runTurn(rc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
