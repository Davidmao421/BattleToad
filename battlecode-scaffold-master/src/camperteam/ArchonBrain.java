package camperteam;

import java.util.*;
import battlecode.common.*;

public class ArchonBrain implements Brain {

	private enum Routine {
		CLUSTER, GROUP, NONE;
	}

	private Routine last;
	private Routine current;
	private static final int BROADCAST_RANGE = 70;
	private RobotController rc;
	private MapLocation lastLoc;
	private double radius;
	// private List<MapLocation> knownEnemyArchons;

	private Map<Integer, RobotInfo> robots;

	int panicRobot = -1;
	int leader;
	boolean isLeader;

	private void setRoutine(Routine r) {
		last = current;
		current = r;
	}

	private boolean canBuild(RobotType type, Direction dir) {
		RobotInfo[] robots = rc.senseNearbyRobots(1);
		Set<MapLocation> locs = new HashSet<>();
		for (RobotInfo i : robots)
			locs.add(i.location);
		return rc.canBuild(dir, type)
				&& rc.senseRubble(rc.getLocation().add(dir)) < GameConstants.RUBBLE_OBSTRUCTION_THRESH
				&& !locs.contains(rc.getLocation().add(dir));
	}

	private boolean buildRobot(RobotType type) throws GameActionException {
		ArrayList<Direction> list = new ArrayList<>(Arrays.asList(Statics.directions));
		while (list.size() > 0) {
			Direction d = list.remove((int) (Math.random() * list.size()));
			if (canBuild(type, d)) {
				try {
					rc.build(d, type);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	private void initialize() throws GameActionException {
		lastLoc = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		current = Routine.GROUP;
		robots = new TreeMap<Integer, RobotInfo>();
		radius = 2;
	}

	private void buildTurretCluster() throws GameActionException {
		if (isLeader) {
			if (rc.hasBuildRequirements(RobotType.TURRET)) {
				if (buildRobot(RobotType.TURRET)) {
					radius += 0.5;
				} else {
					rc.broadcastMessageSignal((int) radius, 0, BROADCAST_RANGE);
				}
			} else {
				rc.broadcastMessageSignal((int) radius, 0, BROADCAST_RANGE);
			}
			if (rc.isCoreReady()) {
				rc.broadcastMessageSignal((int) radius, 0, BROADCAST_RANGE);
			}
		} else {
			if (!digNearby(rc)) {
				for (int i = 0; i < 8; i++) {
					Direction dir = Statics.directions[(i + 8) % 8];
					MapLocation loc = rc.getLocation().add(dir);
					if (rc.canMove(dir)) {
						if (loc.distanceSquaredTo(lastLoc) <= 2) {
							rc.move(dir);
							return;
						}
					}
				}
			}
		}
	}

	private static boolean digNearby(RobotController rc) throws GameActionException {
		int k = (int) (Math.random() * 8);
		for (int i = 0; i < 8; i++) {
			MapLocation loc = rc.getLocation().add(Statics.directions[(i + k) % 8]);
			if (rc.senseRubble(loc) > GameConstants.RUBBLE_SLOW_THRESH && rc.onTheMap(loc)) {
				rc.clearRubble(Statics.directions[(i + k) % 8]);
				return true;
			}
		}
		return false;
	}

	private void group() throws GameActionException {
		if (isLeader) {
			Statics.moveTo(rc.getLocation().directionTo(lastLoc), rc);
		} else {
			if (rc.getLocation().equals(lastLoc)) {
				for (int i = 0; i < 8; i++) {
					Direction dir = Statics.directions[i];
					if (rc.canMove(dir)) {
						rc.move(dir);
						return;
					}
				}
				digNearby(rc);
			} else {
				Statics.moveTo(rc.getLocation().directionTo(lastLoc), rc);
			}
		}
	}

	Direction _moveDirection;

	private void determineRoutine() {
		if (isLeader) {
			if (!rc.getLocation().equals(lastLoc)) {
				setRoutine(Routine.GROUP);
			} else {
				setRoutine(Routine.CLUSTER);
			}
		} else {
			if (lastLoc.distanceSquaredTo(rc.getLocation()) > 2) {
				setRoutine(Routine.GROUP);
			} else {
				setRoutine(Routine.CLUSTER);
			}
		}

	}

	private void runTurn() throws GameActionException {
		if (!rc.isCoreReady()) {
			return;
		}
		if (rc.getRoundNum() == 1) {
			Signal[] incoming = rc.emptySignalQueue();
			rc.setIndicatorString(2, "" + incoming.length + " messages");
			rc.broadcastSignal(BROADCAST_RANGE * 30);
			if (incoming.length == 0) {
				isLeader = true;
			} else {
				isLeader = false;
			}
		}
		determineRoutine();
		switch (current) {
		case NONE:
			runTurn();
			return;
		case GROUP:
			group();
			break;
		case CLUSTER:
			buildTurretCluster();
			break;
		default:
			break;
		}

		// DEBUG
		String s = "";
		switch (current) {
		case NONE:
			s = "none";
			break;
		case CLUSTER:
			s = "turret cluster";
			break;
		case GROUP:
			s = "group";
			break;
		default:
			s = "fuck";
			break;

		}
		rc.setIndicatorString(1, s);
		// System.out.println(s);
	}

	@Override
	public void run(RobotController rcI) {
		rc = rcI;
		try {
			initialize();
		} catch (GameActionException e1) {
			e1.printStackTrace();
		}

		while (true) {
			Clock.yield();
			try {
				runTurn();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}

}