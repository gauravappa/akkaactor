package com.akkapractice.casestudywithmultiplehandler;

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

public class RacerControllerBehavior1 extends AbstractBehavior<RacerControllerBehavior1.Command> {

	
	private static Map<ActorRef<RacerBehavior1.Command>,Integer> currentPositionMap;
	private int raceLength=100;
	static long  start;
	Object TIMER_KEY;
	
	
	private static void displayRace() {
		int displayLength=50;
		for (int i = 0; i < 50; ++i) System.out.println();
		System.out.println("Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
		System.out.println("    " + new String (new char[displayLength]).replace('\0', '='));
		
		int i =0;
		for (ActorRef<RacerBehavior1.Command> racer : currentPositionMap.keySet()) {
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
		
		ActorRef<RacerBehavior1.Command> racer;
		
		int position;
		
		public RacerUpdateCommand(ActorRef<RacerBehavior1.Command> racer,int position) {
			this.racer=racer;
			this.position=position;
		}
		
		public ActorRef<RacerBehavior1.Command> getRacer(){
			
			return racer;
		}
		
		public int getPosition() {
			
		return position;
		}
		
	}

	public static class GetPositionCommand implements Command{
		
		private static final long serialVersionUID=1L;
		
		
	}

	private RacerControllerBehavior1(ActorContext<RacerControllerBehavior1.Command> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public static Behavior<RacerControllerBehavior1.Command> create() {
		return Behaviors.setup(context -> {
			return new RacerControllerBehavior1(context);
		});
	}

	@Override
	public Receive<Command> createReceive() {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				.onMessage(RacerControllerBehavior1.StartCommand.class, message->{
					start=System.currentTimeMillis();
					currentPositionMap=new HashMap<>();
					
					for(int i=1;i<=10;i++) {
						
						ActorRef<RacerBehavior1.Command> racer= getContext()
								.spawn(RacerBehavior1.create(), "racer"+i);
						
						currentPositionMap.put(racer,0);
						
						racer.tell(new RacerBehavior1.StartCommand(raceLength));
						
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
					
					for(ActorRef<RacerBehavior1.Command> racer:currentPositionMap.keySet()) {
						
						racer.tell(new RacerBehavior1.PositionCommand(getContext().getSelf()));
					}
					
					return this;
				})
				.build();
	}

}
