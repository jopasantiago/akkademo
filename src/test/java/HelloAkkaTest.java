import akka.actor.*;
import akka.testkit.javadsl.TestKit;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class HelloAkkaTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    // #test_snippet
    @Test
    public void testSetGreeter() {
        new TestKit(system) {{
            final TestActorRef<HelloAkkaJava.Greeter> greeter =
                TestActorRef.create(system, Props.create(HelloAkkaJava.Greeter.class), "greeter1");

            greeter.tell(new HelloAkkaJava.WhoToGreet("testkit"), getTestActor());

            Assert.assertEquals("hello, testkit", greeter.underlyingActor().greeting);
        }};
    }

    @Test
    public void testGetGreeter() {
        new TestKit(system) {{

            final ActorRef greeter = system.actorOf(Props.create(HelloAkkaJava.Greeter.class), "greeter2");

            greeter.tell(new HelloAkkaJava.WhoToGreet("testkit"), getTestActor());
            greeter.tell(new HelloAkkaJava.Greet(), getTestActor());

            final HelloAkkaJava.Greeting greeting = expectMsgClass(HelloAkkaJava.Greeting.class);
            Assert.assertEquals("hello, testkit", greeting.message);

        }};
    }
    // end #test_snippet
}
