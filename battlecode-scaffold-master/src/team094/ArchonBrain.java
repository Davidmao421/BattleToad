package team094;

import java.util.*;
import battlecode.common.*;

public class ArchonBrain implements Brain {

	private enum Routine {
		TURRET_CLUSTER, CHARGE, SCAVENGE, RANDOM, NONE, SCOUT
	}

	private int turns;
	private Routine last;
	private Routine current;
	private int[] possibleDirections = new int[] { 0, 1, -1, 2, 3, -2, -3, 4 };
	private static final int BROADCAST_RANGE = 70;
	private RobotController rc;
	private MapLocation lastLoc;
	
	private List<MapLocation> knownParts;
	private List<MapLocation> knownEnemyArchons;
	private List<MapLocation> knownNeutralRobots;
	
	private Map<Integer, RobotInfo> robots;
	
	int panicRobot = -1;

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
		if (!rc.hasBuildRequirements(type)) {
			scavenge();
			turns--;
			return false;
		}
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

	private void moveTowards(Direction dir) throws GameActionException {
		for (int i : possibleDirections) {
			Direction candidateDirection = Direction.values()[(dir.ordinal() + i + 8) % 8];
			if (rc.canMove(candidateDirection)) {
				rc.move(candidateDirection);
				break;
			}
		}
	}

	private boolean buildScout() throws GameActionException {
		return buildRobot(RobotType.SCOUT);
	}

	private void intialize() throws GameActionException {
		turns = 0;
		buildScout();
		lastLoc = rc.getLocation();
		current = Routine.TURRET_CLUSTER;
		
		knownEnemyArchons = new LinkedList<>();
		knownNeutralRobots = new LinkedList<>();
		knownParts = new LinkedList<>();
		robots = new TreeMap<>();
	}

	private void buildTurretCluster() throws GameActionException {
		switch (turns) {
		case 1:
		case 2:
		case 3:
			buildRobot(RobotType.SOLDIER);
			break;
		default:
			setRoutine(Routine.NONE);
			turns = 0;
			break;
		}
	}

	private void scavenge() throws GameActionException {
		MapLocation[] sensedParts = rc.sensePartLocations(-1);
		MapLocation[] parts = (MapLocation[]) Statics.combineArrays(sensedParts, knownParts.toArray());
		RobotInfo[] neutrals = rc.senseNearbyRobots(-1, Team.NEUTRAL);
		// if neutral robot
		if (neutrals.length != 0) {
			MapLocation[] locs = new MapLocation[neutrals.length];
			int i = 0;
			for (RobotInfo r : neutrals) {
				locs[i] = r.location;
				if (locs[i].distanceSquaredTo(rc.getLocation()) <= 2) {
					if (rc.isCoreReady())
						rc.activate(locs[i]);
					return;
				}
				i++;
			} // reaches here if cannot activate
			_moveDirection = rc.getLocation().directionTo(Statics.closestLoc(rc.getLocation(), locs));
			if (rc.isCoreReady())
				moveTowards(_moveDirection);
			return;
		}
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
		//I am SO sorry for this but it's the easiest way. please forgive me code gods
		checkFree:
		if (freeParts.size() != 0) {
			MapLocation[] array = new MapLocation[freeParts.size()];
			int index = 0;
			for(MapLocation l:freeParts) {
				array[index] = l;
				index++;
			}
			MapLocation partLoc = Statics.closestLoc(rc.getLocation(), array);
			if (rc.getLocation().distanceSquaredTo(partLoc) < rc.getType().sensorRadiusSquared && knownParts.contains(partLoc) && !Statics.contains(partLoc, sensedParts))
			{
				knownParts.remove(partLoc);
				freeParts.remove(partLoc);
				break checkFree; //Shit I suck at life.
			}
			moveTowards(rc.getLocation().directionTo(partLoc));
			return;
		}
		
		// if hard parts
		checkHard: //FML this is by far the best way of doing this
		if (hardParts.size() != 0) {
			MapLocation[] array = new MapLocation[hardParts.size()];
			int index = 0;
			for(MapLocation l:hardParts) {
				array[index] = l;
				index++;
			}
			MapLocation loc = Statics.closestLoc(rc.getLocation(), array);
			if (rc.getLocation().distanceSquaredTo(loc) < rc.getType().sensorRadiusSquared && knownParts.contains(loc) && !Statics.contains(loc, sensedParts)){
				knownParts.remove(loc);
				hardParts.remove(loc);
				break checkHard; //Again I'm really really sorry
			}

			if (rc.getLocation().distanceSquaredTo(loc) > 7) {
				moveTowards(rc.getLocation().directionTo(loc));
			}
		}
	}

