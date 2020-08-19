package com.github.systeminvecklare.simulation;

import java.util.Collection;
import java.util.List;

public interface ISimulatedWorld {
	public static final long TIME_UNSET = Long.MIN_VALUE;
	
	void setCurrentTime(long currentTime);
	long getCurrentTime();
	
	IWorldEventEnvironment getEventEnvironment();
	Collection<IPredictable> getPredictables();
	List<IWorldEvent> getEvents();
}
