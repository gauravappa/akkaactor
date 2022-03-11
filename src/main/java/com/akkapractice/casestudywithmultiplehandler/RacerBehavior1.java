package com.akkapractice.casestudywithmultiplehandler;

import java.io.Serializable;
import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RacerBehavior1 extends AbstractBehavior<RacerBehavior1.Command>{


	private final double defaultAverageSpeed = 48.2;
	private Random random;	
	
	private double currentSpeed = 0;
	
	
	public interface Command extends Serializable {

	}

	public static class StartCommand implements Command{
		
		private static final long serialVersionUID=1L;
		
		private int raceLength;
		
		public StartCommand(int raceLength) {
			
			this.raceLength=raceLength;
			
		}
		
		public int getRaceLength() {
			
			return raceLength;
			
		}
		
	}
	
	public static class PositionCommand implements Command {

		private static final long serialVersionUID=1L;
		
		ActorRef<RacerControllerBehavior1.Command> controller;
		
		public PositionCommand(ActorRef<RacerControllerBehavior1.Command> controller) {
			
			this.controller=controller;
			
		}
		
		public ActorRef<RacerControllerBehavior1.Command> getController(){
			
			return controller;
		}
		
		
		
	}
	
	private RacerBehavior1(ActorContext<Command> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public static Behavior<RacerBehavior1.Command> create(){
	
		return Behaviors.setup(RacerBehavior1::new);
		
	}

	private double getMaxSpeed(int averageSpeedAdjustmentFactor) {
		return defaultAverageSpeed * (1+((double)averageSpeedAdjustmentFactor / 100));
	}
		
	private double getDistanceMovedPerSecond() {
		return currentSpeed * 1000 / 3600;
	}
	
	private void determineNextSpeed(int raceLength,int averageSpeedAdjustmentFactor,double currentPosition) {
		if (currentPosition < (raceLength / 4)) {
			currentSpeed = currentSpeed  + (((getMaxSpeed(averageSpeedAdjustmentFactor) - currentSpeed) / 10) * random.nextDouble());
		}
		else {
			currentSpeed = currentSpeed * (0.5 + random.nextDouble());
		}
	
		if (currentSpeed > getMaxSpeed(averageSpeedAdjustmentFactor)) 
			currentSpeed = getMaxSpeed(averageSpeedAdjustmentFactor);
		
		if (currentSpeed < 5)
			currentSpeed = 5;
		
		if (currentPosition > (raceLength / 2) && currentSpeed < getMaxSpeed(averageSpeedAdjustmentFactor) / 2) {
			currentSpeed = getMaxSpeed(averageSpeedAdjustmentFactor) / 2;
		}
	}
		

	@Override
	public Receive<Command> createReceive() {
		// TODO Auto-generated method stub
		return notYetRunning();
	}
	
	
	public Receive<Command> notYetRunning() {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				.onMessage(StartCommand.class, message->{
					 int raceLength;
					raceLength=message.getRaceLength();
					random = new Random();
					int averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
					double currentPosition=0;
					
					return running(raceLength, averageSpeedAdjustmentFactor,currentPosition);
				})
				.onMessage(PositionCommand.class, message->{
	
					message.getController().tell(new RacerControllerBehavior1.RacerUpdateCommand(getContext().getSelf(), 0));
					
					return this;
				})
				.build();
	}
	
	
	public Receive<Command> running(int raceLength,int averageSpeedAdjustmentFactor,double currentPosition) {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				.onMessage(PositionCommand.class, message->{
					
						determineNextSpeed(raceLength,averageSpeedAdjustmentFactor,currentPosition);
						double newPosition= currentPosition;
						newPosition += getDistanceMovedPerSecond();
						if (newPosition >= raceLength ) {
							newPosition  = raceLength;
						 return completed(raceLength);
						}
						
						message.getController().tell(new RacerControllerBehavior1.RacerUpdateCommand(getContext().getSelf(), (int)newPosition));
					
					return running(raceLength, averageSpeedAdjustmentFactor,newPosition);
				})
				.build();
	}
	
	public Receive<Command> completed(int raceLength){
		
		return newReceiveBuilder()
				.onMessage(PositionCommand.class, message->{
					//System.out.println(getContext().getSelf().path()+" finished");
					
					message.getController().tell(new RacerControllerBehavior1.RacerUpdateCommand(getContext().getSelf(), raceLength));
					message.getController().tell(new RacerControllerBehavior1.RacerFinishedCommand(getContext().getSelf()));
					
				return waitingToStop();
			})
				.build();
	}
	
	public Receive<RacerBehavior1.Command> waitingToStop(){
		
		return newReceiveBuilder()
				.onAnyMessage(message->{
					
					return Behaviors.same();
				})
				.onSignal(PostStop.class, signal->{
					System.out.println("I am about to terminate");
					return Behaviors.same();
				})
				.build();
	}
	
}