	Direction _moveDirection;

	private void randomlyMove() throws GameActionException {
		if (turns > 5) {
			rc.broadcastSignal(BROADCAST_RANGE);
			_moveDirection = null;
			turns = 0;
			setRoutine(Routine.NONE);
			return;
		}
		RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		RobotInfo[] zombies = rc.senseNearbyRobots(-1, Team.ZOMBIE);

		Statics.runAway(rc, allies, enemies);

		if (_moveDirection == null)
			_moveDirection = Statics.directions[(int) (Math.random() * Statics.directions.length)];

		if (rc.canMove(_moveDirection)) {
			rc.move(_moveDirection);
			return;
		}

		ArrayList<Direction> potential = new ArrayList<>(Arrays.asList(Statics.directions));
		while (potential.size() > 0) {
			Direction d = potential.remove((int) (Math.random() * potential.size()));
			if (rc.canMove(d)) {
				_moveDirection = d;
				rc.move(d);
				return;
			}
		}
	}

	private void determineRoutine() {
		if (last != Routine.TURRET_CLUSTER)
			setRoutine(Routine.TURRET_CLUSTER);
		else
			setRoutine(Routine.SCAVENGE);
	}

	private void charge() {
		// TODO: Implement charge
	}

	private void navigateToAttack(MapLocation attackLoc) throws GameActionException { // TODO:
		if (rc.getLocation().distanceSquaredTo(attackLoc) <= 100) { // might
																	// want to
																	// change
																	// value
			// TODO: need to send signals to tell soldiers to rush attackLoc
			Direction toAttack = rc.getLocation().directionTo(attackLoc);
			if (rc.isCoreReady() && canBuild(RobotType.SOLDIER, toAttack)) {
				rc.build(toAttack, RobotType.SOLDIER);
			}
		} else {
			Statics.navigateTo(attackLoc, rc);
		}

	}

	private void runTurn() throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal s : signals){
			switch (SignalEncoder.getPacketType(s)) {
			case ATTACK_ENEMY:
				//TODO: decide what archons should do
				break;
			case CHANGE_SCHEME:
				//TODO: implement schemes
				break;
			case DEAD:
				int id = SignalEncoder.decodeDead(s);
				if (robots.containsKey(id))
					buildRobot(robots.get(id).type);
				robots.remove(id);
				break;
			case ECHO:
				//Lulz
				break;
			case LOCAL_ATTACK:
				//TODO: Figure this shit out
				break;
			case NEUTRAL_ROBOT:
				Signal neutral = SignalEncoder.decodeNeutralRobot(s);
				knownNeutralRobots.add(neutral.getLocation());
				break;
			case NEW_ROBOT:
				//Archon prob sending this out
				break;
			case OTHER:
				//WTF
				break;
			case PANIC:
				//TODO: Decide what to do
				break;
			case PANIC_OVER:
				break;
			case PARTS_CACHE:
				Signal parts = SignalEncoder.decodeParts(s);
				knownParts.add(parts.getLocation());
				break;
			default:
				break;
			}
		}
		
		
		if (!rc.isCoreReady()) {
			turns--;
			return;
		}
		switch (current) {
		case CHARGE:
			break;
		case NONE:
			determineRoutine();
			runTurn();
			return;
		case RANDOM:
			randomlyMove();
			break;
		case SCAVENGE:
			scavenge();
			break;
		case SCOUT:
			buildScout();
			break;
		case TURRET_CLUSTER:
			buildTurretCluster();
			break;
		default:
			break;
		}

		// DEBUG
		String s = "";
		switch (current) {
		case CHARGE:
			s = "charge";
			break;
		case NONE:
			s = "none";
			break;
		case RANDOM:
			s = "random";
			break;
		case SCAVENGE:
			s = "scavenge";
			break;
		case SCOUT:
			s = "scout";
			break;
		case TURRET_CLUSTER:
			s = "turret cluster";
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
			intialize();
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