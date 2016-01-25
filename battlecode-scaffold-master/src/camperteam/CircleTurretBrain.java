package camperteam;

import battlecode.common.*;

public class CircleTurretBrain implements Brain {

	private int radius;
	private MapLocation center;
	private double previousHealth;
	private int timer;

	public void initialize(RobotController rc) {
		timer = 0;
		previousHealth = rc.getHealth();
		radius = 5;
		center = Statics.closestRobot(rc.getLocation(), rc.senseNearbyRobots(-1, rc.getTeam())).location;
		rc.setIndicatorDot(center, 128, 128, 128);
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
		if (rc.getType() == RobotType.TURRET) {
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
		rc.setIndicatorString(1, "" + radius + ":Radius");
		Signal[] signals = rc.emptySignalQueue();

		for (Signal s : signals) {
			if (s.getTeam() == rc.getTeam()) {
				center = s.getLocation();
				radius = s.getMessage()[0];
			}

		}
		boolean shouldMoveIn = shouldMoveIn(rc), shouldMoveOut = shouldMoveOut(rc);
		if (shouldMoveIn || shouldMoveOut) {
			if (!enemiesInSight(rc)) {
				if (rc.getType() == RobotType.TURRET) {
					timer = 20;
					rc.pack();
				}
			}
		}
		if (rc.getType() == RobotType.TTM && rc.isCoreReady()) {
			if (timer == 0 || enemiesInSight(rc)) {
				rc.unpack();
			} else if(shouldMoveOut) {
				forward(rc);
			} else if (shouldMoveIn) {
				reverse(rc);
			} else if (rc.getType() == RobotType.TTM && rc.isCoreReady()) {
				rc.unpack();
			}
		}
		attackNearby(rc);
	}

	private boolean enemiesInSight(RobotController rc) {
		RobotInfo[] robots = rc.senseNearbyRobots();
		for (RobotInfo robot:robots) {
			if (robot.team!=rc.getTeam()) {
				return true;
			}
		}
		return false;
	}

	private boolean shouldMoveOut(RobotController rc) throws GameActionException {
		MapLocation loc;
		Direction dir = rc.getLocation().directionTo(center).opposite();
		for (int i = -1; i <= 1; i++) {
			loc = rc.getLocation().add(Statics.directions[(dir.ordinal() + i + 8) % 8]);
			if (rc.senseRobotAtLocation(
					rc.getLocation().add(Statics.directions[(dir.ordinal() + i + 8) % 8])) == null) {
				int r = loc.distanceSquaredTo(center);
				if (r < radius) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean shouldMoveIn(RobotController rc) throws GameActionException {
		MapLocation loc;
		Direction dir = rc.getLocation().directionTo(center);
		for (int i = -1; i <= 1; i++) {
			loc = rc.getLocation().add(Statics.directions[(dir.ordinal() + i + 8) % 8]);
			if (rc.senseRobotAtLocation(
					rc.getLocation().add(Statics.directions[(dir.ordinal() + i + 8) % 8])) == null) {
				int r = loc.distanceSquaredTo(center);
				if (r > radius) {
					return true;
				}
			}
		}
		return false;
	}

	private void forward(RobotController rc) throws GameActionException {
		if (rc.isCoreReady()) {
			Statics.moveTo(rc.getLocation().directionTo(center).opposite(), rc);
		}
	}

	private void reverse(RobotController rc) throws GameActionException {
		if (rc.isCoreReady()) {
			Statics.moveTo(rc.getLocation().directionTo(center), rc);
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
				if (timer > 0) {
					timer--;
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}
	}

}
