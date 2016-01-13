package team094;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public class ScoutBrain implements Brain {
	static LocationInfo[][] hashmap = new LocationInfo[100][100];
	static MapLocation[] mLB = new MapLocation[4];
	static int mLBIndex = 0;
	static Random rnd;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
        Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static Team enemyTeam, myTeam;
	static MapLocation move;
	public void run(RobotController rc){
		intialize(rc);
		myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
		rnd = new Random(rc.getID());
		while(true){
			scoutcode(rc);
			Clock.yield();
		}
	}
	public static void intialize(RobotController rc){
		
	}
	private static void scoutcode(RobotController rc){
		sense(rc);
		move(rc);
	}
	private static void sense(RobotController rc){
		MapLocation[] partsLoc = rc.sensePartLocations(-1);
		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(),-1);
		RobotInfo[] neutrals = rc.senseNearbyRobots(-1, enemyTeam);
		for(MapLocation loc: partsLoc){
			if(hashmap[(loc.x+16000)%100][(loc.y+16000)%100]!=null){
				continue;
			}
			hashmap[(loc.x+16000)%100][(loc.y+16000)%100]=new LocationInfo(loc, null, rc.senseParts(loc), rc.senseRubble(loc), 0);
			mLB[mLBIndex] = loc;
			mLBIndex++;
			if(mLBIndex == mLB.length){
				mLBIndex = 0;
				messageback();
			}
		}
		for(RobotInfo zombie: enemies){
			if(zombie.type == RobotType.ZOMBIEDEN){
				if(hashmap[(zombie.location.x+16000)%100][(zombie.location.y+16000)%100]!=null){
					continue;
				}
				hashmap[(zombie.location.x+16000)%100][(zombie.location.y+16000)%100]=new LocationInfo(zombie.location, zombie, 0, rc.senseRubble(zombie.location), 0);
				mLB[mLBIndex] = zombie.location;
				mLBIndex = 0;
				mLBIndex++;
			}
			if(mLBIndex == mLB.length){
				mLBIndex = 0;
				messageback();
			}
		}
		for(RobotInfo neutral: neutrals){
			if(hashmap[(neutral.location.x+16000)%100][(neutral.location.y+16000)%100]!=null){
				continue;
			}
			hashmap[(neutral.location.x+16000)%100][(neutral.location.y+16000)%100]=new LocationInfo(neutral.location, neutral, 0, rc.senseRubble(neutral.location), 0);
			mLB[mLBIndex] = neutral.location;
			mLBIndex++;
			if(mLBIndex == mLB.length){
				messageback();
			}
		}
		
		
	}
	private static void move(RobotController rc){
		RobotInfo[] myArray = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared,myTeam);
		if(myArray.length>=30){
			RobotInfo[] myNewArray = new RobotInfo[30];
			for (int i = 0; i < 30; ++i) {
				myNewArray[i]=myArray[i];
			}
			myArray = myNewArray;
		}
		if(myArray.length != 0){
			
			move = coolMethods.reflect(coolMethods.averageLocation(myArray),rc);
		}
		if(rc.isCoreReady()&& move!=null){
			try {
				coolMethods.navigateTo(move,rc);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	private static void messageback() {
		
	}
}