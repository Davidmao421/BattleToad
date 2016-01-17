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
	private int[] possibleDirections = new int[]{0,1,-1,2,3,-2,-3,4};
	private static final int BROADCAST_RANGE = 70;
	private RobotController rc;
	private void setRoutine(Routine r) {
		last = current;
		current = r;
	}
	
	private boolean canBuild(RobotType type, Direction dir){
		RobotInfo[] robots = rc.senseNearbyRobots(1);
		Set<MapLocation> locs = new HashSet<>();
		for (RobotInfo i : robots)
			locs.add(i.location);
		return rc.canBuild(dir, type) && rc.senseRubble(rc.getLocation().add(dir)) < GameConstants.RUBBLE_OBSTRUCTION_THRESH && !locs.contains(rc.getLocation().add(dir));
	}

	private boolean buildRobot(RobotType type) throws GameActionException {
		ArrayList<Direction> list = new ArrayList<>(Arrays.asList(Statics.directions));
		if (!rc.hasBuildRequirements(type)){
			scavenge();
			turns--;
			return false;
		}
		while (list.size() > 0) {
			Direction d = list.remove((int) (Math.random()*list.size()));
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
		for(int i:possibleDirections) {
			Direction candidateDirection = Direction.values()[(dir.ordinal()+i+8)%8];
			if(rc.canMove(candidateDirection)) {
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
//		buildScout();
		current = Routine.TURRET_CLUSTER;
	}

	private void buildTurretCluster() throws GameActionException {
		switch (turns) {
		case 1:
		case 2:
		case 3:
			buildRobot(RobotType.SOLDIER);
			break;
		case 4:
			buildRobot(RobotType.TURRET);
			RobotInfo[] robots = rc.senseNearbyRobots();
			for (RobotInfo r : robots)
				if (r.team == rc.getTeam() && r.type == RobotType.TURRET
						&& r.location.distanceSquaredTo(rc.getLocation()) == 1) {
					Signal s = SignalEncoder.encodeRobot(r.type, r.ID, r.location, rc.getLocation());
					rc.broadcastMessageSignal(s.getMessage()[0], s.getMessage()[1], BROADCAST_RANGE);
				}
			break;
		default:
			setRoutine(Routine.NONE);
			turns = 0;
			break;
		}
	}

	private void scavenge() throws GameActionException {
		MapLocation[] potential =  rc.sensePartLocations(BROADCAST_RANGE);
		RobotInfo[] neutrals = rc.senseNearbyRobots(BROADCAST_RANGE, Team.NEUTRAL);
		
		if(neutrals.length!=0) {
			MapLocation[] locs = new MapLocation[neutrals.length];
			int i = 0;
			for(RobotInfo r:neutrals) {
				locs[i] = r.location;
				if(locs[i].distanceSquaredTo(rc.getLocation())<=2) {
					if(rc.isCoreReady())
						rc.activate(locs[i]);
					return;
				}
				i++;
			}
			_moveDirection = rc.getLocation().directionTo(Statics.closestLoc(rc.getLocation(), locs));
			if(rc.isCoreReady())
				moveTowards(_moveDirection);
			return;
		}
		
		if (potential.length == 0 || turns !=1){
			setRoutine(Routine.RANDOM);
			randomlyMove();
			return;
		}
		
		_moveDirection = rc.getLocation().directionTo(Statics.closestLoc(rc.getLocation(), potential));
		
		if(rc.isCoreReady())
			moveTowards(_moveDirection);
		
		//if (rc.canMove(_moveDirection)){
		//	rc.move(_moveDirection);
		//	return;
		//}

	}
	
	Direction _moveDirection;
	private void randomlyMove() throws GameActionException{
		if (turns > 5){
			rc.broadcastSignal(BROADCAST_RANGE);
			_moveDirection = null;
			turns = 0;
			setRoutine(Routine.NONE);
			return;
		}
		RobotInfo[] nearby = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);
		int allies = 0;
		int enemies = 0;
		for(RobotInfo r:nearby) {
			if(r.team.equals(rc.getTeam())) {
				allies++;
			} else {
				enemies++;
			}
		}
		
		if(enemies>allies) {//run away
			MapLocation away = rc.getLocation().add(Statics.directions[0]);
			for(RobotInfo r:nearby) {
				if(!r.team.equals(rc.getTeam())) {
					away = r.location;
					break;
				}
			}
			_moveDirection = away.directionTo(rc.getLocation());
			int[] dirs = new int[]{0,1,-1,2,-2};
			for(int i:dirs) {
				Direction candidateDirection = Statics.directions[_moveDirection.ordinal()+i+8%8];
				if(rc.canMove(candidateDirection)) {
					rc.move(candidateDirection);
					return;
				}
			}
		}
		
		if (_moveDirection == null)
			_moveDirection = Statics.directions[(int) (Math.random()*Statics.directions.length)];
		
		if (rc.canMove(_moveDirection)){
			rc.move(_moveDirection);
			return;
		}
		
		ArrayList<Direction> potential = new ArrayList<>(Arrays.asList(Statics.directions));
		while (potential.size() > 0){
			Direction d = potential.remove((int)(Math.random()*potential.size()));
			if (rc.canMove(d)){
				_moveDirection = d;
				rc.move(d);
				return;
			}
		}
	}
	
	private void determineRoutine(){
		if (last != Routine.TURRET_CLUSTER)
			setRoutine(Routine.TURRET_CLUSTER);
		else 
			setRoutine(Routine.SCAVENGE);
	}
	
	private void charge(){
		//TODO: Implement charge
	}
	
	private void navigateToAttack(MapLocation attackLoc) throws GameActionException{ //TODO: should be used in tandem with scouts to tell where to go etc
		if(rc.getLocation().distanceSquaredTo(attackLoc)<=100) { //might want to change value
			//TODO: need to send signals to tell soldiers to rush attackLoc
			Direction toAttack = rc.getLocation().directionTo(attackLoc);
			if(rc.isCoreReady()&&canBuild(RobotType.SOLDIER, toAttack)) {
				rc.build(toAttack, RobotType.SOLDIER);
			}
		}
		else {
			Statics.navigateTo(attackLoc, rc);
		}
			
	}
	
	private void runTurn() throws GameActionException {
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
		
		
		//DEBUG
		String s = "";
		switch(current){
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
//		System.out.println(s);
	}

	@Override
	public void run(RobotController rcI) {
		rc=rcI;
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