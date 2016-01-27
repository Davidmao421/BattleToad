package team094;

import java.util.ArrayList;

import battlecode.common.*;
import team094.SimpleEncoder.MessageType;

public class TurretBrain implements Brain {

	private int radius;
	private MapLocation center;
	private double previousHealth;
	private int timer;
	private MapLocation attackLoc;
	private ArrayList<MapLocation> targets;
	private MapLocation[] startingLocs;
	private MapLocation[] enemyLocs;
	private MapLocation mapCenter;
	private MapLocation leaderLoc;

	private void attack(RobotController rc, MapLocation hostile) throws GameActionException {
		if (rc.getType() == RobotType.TTM)
			return;

		if (hostile == null)
			return;
		if (rc.canAttackLocation(hostile) && rc.isCoreReady() && rc.isWeaponReady())
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
		if (target != null) {
			attack(rc, target);
			return true;
		} else {
			for (RobotInfo r : hostiles) {
				targets.add(r.location);
			}
			if (targets.size() != 0) {
				MapLocation[] temp = new MapLocation[targets.size()];
				int i = 0;
				for (MapLocation r : targets) {
					temp[i] = r;
					i++;
				}
				if (temp != null) {
					MapLocation target2 = Statics.closestRobot(rc.getLocation(), temp,
							GameConstants.TURRET_MINIMUM_RANGE);
					if (target2 != null) {
						attack(rc, target2);
					}
				}
			}
		}
		return false;

	}

	private void move(RobotController rc, Direction d) throws GameActionException {
		if (rc.getType() == RobotType.TURRET) {
			rc.pack();
			return;
		}

		if (rc.canMove(d) && rc.isCoreReady())
			rc.move(d);
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
		Direction dir = rc.getLocation().directionTo(leaderLoc).opposite();
		for (int i = -1; i <= 1; i++) {
			loc = rc.getLocation().add(Statics.directions[(dir.ordinal() + i + 8) % 8]);
			if (rc.senseRobotAtLocation(
					rc.getLocation().add(Statics.directions[(dir.ordinal() + i + 8) % 8])) == null) {
				int r = loc.distanceSquaredTo(leaderLoc);
				if (r < radius) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean shouldMoveIn(RobotController rc) throws GameActionException {
		MapLocation loc;
		Direction dir = rc.getLocation().directionTo(leaderLoc);
		for (int i = -1; i <= 1; i++) {
			loc = rc.getLocation().add(Statics.directions[(dir.ordinal() + i + 8) % 8]);
			if (rc.senseRobotAtLocation(
					rc.getLocation().add(Statics.directions[(dir.ordinal() + i + 8) % 8])) == null) {
				int r = loc.distanceSquaredTo(leaderLoc);
				if (r > radius) {
					return true;
				}
			}
		}
		return false;
	}

	private void forward(RobotController rc) throws GameActionException {
		if (rc.isCoreReady()) {
			Statics.moveTo(rc.getLocation().directionTo(leaderLoc).opposite(), rc);
		}
	}

	private void reverse(RobotController rc) throws GameActionException {
		if (rc.isCoreReady()) {
			Statics.moveTo(rc.getLocation().directionTo(leaderLoc), rc);
		}
	}

	private void processSignals(RobotController rc) throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal s : signals) {
			if (s.getTeam() == rc.getTeam() && s.getMessage() != null) {
				MessageType type = SimpleEncoder.decodeType(s.getMessage()[0]);
				switch (type) {
				case ENEMY:
					MapLocation enemyLoc = SimpleEncoder.decodeLocation(s.getMessage()[1]);
					if (!targets.contains(enemyLoc)) {
						targets.add(enemyLoc);
					}
					break;
				case CENTERHERE:
					leaderLoc = SimpleEncoder.decodeLocation(s.getMessage()[1]);
					break;
				case LEADERCHECK:
					break;
				case MOVETO:
					break;
				case NEUTRALARCHON:
					break;
				case RADIUS:
					leaderLoc = s.getLocation();
					radius = s.getMessage()[1];
					break;
				case ZOMBIEDEN:
					break;
				case TURRETQUORUM:
					rc.broadcastSignal(rc.getLocation().distanceSquaredTo(s.getLocation()));
					break;
				default:
					break;
				}
			}
		}
	}

	public void runTurn(RobotController rc) throws GameActionException {
		targets = new ArrayList<MapLocation>();
		processSignals(rc);
		rc.setIndicatorString(1, "" + radius + ":Radius");
		boolean seeEnemies = enemiesInSight(rc);
		boolean shouldMoveIn = shouldMoveIn(rc), shouldMoveOut = shouldMoveOut(rc);
		if (shouldMoveIn || shouldMoveOut) {
			if (!seeEnemies && rc.isCoreReady()) {
				if (rc.getType() == RobotType.TURRET) {
					rc.pack();
				}
			}
		}
		if (rc.getType() == RobotType.TTM && rc.isCoreReady()) {
			if (seeEnemies) {
				rc.unpack();
			} else if (shouldMoveOut) {
				forward(rc);
			} else if (shouldMoveIn) {
				reverse(rc);
			} else if (rc.getType() == RobotType.TTM && rc.isCoreReady()) {
				rc.unpack();
			}
		}
		attackNearby(rc);
	}

	private void oldRunTurn(RobotController rc) throws GameActionException {
		rc.setIndicatorString(1, "" + radius + ":Radius");
		processSignals(rc);

		if (rc.getType() == RobotType.TTM && rc.isCoreReady()) {
			if (validLocation(rc, rc.getLocation())) {
				rc.unpack();
			} else {

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

	public boolean validLocation(RobotController rc, MapLocation loc) throws GameActionException {
		return (loc.x + loc.y) % 2 == 0 && rc.senseRobotAtLocation(loc) != null
				&& rc.senseRubble(loc) < GameConstants.RUBBLE_OBSTRUCTION_THRESH;
	}

	private void initialize(RobotController rc) throws GameActionException {
		startingLocs = rc.getInitialArchonLocations(rc.getTeam());
		enemyLocs = rc.getInitialArchonLocations(rc.getTeam().opponent());
		MapLocation[] allArchons = new MapLocation[startingLocs.length + enemyLocs.length];
		int i = 0;
		for (MapLocation m : startingLocs) {
			allArchons[i] = m;
			i++;
		}
		for (MapLocation m : enemyLocs) {
			allArchons[i] = m;
			i++;
		}
		mapCenter = Statics.com(allArchons);
		leaderLoc = Statics.farthestLoc(mapCenter, startingLocs);
		timer = 0;
		previousHealth = rc.getHealth();
		radius = 8;
		targets = new ArrayList<MapLocation>();
		rc.pack();
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
