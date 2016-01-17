package team094;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.sun.org.apache.bcel.internal.generic.BREAKPOINT;

import battlecode.common.*;

public class ScoutBrain implements Brain {
	MapLocation enemyCom, teamCom;
	
	Queue<Signal> broadcastQueue;

	public void initialize(RobotController rc) {
		enemyCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam().opponent()));
		teamCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		broadcastQueue = new LinkedList<>();
	}

	public void radiate(RobotController rc) throws GameActionException{
		Direction d = teamCom.directionTo(rc.getLocation());
		if (rc.canMove(d))
			rc.move(d);
	}
	
	public void senseBroadcast(RobotController rc){
		RobotInfo[] robots = rc.senseNearbyRobots();
		MapLocation[] parts = rc.sensePartLocations(-1);
		
		for (MapLocation part : parts){
			broadcastQueue.offer(SignalEncoder.encodeParts(part,rc.senseParts(part)));
		}
		
		for (RobotInfo info : robots){
			if (info.team == rc.getTeam())
				continue;
			if (rc.getTeam().opponent() == info.team && (info.type == RobotType.ZOMBIEDEN || info.type == RobotType.ARCHON))
				broadcastQueue.offer(SignalEncoder.encodeRobot(info.type,info.ID,info.location));
		}
	}
	
	public void runTurn(RobotController rc) throws GameActionException {
		sense(rc);
		radiate(rc);
	}

	public void run(RobotController rc) {
		try {
			initialize(rc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while(true){
			Clock.yield();
			try{
				runTurn(rc);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
/*	public void run(RobotController rc){
		while (true)Clock.yield(); // what the hell is this
	}*/
}