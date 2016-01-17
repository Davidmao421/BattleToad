package team094;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;


import battlecode.common.*;

public class ScoutBrain implements Brain {
	MapLocation enemyCom, teamCom;

	// Queue<Signal> broadcastQueue;
	Queue<Signal> broadcastQueue;
	Set<Signal> sentSignals;
	
	RobotController rc;
	public void initialize() {
		enemyCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam().opponent()));
		teamCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		broadcastQueue = new LinkedList<>();
		sentSignals = new TreeSet<>();
	}
	
	public void addBroadcast(Signal s){
		if (sentSignals.contains(s))
			return;
		addBroadcast(s);
	}

	public void radiate() throws GameActionException {
		Direction d = teamCom.directionTo(rc.getLocation());
		if (rc.canMove(d))
			rc.move(d);
	}

	public void senseBroadcast() {
		RobotInfo[] robots = rc.senseNearbyRobots();
		MapLocation[] parts = rc.sensePartLocations(-1);

		for (MapLocation part : parts) {
			if (!broadcastQueue.contains(SignalEncoder.encodeParts(part, rc.senseParts(part)))) {
				addBroadcast(SignalEncoder.encodeParts(part, rc.senseParts(part)));
			}
		}

		for (RobotInfo info : robots) {
			if (info.team == rc.getTeam())
				continue;
			if (rc.getTeam().opponent() == info.team
					&& (info.type == RobotType.ZOMBIEDEN || info.type == RobotType.ARCHON)) {
				if (!broadcastQueue.contains(SignalEncoder.encodeRobot(info.type, info.ID, info.location)))
				addBroadcast(SignalEncoder.encodeRobot(info.type, info.ID, info.location));
			}
			if (info.team.equals(Team.NEUTRAL)) {
				if (!broadcastQueue.contains(SignalEncoder.encodeNeutralRobot(info.type, info.ID, info.location)))
				addBroadcast(SignalEncoder.encodeNeutralRobot(info.type, info.ID, info.location));
			}
		}

	}

	public void runTurn() throws GameActionException {
		senseBroadcast();
		radiate(); // TODO: working movement
		broadcast();
	}

	public void broadcast() throws GameActionException {
		if (rc.isCoreReady() && !broadcastQueue.isEmpty()) {
			Signal s = broadcastQueue.poll();
			rc.broadcastMessageSignal(s.getMessage()[0], s.getMessage()[1], -1); //TODO: figure out a good actual broadcast range
			sentSignals.add(s);
			broadcast();
		}
	}

	public void run(RobotController rc1) {
		rc = rc1;
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			Clock.yield();
			try {
				runTurn();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/*
	 * public void run(){ while (true)Clock.yield(); // what
	 * the hell is this }
	 */
}