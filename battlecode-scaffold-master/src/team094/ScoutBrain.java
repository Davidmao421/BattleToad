package team094;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

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
		sentSignals = new HashSet<>();

	}

	public void addBroadcast(Signal s) {
		if (CompareStuff.containsSignal(s, broadcastQueue) || CompareStuff.containsSignal(s, sentSignals))
			return;
		broadcastQueue.add(s);
	}

	public void radiate() throws GameActionException {
		Direction d = teamCom.directionTo(rc.getLocation());
		if (rc.canMove(d))
			rc.move(d);
	}

	public void senseBroadcast() {
		RobotInfo[] robots = rc.senseNearbyRobots();
		MapLocation[] parts = rc.sensePartLocations(-1);

		rc.setIndicatorString(1, "Nearby Robots: " + robots.length);
		rc.setIndicatorString(2, "Nearby parts: " + parts.length);
		
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
				// addBroadcast(SignalEncoder.encodeRobot(info.type, info.ID,
				// info.location, rc.getLocation()));
			}

			if (info.team == Team.NEUTRAL) {
				addBroadcast(SignalEncoder.encodeNeutralRobot(info.type, info.ID, info.location));
			}
		}

	}

	public void runTurn() throws GameActionException {
		senseBroadcast();

		if (broadcast())
			return;
		radiate(); // TODO: working movement
	}

	public boolean broadcast() throws GameActionException {
		if (rc.isCoreReady() && !broadcastQueue.isEmpty()) {
			Signal s = broadcastQueue.remove();
			rc.broadcastMessageSignal(s.getMessage()[0], s.getMessage()[1], 1600); // TODO: Figure out a good broadcast range
			rc.setIndicatorString(0, "Broadcast queue: " + broadcastQueue.size());
			sentSignals.add(s);
			broadcast();

			return true;
		}

		return false;
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
			if (!rc.isCoreReady())
				continue;
			try {
				runTurn();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}