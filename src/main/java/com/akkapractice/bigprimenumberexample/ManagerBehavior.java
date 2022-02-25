package com.akkapractice.bigprimenumberexample;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command>{
	long start ;
	long end ;
	SortedSet<BigInteger> set= new TreeSet<>();
	public interface Command extends Serializable{
		
	}
	
	public static class InstructionCommand implements Command{
		public static final long serialVersionUID = 1L;
		
		String message;

		public InstructionCommand(String message) {
			super();
			this.message = message;
		}

		public static long getSerialversionuid() {
			return serialVersionUID;
		}

		public String getMessage() {
			return message;
		}
		
		
		
		
	}
	
	public static class ResultCommand implements Command{
		
		public static final long serialVersionUID=1L;
		
		BigInteger result;
		
		public ResultCommand(BigInteger result) {
			this.result=result;
		}
		
		
		public BigInteger getResult() {
			
			return result;
			
		} 
		
	}
	
	private ManagerBehavior(ActorContext<ManagerBehavior.Command> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public static Behavior<ManagerBehavior.Command> create(){
		
		return Behaviors.setup(ManagerBehavior::new);
	}

	@Override
	public Receive<ManagerBehavior.Command> createReceive() {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				.onMessage(InstructionCommand.class, command->{
					
					start= System.currentTimeMillis();
					if(command.getMessage().equals("start")) {
					for(int i=0;i<100;i++) {
						
						ActorRef<WorkerBehavior.Command> actor=getContext()
						.spawn(WorkerBehavior.create(), "worker::"+i);
						
						actor.tell(new WorkerBehavior.Command("start", getContext().getSelf()));
						
					}}
				return this;
				})
				.onMessage(ResultCommand.class, command->{
					
					set.add(command.getResult());
					System.out.println("I have Received "+set.size()+" Primes..");

					if(set.size()==100) {
						end = System.currentTimeMillis();
						System.out.println("Time taken :: "+(end-start)+" ms.");
						set.forEach(System.out::println);
					}
					
					return this;
				})
				.build();
	}
	
	

}
