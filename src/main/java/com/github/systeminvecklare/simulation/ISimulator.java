package com.github.systeminvecklare.simulation;

public interface ISimulator {
	void setTargetTime(long targetTime);
	void simulate(ISimulatedWorld simulatedWorld);

}
