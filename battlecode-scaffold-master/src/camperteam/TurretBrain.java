package camperteam;

import battlecode.common.*;
import camperteam.SimpleEncoder.MessageType;

public class TurretBrain implements Brain {

	private int radius;
	private MapLocation center;
	private double previousHealth;
	private int timer;
	private MapLocation attackLoc;

	private void initialize(RobotController rc) {
		timer = 0;
		previousHealth = rc.getHealth();
		radius = 5;
		center = Statics.closestRobot(rc.getLocation(), rc.senseNearbyRobots(-1, rc.getTeam())).location;
		rc.setIndicatorDot(center, 128, 128, 128);
	}

	private void attack(RobotController rc, MapLocation hostile) throws GameActionException {
		if (rc.getType() == RobotType.TTM)
			rc.unpack();

		if (hostile == null)
			return;
		if (rc.canAttackLocation(hostile) && rc.isCoreReady())
			rc.attackLocation(hostile);
	}

	private void attack(RobotController rc, RobotInfo hostile) throws GameActionException {
		attack(rc, hostile.location);
		return;
	}

	private boolean attackNearby(RobotController rc) throws GameActionException {
		RobotInfo[] hostiles = rc.senseHostileRobots(rc.getLocation(), -1);
		RobotInfo target = Statics.closestRobot(rc.getLocation(), hostiles, GameConstants.TURRET_MINIMUM_RANGE);
		// This if statement will need to get much more difficult
		if (target != null && rc.isCoreReady() && rc.isWeaponReady()) {
			attack(rc, target);
			return true;
		}
		return false;
	}

	private boolean attackSpecific(RobotController rc, MapLocation target) throws GameActionException {
		if (rc.canAttackLocation(target) && rc.isCoreReady() && rc.isWeaponReady()) {
			rc.attackLocation(target);
			return true;
		}
		return false;
	}

	private boolean enemiesInSight(RobotController rc) {
		RobotInfo[] robots = rc.senseNearbyRobots();
		for (RobotInfo robot : robots) {
			if (robot.team != rc.getTeam()) {
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

	private void processSignals(RobotController rc) throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();

		for (Signal s : signals) {
			if (s.getTeam() == rc.getTeam()) {
				MessageType type = SimpleEncoder.decodeType(s.getMessage()[0]);
				switch (type) {
				case ATTACK:
					attackLoc = SimpleEncoder.decodeLocation(s.getMessage()[1]);
					break;
				case CENTERHERE:
					break;
				case LEADERCHECK:
					break;
				case MOVETO:
					break;
				case NEUTRALARCHON:
					break;
				case RADIUS:
					center = s.getLocation();
					radius = s.getMessage()[1];
					break;
				case ZOMBIEDEN:
					break;
				default:
					break;
				}
			}
		}
	}

	private void runTurn(RobotController rc) throws GameActionException {
		rc.setIndicatorString(1, "" + radius + ":Radius");
		processSignals(rc);
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
			} else if (shouldMoveOut) {
				forward(rc);
			} else if (shouldMoveIn) {
				reverse(rc);
			} else if (rc.getType() == RobotType.TTM && rc.isCoreReady()) {
				rc.unpack();
			}
		}
		if (attackLoc != null) {
			if (attackSpecific(rc, attackLoc)) {
				attackLoc = null;
				return;
			}
			attackLoc = null;
		}
		attackNearby(rc);
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
