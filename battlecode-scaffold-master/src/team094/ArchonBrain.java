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
	private int facing;
	private int[] possibleDirections = new int[]{0,1,-1,2,3,-2,-3,4};
	private static final int BROADCAST_RANGE = 70;

	private void setRoutine(Routine r) {
		last = current;
		current = r;
	}
	
	private boolean canBuild(RobotController rc, RobotType type, Direction dir){
		RobotInfo[] robots = rc.senseNearbyRobots(1);
		Set<MapLocation> locs = new HashSet<>();
		for (RobotInfo i : robots)
			locs.add(i.location);
		return rc.canBuild(dir, type) && rc.senseRubble(rc.getLocation().add(dir)) < GameConstants.RUBBLE_OBSTRUCTION_THRESH && !locs.contains(rc.getLocation().add(dir));
	}

	private boolean buildRobot(RobotController rc, RobotType type) throws GameActionException {
		ArrayList<Direction> list = new ArrayList<>(Arrays.asList(Statics.directions));
		if (!rc.hasBuildRequirements(type)){
			scavenge(rc);
			turns--;
			return false;
		}
		while (list.size() > 0) {
			Direction d = list.remove((int) (Math.random()*list.size()));
			if (canBuild(rc,type, d)) {
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
	
	private void moveTowards(RobotController rc, Direction dir) throws GameActionException {
		for(int i:possibleDirections) {
			Direction candidateDirection = Direction.values()[(dir.ordinal()+i+8)%8];
			if(rc.canMove(candidateDirection)) {
				rc.move(candidateDirection);
				break;
			}
		}
	}

	private boolean buildScout(RobotController rc) throws GameActionException {
		return buildRobot(rc, RobotType.SCOUT);
	}

	private void intialize(RobotController rc) throws GameActionException {
		turns = 0;
		facing=0;
		buildScout(rc);
		current = Routine.TURRET_CLUSTER;
	}

	private void buildTurretCluster(RobotController rc) throws GameActionException {
		switch (turns) {
		case 1:
		case 2:
		case 3:
			buildRobot(rc, RobotType.SOLDIER);
			break;
		case 4:
			buildRobot(rc, RobotType.TURRET);
			RobotInfo[] robots = rc.senseNearbyRobots();
			for (RobotInfo r : robots)
				if (r.team == rc.getTeam() && r.type == RobotType.TURRET
						&& r.location.distanceSquaredTo(rc.getLocation()) == 1) {
					Signal s = SignalEncoder.encodeRobot(r.type, r.ID, r.location);
					rc.broadcastMessageSignal(s.getMessage()[0], s.getMessage()[1], BROADCAST_RANGE);
				}
			break;
		default:
			setRoutine(Routine.NONE);
			turns = 0;
			break;
		}
	}

	private void scavenge(RobotController rc) throws GameActionException {
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
				moveTowards(rc,_moveDirection);
			return;
		}
		
		if (potential.length == 0 || turns !=1){
			setRoutine(Routine.RANDOM);
			randomlyMove(rc);
			return;
		}
		
		_moveDirection = rc.getLocation().directionTo(Statics.closestLoc(rc.getLocation(), potential));
		
		if(rc.isCoreReady())
			moveTowards(rc,_moveDirection);
		
		//if (rc.canMove(_moveDirection)){
		//	rc.move(_moveDirection);
		//	return;
		//}

	}
	
	Direction _moveDirection;
	private void randomlyMove(RobotController rc) throws GameActionException{
		if (turns > 5){
			_moveDirection = null;
			turns = 0;
			setRoutine(Routine.NONE);
			return;
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
	
	private void determineRoutine(RobotController rc){
		if (last != Routine.TURRET_CLUSTER)
			setRoutine(Routine.TURRET_CLUSTER);
		else 
			setRoutine(Routine.SCAVENGE);
	}
	
	private void charge(RobotController rc){
		//TODO: Implement charge
	}

	private void runTurn(RobotController rc) throws GameActionException {
		if (!rc.isCoreReady())
			return;
		switch (current) {
		case CHARGE:
			break;
		case NONE:
			determineRoutine(rc);
			runTurn(rc);
			return;
		case RANDOM:
			randomlyMove(rc);
			break;
		case SCAVENGE:
			scavenge(rc);
			break;
		case SCOUT:
			buildScout(rc);
			break;
		case TURRET_CLUSTER:
			buildTurretCluster(rc);
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
	public void run(RobotController rc) {
		try {
			intialize(rc);
		} catch (GameActionException e1) {
			e1.printStackTrace();
		}

		while (true) {
			Clock.yield();
			turns++;
			try {
				runTurn(rc);
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}

}