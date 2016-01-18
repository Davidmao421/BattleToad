package camperteam;

import java.util.*;
import battlecode.common.*;

public class ArchonBrain implements Brain {

	private enum Routine {
		CLUSTER, GROUP, NONE;
	}

	private int turns;
	private Routine last;
	private Routine current;
	private static final int BROADCAST_RANGE = 70;
	private RobotController rc;
	private MapLocation lastLoc;
	private MapLocation[] archonLocs;
	private List<MapLocation> knownParts;
	// private List<MapLocation> knownEnemyArchons;
	private List<MapLocation> knownNeutralRobots;

	private Map<Integer, RobotInfo> robots;

	int panicRobot = -1;
	int head;

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
		turns = 0;
		lastLoc = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		current = Routine.GROUP;

		knownNeutralRobots = new LinkedList<MapLocation>();
		knownParts = new LinkedList<MapLocation>();
		robots = new TreeMap<Integer, RobotInfo>();
	}

	private void buildTurretCluster() throws GameActionException {
		if (rc.getID() == head) {
			if (rc.hasBuildRequirements(RobotType.TURRET)) {
				buildRobot(RobotType.TURRET);
			}
		}
	}

	private void group() throws GameActionException {
		Statics.moveTo(rc.getLocation().directionTo(lastLoc), rc);
	}

	Direction _moveDirection;

	private void determineRoutine() {
		setRoutine(Routine.GROUP);
	}

	private void runTurn() throws GameActionException {
		if (!rc.isCoreReady()) {
			turns--;
			return;
		}
		if (rc.getRoundNum() == 0) {
			Signal[] incoming = rc.emptySignalQueue();
			rc.setIndicatorString(1, "" + incoming.length + " messages");
			rc.broadcastSignal(BROADCAST_RANGE * 2);
		}
		switch (current) {
		case NONE:
			determineRoutine();
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
			turns++;
			try {
				runTurn();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}

}