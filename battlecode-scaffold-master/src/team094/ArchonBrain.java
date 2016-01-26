package team094;

import java.util.*;
import battlecode.common.*;
import team094.SimpleEncoder.MessageType;

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
	private MapLocation[] startingLocs;
	private int archonIndex;
	private boolean isScavenger;
	int turns;

	int guardsBuilt;
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

	private void receive() {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal s : signals) {
			if (s.getMessage() == null || s.getTeam() != rc.getTeam())
				continue;
			if (SimpleEncoder.decodeType(s.getMessage()[0]) == SimpleEncoder.MessageType.NEUTRALARCHON)
				knownNeutralRobots.add(SimpleEncoder.decodeLocation(s.getMessage()[1]));
			else if (SimpleEncoder.decodeType(s.getMessage()[0]) == MessageType.PARTSCACHE)
				knownParts.add(SimpleEncoder.decodeLocation(s.getMessage()[1]));
		}
	}

	private void sense() {
		MapLocation[] cache = rc.sensePartLocations(-1);
		for (MapLocation l : cache)
			knownParts.add(l);
		RobotInfo[] infos = rc.senseNearbyRobots(-1, Team.NEUTRAL);
		for (RobotInfo i : infos)
			knownNeutralRobots.add(i.location);
	}
	public void senseEnemies() throws GameActionException {
		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), -1);
		for (RobotInfo r : enemies) {
			messagesSent++;
			if (messagesSent < 20)
				rc.broadcastMessageSignal(SimpleEncoder.encodeType(MessageType.ENEMY),
						SimpleEncoder.encodeLocation(r.location), rc.getType().sensorRadiusSquared);
		}
	}

	int messagesSent;
	private void scavenger() throws GameActionException {
		messagesSent=0;
		sense();
		senseEnemies();
		if (rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared).length > 0) {
			Statics.moveTo(rc.getLocation().directionTo(startingLocs[0]), rc);
		}
		receive();
		scavenge();
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
			if (isScavenger) {
				scavenger();
				return;
			}
			Statics.moveTo(rc.getLocation().directionTo(lastLoc), rc);
		}
	}

	private List<MapLocation> knownParts;
	// private List<MapLocation> knownEnemyArchons;
	private List<MapLocation> knownNeutralRobots;

	private void scavengeNeutrals() throws GameActionException {
		RobotInfo[] sensedNeutralsArr = rc.senseNearbyRobots(-1, Team.NEUTRAL);
		MapLocation[] sensedNeutrals = new MapLocation[sensedNeutralsArr.length];
		for (int i = 0; i < sensedNeutrals.length; i++)
			sensedNeutrals[i] = sensedNeutralsArr[i].location;
		MapLocation[] neutrals = Statics.combineLocs(sensedNeutrals, knownNeutralRobots.toArray(new MapLocation[0]));

		// if neutral robot
		if (neutrals.length != 0) {
			MapLocation[] locs = new MapLocation[neutrals.length];
			int i = 0;
			for (MapLocation r : neutrals) {
				if (r.distanceSquaredTo(rc.getLocation()) < rc.getType().sensorRadiusSquared
						&& !Statics.contains(r, sensedNeutrals)) {
					knownNeutralRobots.remove(r);
					if (knownNeutralRobots.size() + sensedNeutrals.length < 1)
						return;
					continue;
				}
				locs[i] = r;
				if (locs[i].distanceSquaredTo(rc.getLocation()) <= 2) {
					if (rc.isCoreReady())
						rc.activate(locs[i]);
					return;
				}
				i++;
			} // reaches here if cannot activate
			Direction d = rc.getLocation().directionTo(Statics.closestLoc(rc.getLocation(), locs));
			if (rc.isCoreReady() && d != null)
				Statics.moveTo(d, rc);
			return;
		}
	}

	private void handleFreeParts(MapLocation[] sensedParts, MapLocation[] parts, List<MapLocation> freeParts)
			throws GameActionException {
		if (freeParts.size() != 0) {
			MapLocation[] array = new MapLocation[freeParts.size()];
			int index = 0;
			for (MapLocation l : freeParts) {
				array[index] = l;
				index++;
			}
			MapLocation partLoc = Statics.closestLoc(rc.getLocation(), array);
			if (rc.getLocation().distanceSquaredTo(partLoc) < rc.getType().sensorRadiusSquared
					&& !Statics.contains(partLoc, sensedParts)) {
				knownParts.remove(partLoc);
				freeParts.remove(partLoc);
				handleFreeParts(sensedParts, parts, freeParts);
				return;
			}
			Statics.moveTo(rc.getLocation().directionTo(partLoc), rc);
			return;
		}
	}

	private void handleHardParts(MapLocation[] sensedParts, MapLocation[] parts, List<MapLocation> hardParts)
			throws GameActionException {
		if (hardParts.size() != 0) {
			MapLocation[] array = new MapLocation[hardParts.size()];
			int index = 0;
			for (MapLocation l : hardParts) {
				array[index] = l;
				index++;
			}
			MapLocation loc = Statics.closestLoc(rc.getLocation(), array);
			if (rc.getLocation().distanceSquaredTo(loc) < rc.getType().sensorRadiusSquared
					&& !Statics.contains(loc, sensedParts)) {
				knownParts.remove(loc);
				hardParts.remove(loc);
				handleHardParts(sensedParts, parts, hardParts);
				return;
			}

			RobotInfo[] friends = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadiusSquared, rc.getTeam());

			if (friends.length < 5) {
				// setRoutine(Routine.TURRET_CLUSTER);
			} else {
				Statics.moveTo(rc.getLocation().directionTo(loc), rc);
			}
		}
	}

	private void randomlyMove() throws GameActionException {
		if (turns < 150)
			return;
		if (!rc.isCoreReady())
			return;
		if (!buildRobot(RobotType.SCOUT)) {
			Statics.moveTo(rc.getLocation().directionTo(startingLocs[0]), rc);

		} else {
			turns = 0;
		}
	}

	private void scavenge() throws GameActionException {
		MapLocation[] sensedParts = rc.sensePartLocations(-1);
		MapLocation[] parts = new MapLocation[0];
		parts = Statics.combineLocs(sensedParts, knownParts.toArray(new MapLocation[0]));

		scavengeNeutrals();

		// if no parts
		if (parts.length == 0) {
			randomlyMove();
			return;
		}

		// if parts
		ArrayList<MapLocation> freeParts = new ArrayList<MapLocation>();
		ArrayList<MapLocation> hardParts = new ArrayList<MapLocation>();
		for (MapLocation l : parts) {
			if (rc.senseRubble(l) < GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
				freeParts.add(l);
			} else {
				hardParts.add(l);
			}
		}

		// if easy parts
		handleFreeParts(sensedParts, parts, freeParts);

		// if hard parts
		handleHardParts(sensedParts, parts, hardParts);

		// Parts could have already been taken
		if (knownParts.size() + sensedParts.length < 1) {
			randomlyMove();
			return;
		}
	}

	private void cluster() throws GameActionException {
		if (isLeader) {// leader
			if (rc.isCoreReady()) {
				if (turns%150==0){
					if (rc.hasBuildRequirements(RobotType.SCOUT)) {
						buildRobot(RobotType.SCOUT);
						guardsBuilt++;
						return;
					}
				}
				if (guardsBuilt < 2)
					if (rc.hasBuildRequirements(RobotType.SOLDIER)) {
						buildRobot(RobotType.SOLDIER);
						guardsBuilt++;
						return;
					}
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
					// sendRadiusSignal(rc);
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
							Statics.moveTo(dir,rc);
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
			if (isScavenger) {
				setRoutine(Routine.GROUP);
				return;
			}
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
		circleOfHealing(rc);
		if (!rc.isCoreReady()) {
			return;
		}
		if (rc.getRoundNum() == 1) {// select leader
			Signal[] incoming = rc.emptySignalQueue();
			rc.setIndicatorString(3, "" + incoming.length + " messages");
			rc.broadcastSignal(BROADCAST_RANGE * 30);
			// isLeader =
			// !startingLocs[startingLocs.length-1].equals(rc.getLocation());
			isLeader = startingLocs[0].equals(rc.getLocation());
			if (startingLocs.length == 1)
				isLeader = true;

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
		// lastLoc = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		startingLocs = rc.getInitialArchonLocations(rc.getTeam());
		lastLoc = startingLocs[0];
		current = Routine.GROUP;
		robots = new TreeMap<Integer, RobotInfo>();
		radius = 8;
		radiusInc = 7;
		space = true;
		knownParts = new LinkedList<>();
		knownNeutralRobots = new LinkedList<>();
		for (int i = 0; i < startingLocs.length; i++)
			if (startingLocs[i].equals(rc.getLocation())) {
				archonIndex = i;
				break;
			}
		isScavenger = (startingLocs.length != 1 && archonIndex == startingLocs.length - 1);
		turns = 0;
		guardsBuilt = 0;
		// buildRobot(RobotType.SOLDIER);
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
				turns++;
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}

}