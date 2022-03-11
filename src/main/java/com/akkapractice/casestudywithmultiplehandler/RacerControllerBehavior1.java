package com.akkapractice.casestudywithmultiplehandler;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RacerControllerBehavior1 extends AbstractBehavior<RacerControllerBehavior1.Command> {

	
	private static Map<ActorRef<RacerBehavior1.Command>,Integer> currentPositionMap;
	private Map<ActorRef<RacerBehavior1.Command>,Long> finishingTimes;
	private int raceLength=100;
	static long  start;
	Object TIMER_KEY;
	
	
	private static void displayRace() {
		int displayLength=100;
	//	for (int i = 0; i < 50; ++i) System.out.println();
		System.out.println("Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
		System.out.println("    " + new String (new char[displayLength]).replace('\0', '='));
		
		Set<ActorRef<RacerBehavior1.Command>> racerList= currentPositionMap.keySet();
		
		List<ActorRef<RacerBehavior1.Command>> racerSortedList=racerList.stream().sorted((a,b)->{
			
			return (a.path().toString().substring(a.path().toString().length()-1))
			.compareTo((b.path().toString().substring(b.path().toString().length()-1)));}
			
		).collect(Collectors.toList());
		
		for (ActorRef<RacerBehavior1.Command> racer : racerSortedList) {
			
			System.out.println( racer.path().toString().substring(racer.path().toString().length()-1)+ " : "  + new String (new char[currentPositionMap.get(racer) * displayLength / 100]).replace('\0', '*'));
	
		}
		/*
		 * int i =0; for (ActorRef<RacerBehavior1.Command> racer :
		 * currentPositionMap.keySet()) { if(currentPositionMap.get(racer)==100) {
		 * System.out.println(i + " : " + new String (new
		 * char[currentPositionMap.get(racer) * displayLength / 100]).replace('\0',
		 * '*')); } }
		 */
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

	
	public static class RacerFinishedCommand implements Command{
		private static final long serialVersionUID=1L;
		
		ActorRef<RacerBehavior1.Command> racer;
		
		
		public RacerFinishedCommand(ActorRef<RacerBehavior1.Command> racer) {
			
			this.racer=racer;
			
		}
		
		public ActorRef<RacerBehavior1.Command> getRacer(){
			
			return racer;
		}
		
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
					finishingTimes = new HashMap<>();
					for(int i=0;i<=9;i++) {
						
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
				}).onMessage(RacerFinishedCommand.class, message->{
					
					currentPositionMap.put(message.getRacer(), raceLength);
					finishingTimes.put(message.getRacer(), System.currentTimeMillis());
					displayRace();
					getContext().getLog().info(message.getRacer().path()+" finished");
					if(finishingTimes.size()==10) {
						
						return raceCompleteMessageHandler();
					}else {
					return Behaviors.same();}
				})
				.build();
	}

	
	public Receive<RacerControllerBehavior1.Command> raceCompleteMessageHandler(){
		
		return newReceiveBuilder()
				.onMessage(GetPositionCommand.class, message->{
					
					for(ActorRef<RacerBehavior1.Command> racer:finishingTimes.keySet()) {
						
						getContext().stop(racer);
					}
					displayResult();
				return Behaviors.withTimers(timer->{
					
					timer.cancelAll();
					return Behaviors.stopped();
				});
					
					
				})
				.build();
	}
	public void displayResult() {
        System.out.println("Results");
        finishingTimes.values().stream().sorted().forEach(it -> {
            for (ActorRef<RacerBehavior1.Command> key : finishingTimes.keySet()) {
                if (finishingTimes.get(key) == it) {
                    String racerId = key.path().toString().substring(key.path().toString().length() -1);
                    System.out.println("Racer " + racerId + " finished in " + ( (double)it - start ) / 1000 + " seconds.");
                }
            }
        });}
	
}
