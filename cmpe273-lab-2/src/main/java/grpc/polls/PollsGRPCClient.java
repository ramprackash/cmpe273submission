/*
package grpc.polls;

import io.grpc.ChannelImpl;
import io.grpc.transport.netty.NegotiationType;
import io.grpc.transport.netty.NettyChannelBuilder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.sjsu.cmpe273.lab2.PollServiceGrpc;
import edu.sjsu.cmpe273.lab2.PollRequest;
import edu.sjsu.cmpe273.lab2.PollResponse;

public class PollsGRPCClient {
  private static final Logger logger = Logger.getLogger(PollsGRPCClient.class.getName());

  private final ChannelImpl channel;
  private final PollServiceGrpc.PollServiceBlockingStub blockingStub;

  public PollsGRPCClient(String host, int port) {
    channel =
        NettyChannelBuilder.forAddress(host, port).negotiationType(NegotiationType.PLAINTEXT)
            .build();
    blockingStub = PollServiceGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTerminated(5, TimeUnit.SECONDS);
  }

  public void greet(String question) {
    try {
      logger.info("Will create poll for : " + question + " ...");
      PollRequest request = 
              PollRequest.newBuilder()
                  .setModerator(1)
                  .setQuestion(question)
                  .setStartedAt("2015-02-23T13:00:00.000Z")
                  .setExpiredAt("2015-02-24T13:00:00.000Z")
                  .addChoice("iPhone")
                  .addChoice("android")
                  .build();
      PollResponse response = blockingStub.createPoll(request);
      logger.info("Created Poll with ID : " + response.getId());
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, "RPC failed", e);
      return;
    }
  }

  public static void main(String[] args) throws Exception {
    PollsGRPCClient client = new PollsGRPCClient("localhost", 50051);
    try {
      String user = "Android or iPhone";
      if (args.length > 0) {
        user = args[0]; 
      }
      client.greet(user);
    } finally {
      client.shutdown();
    }
  }
}
*/

/* Client from Prof */


package grpc.polls;

import io.grpc.ChannelImpl;
import io.grpc.transport.netty.NegotiationType;
import io.grpc.transport.netty.NettyChannelBuilder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.sjsu.cmpe273.lab2.PollServiceGrpc;
import edu.sjsu.cmpe273.lab2.PollRequest;
import edu.sjsu.cmpe273.lab2.PollResponse;

public class PollsGRPCClient {
    private static final Logger logger = Logger.getLogger(PollsGRPCClient.class.getName());

    private final ChannelImpl channel;
    private final PollServiceGrpc.PollServiceBlockingStub blockingStub;

    public PollsGRPCClient(String host, int port) {
        channel =
                NettyChannelBuilder.forAddress(host, port).negotiationType(NegotiationType.PLAINTEXT)
                        .build();
        blockingStub = PollServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTerminated(5, TimeUnit.SECONDS);
    }

    public void createPoll(String moderatorId , String question, String startedAt, String expiredAt, String[] choice) {
        if (choice == null || choice.length < 2) {
            new RuntimeException("choice must have two items in it");
        }
        try {
            logger.info("Creating a new poll for moderator " + moderatorId);
            PollRequest request = PollRequest.newBuilder()
                    .setModeratorId(moderatorId)
                    .setQuestion(question)
                    .setStartedAt(startedAt)
                    .setExpiredAt(expiredAt)
                    .addChoice(choice[0])
                    .addChoice(choice[1])
                    .build();

            PollResponse response = blockingStub.createPoll(request);
            logger.info("Created a new poll with id = " + response.getId());
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "RPC failed", e);
            return;
        }
    }

    public static void main(String[] args) throws Exception {
        PollsGRPCClient client = new PollsGRPCClient("localhost", 50051);
        try {
            /* Access a service running on the local machine on port 50051 */
            String moderatorId = "1";
            String question = "What type of smartphone do you have?";
            String startedAt = "2015-03-18T13:00:00.000Z";
            String expiredAt = "2015-03-19T13:00:00.000Z";
            String[] choice = new String[] { "Android", "iPhone" };

            client.createPoll(moderatorId, question, startedAt, expiredAt, choice);

        } finally {
            client.shutdown();
        }
    }
}
