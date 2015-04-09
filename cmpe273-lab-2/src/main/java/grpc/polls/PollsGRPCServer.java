package grpc.polls;

import io.grpc.ServerImpl;
import io.grpc.stub.StreamObserver;
import io.grpc.transport.netty.NettyServerBuilder;

import java.util.logging.Logger;
import java.util.*;
import edu.sjsu.cmpe273.lab2.PollServiceGrpc;
import edu.sjsu.cmpe273.lab2.PollRequest;
import edu.sjsu.cmpe273.lab2.PollResponse;

import models.Poll;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class PollsGRPCServer {
  private static final Logger logger = Logger.getLogger(PollsGRPCServer.class.getName());

  /* The port on which the server should run */
  private int port = 50051;
  private ServerImpl server;

  public void start() throws Exception {
    server = NettyServerBuilder.forPort(port)
        .addService(PollServiceGrpc.bindService(new GreeterImpl()))
        .build().start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        PollsGRPCServer.this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws Exception {
    final PollsGRPCServer server = new PollsGRPCServer();
    server.start();
  }

  private class GreeterImpl implements PollServiceGrpc.PollService {

    @Override
    public void createPoll(PollRequest req, StreamObserver<PollResponse> responseObserver) {
            ArrayList<String> nc = new ArrayList<String>();
            int i = 0;
            logger.info("Question: " + req.getQuestion());
            while (i < req.getChoiceCount()) {
                    nc.add(req.getChoice(i++));
                    logger.info("Choice "+i+ " " + req.getChoice(i-1));
            }
            logger.info("Created Poll for moderator: " + req.getModeratorId());
            Poll poll = new Poll(req.getQuestion(), req.getStartedAt(), req.getExpiredAt(),nc);
            poll.mapToModeratorId(Integer.parseInt(req.getModeratorId()));

      PollResponse reply = PollResponse.newBuilder().setId(poll.getId()).build();
      responseObserver.onValue(reply);
      responseObserver.onCompleted();
    }
  }
}

