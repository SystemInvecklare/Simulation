package com.github.systeminvecklare.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class Simulator implements ISimulator {
	private long targetTime = ISimulatedWorld.TIME_UNSET;
	
	public long getTargetTime() {
		return targetTime;
	}
	
	@Override
	public void setTargetTime(long targetTime) {
		this.targetTime = targetTime;
	}
	
	@Override
	public void simulate(ISimulatedWorld world) {
		long currentTime = world.getCurrentTime();
		if(currentTime == ISimulatedWorld.TIME_UNSET) {
			throw new IllegalStateException("currentTime has not been set");
		}
		if(targetTime == ISimulatedWorld.TIME_UNSET) {
			throw new IllegalStateException("targetTime has not been set");
		}
		Period fullPeriod = new Period(currentTime, targetTime);
		if(fullPeriod.isEmpty()) {
			return;
		}
		
		List<ISimulationStep> simulationChain = createSimulationChain(world.getEvents(), fullPeriod);
		
		for(ISimulationStep step : simulationChain) {
			step.simulate(world);
		}
	}
	
	private static List<ISimulationStep> createSimulationChain(List<IWorldEvent> events, final Period fullPeriod) {
		List<IWorldEvent> eventsInPeriod = getEventsWithinPeriod(events, fullPeriod, new ArrayList<IWorldEvent>());
		
		List<ISimulationStep> steps = new ArrayList<ISimulationStep>();
		
		Period lastPeriod = fullPeriod;
		
		sortEvents(eventsInPeriod);
		Period[] cutPart = new Period[2]; 
		for(IWorldEvent event : eventsInPeriod) {
			lastPeriod.cut(event.getTime(), cutPart);
			if(!cutPart[0].isEmpty()) {
				steps.add(new PredictStep(cutPart[0]));
			}
			lastPeriod = cutPart[1];
			steps.add(new EventStep(event));
		}
		
		steps.add(new PredictStep(lastPeriod));
		
		return steps;
	}
	
	private static void sortEvents(List<IWorldEvent> events) {
		Collections.sort(events, new Comparator<IWorldEvent>() {
			@Override
			public int compare(IWorldEvent o1, IWorldEvent o2) {
				long t1 = o1.getTime();
				long t2 = o2.getTime();
				if(t1 == t2) {
					return o1.getUniqueId().compareTo(o2.getUniqueId());
				} else {
					return Long.compare(t1, t2);
				}
			}
		});
	}

	private static <R extends Collection<? super IWorldEvent>> R getEventsWithinPeriod(Collection<IWorldEvent> events, Period period, R result) {
		for(IWorldEvent event : events) {
			if(period.contains(event.getTime())) {
				result.add(event);
			}
		}
		return result;
	}
	
	private static class Period {
		private final long startTime; //Inclusive
		private final long endTime; //Exclusive
		
		public Period(long startTime, long endTime) {
			this.startTime = startTime;
			this.endTime = endTime;
			if(endTime < startTime) {
				throw new IllegalArgumentException("endTime is before startTime");
			}
		}

		public boolean contains(long time) {
			return time >= startTime && time < endTime;
		}

		public boolean isEmpty() {
			return startTime == endTime;
		}
		
		public float getDelta() {
			return (endTime-startTime)/1000f;
		}

		public void cut(long cutPoint, Period[] result) {
			result[0] = new Period(startTime, cutPoint);
			result[1] = new Period(cutPoint, endTime);
		}
	}
	
	private interface ISimulationStep {
		void simulate(ISimulatedWorld world);
	}
	
	private static class PredictStep implements ISimulationStep {
		private final Period period;

		public PredictStep(Period period) {
			this.period = period;
		}

		@Override
		public void simulate(ISimulatedWorld world) {
			float delta = period.getDelta();
			for(IPredictable predictable : world.getPredictables()) {
				predictable.simulate(delta);
			}
			world.setCurrentTime(period.endTime);
		}
	}
	
	private static class EventStep implements ISimulationStep {
		private final IWorldEvent event;

		public EventStep(IWorldEvent event) {
			this.event = event;
		}

		@Override
		public void simulate(ISimulatedWorld world) {
			event.happen(world.getEventEnvironment());
		}
	}
}
