package camperteam;

import java.util.*;
import battlecode.common.*;
import camperteam.SimpleEncoder.MessageType;

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
	private int radiusInc;
	private boolean space;
	// private List<MapLocation> knownEnemyArchons;

	private Map<Integer, RobotInfo> robots;

	private boolean isLeader;

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

	private boolean circleOfHealing(RobotController rc) throws GameActionException {
		RobotInfo[] friends = rc.senseNearbyRobots(-1, rc.getTeam());
		ArrayList<RobotInfo> targets = new ArrayList<RobotInfo>();
		for (RobotInfo r : friends) {
			if (r.type != RobotType.ARCHON) {
				if (r.location.distanceSquaredTo(rc.getLocation()) <= rc.getType().attackRadiusSquared) {
					if (r.health < r.maxHealth) {
						targets.add(r);
					}
				}
			}
		}

		if (!targets.isEmpty()) {
			RobotInfo triage = targets.get(0);
			for (RobotInfo r : targets) {
				if (r.health < triage.health) {
					triage = r;
				}
			}
			rc.repair(triage.location);
			return true;
		}
		return false;
	}

	private void sendRadiusSignal(RobotController rc) throws GameActionException {
		rc.broadcastMessageSignal(SimpleEncoder.encodeType(MessageType.RADIUS), (int) radius, BROADCAST_RANGE);
	}

	private void increaseRadius(RobotController rc) throws GameActionException {
		radius += radiusInc;
		radiusInc += 2;
		sendRadiusSignal(rc);
	}

	private boolean hasSpace(RobotController rc) throws GameActionException {
		for (Direction d : Statics.directions) {
			MapLocation other = rc.getLocation().add(d);
			if (rc.onTheMap(other) && rc.senseRubble(other) < GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
				if (rc.senseRobotAtLocation(other) != null && rc.senseRobotAtLocation(other).type == RobotType.TTM) {
					return true;
				}
				if (!rc.isLocationOccupied(other)) {
					return true;
				}
			}
		}
		return false;

	}

	private boolean digNearby(RobotController rc) throws GameActionException {
		int k = (int) (Math.random() * 8);
		for (int i = 0; i < 8; i++) {
			MapLocation loc = rc.getLocation().add(Statics.directions[(k + i + 8) % 8]);
			if (rc.senseRubble(loc) > GameConstants.RUBBLE_SLOW_THRESH && rc.onTheMap(loc)) {
				rc.clearRubble(Statics.directions[i]);
				return true;
			}
		}
		return false;
	}

	private void group() throws GameActionException {
		if (isLeader) {// leader
			if (rc.getLocation().isAdjacentTo(lastLoc)) {
				if (rc.senseRubble(lastLoc) > GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
					rc.clearRubble(rc.getLocation().directionTo(lastLoc));
				} else {
					Statics.moveTo(rc.getLocation().directionTo(lastLoc), rc);
				}
			} else {
				Statics.moveTo(rc.getLocation().directionTo(lastLoc), rc);
			}
		} else {// not leader
			Statics.moveTo(rc.getLocation().directionTo(lastLoc), rc);
		}
	}

	private void cluster() throws GameActionException {
		circleOfHealing(rc);
		if (isLeader) {// leader
			if (rc.isCoreReady()) {
				if (rc.hasBuildRequirements(RobotType.TURRET)) {
					if (hasSpace(rc)) {
						space = true;
						buildRobot(RobotType.TURRET);
						sendRadiusSignal(rc);
					} else {
						if (space) {
							increaseRadius(rc);
							space = false;
						}
					}
				} else {
					//sendRadiusSignal(rc);
				}
			}
		} else {// not leader
			if (rc.isCoreReady() && !digNearby(rc)) {
				if (!rc.isCoreReady()) {
					return;
				}
				for (int i = 0; i < 8; i++) {// shuffle
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

	private void determineRoutine() {
		if (isLeader) {
			if (!rc.getLocation().equals(lastLoc)) {
				setRoutine(Routine.GROUP);
			} else {
				setRoutine(Routine.CLUSTER);
			}
		} else {
			if (lastLoc.distanceSquaredTo(rc.getLocation()) > 9) {
				setRoutine(Routine.GROUP);
			} else {
				setRoutine(Routine.CLUSTER);
			}
		}

	}

	private void setRoutine(Routine r) {
		last = current;
		current = r;
	}

	private void runTurn() throws GameActionException {
		if (!rc.isCoreReady()) {
			return;
		}
		if (rc.getRoundNum() == 1) {// select leader
			Signal[] incoming = rc.emptySignalQueue();
			rc.setIndicatorString(3, "" + incoming.length + " messages");
			rc.broadcastSignal(BROADCAST_RANGE * 30);
			isLeader = true;
			for (Signal s : incoming) {
				if (s.getTeam() == rc.getTeam()) {
					isLeader = false;
					break;
				}
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
			cluster();
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
		rc.setIndicatorString(2, "leader" + isLeader);
	}

	private void initialize() throws GameActionException {
		lastLoc = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		current = Routine.GROUP;
		robots = new TreeMap<Integer, RobotInfo>();
		radius = 8;
		radiusInc = 7;
		space = true;
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