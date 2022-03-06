package com.akkapractice.casestudy1;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RacerControllerBehavior extends AbstractBehavior<RacerControllerBehavior.Command> {

	
	private static Map<ActorRef<RacerBehaviour.Command>,Integer> currentPositionMap;
	private int raceLength=100;
	static long  start;
	Object TIMER_KEY;
	
	
	private static void displayRace() {
		int displayLength=160;
		for (int i = 0; i < 50; ++i) System.out.println();
		System.out.println("Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
		System.out.println("    " + new String (new char[displayLength]).replace('\0', '='));
		
		int i =1;
		for (ActorRef<RacerBehaviour.Command> racer : currentPositionMap.keySet()) {
			System.out.println(i + " : "  + new String (new char[currentPositionMap.get(racer) * displayLength / 100]).replace('\0', '*'));
		i++;
		}
	}
	
	public interface Command extends Serializable{

	}
	
	public static class StartCommand implements Command{
		private static final long serialVersionUID=1L;
		
	}
	
	public static class RacerUpdateCommand implements Command{
		private static final long serialVersionUID=1L;
		
		ActorRef<RacerBehaviour.Command> racer;
		
		int position;
		
		public RacerUpdateCommand(ActorRef<RacerBehaviour.Command> racer,int position) {
			this.racer=racer;
			this.position=position;
		}
		
		public ActorRef<RacerBehaviour.Command> getRacer(){
			
			return racer;
		}
		
		public int getPosition() {
			
		return position;
		}
		
	}

	public static class GetPositionCommand implements Command{
		
		private static final long serialVersionUID=1L;
		
		
	}

	private RacerControllerBehavior(ActorContext<RacerControllerBehavior.Command> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public static Behavior<RacerControllerBehavior.Command> create() {
		return Behaviors.setup(context -> {
			return new RacerControllerBehavior(context);
		});
	}

	@Override
	public Receive<Command> createReceive() {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				.onMessage(RacerControllerBehavior.StartCommand.class, message->{
					start=System.currentTimeMillis();
					currentPositionMap=new HashMap<>();
					
					for(int i=1;i<=10;i++) {
						
						ActorRef<RacerBehaviour.Command> racer= getContext()
								.spawn(RacerBehaviour.create(), "racer"+i);
						
						currentPositionMap.put(racer,0);
						
						racer.tell(new RacerBehaviour.StartCommand(raceLength));
						
					}
					return Behaviors.withTimers(timer->{
						
						timer.startTimerAtFixedRate(TIMER_KEY, new GetPositionCommand(), Duration.ofSeconds(1));
						return this;
						
					});
				})
				.onMessage(RacerUpdateCommand.class, message->{
					
					currentPositionMap.put(message.getRacer(), message.getPosition());
					displayRace();
					return this;
					
				})
				.onMessage(GetPositionCommand.class, message->{
					
					for(ActorRef<RacerBehaviour.Command> racer:currentPositionMap.keySet()) {
						
						racer.tell(new RacerBehaviour.PositionCommand(getContext().getSelf()));
					}
					
					return this;
				})
				.build();
	}

}
