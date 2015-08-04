package kembe;

import fj.data.Stream;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceTest {

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>(3000000);

        for (int i = 0; i < 3000000; i++) {
            list.add( "aaaaaabbbbfbfdsnjfdksgfndslhmclushcguilmrjkxndsgcjdsh jkdhkbfjkdlsnchjflsnjks bnklxbvnklv bdfkls vbdfjklsv bdfkslv bf" +
                    "nkvdflsvb jdfksl bvdfils vdfjkslv fdlsbv dfils vbdfjkl vbdfjsl vbdfisl vdfjklsvdfulsvh ufdlsi vnuilw97+ji0p895pwh594pwu5" + i );
        }

        final AtomicLong counter = new AtomicLong( 0 );



        long start = System.currentTimeMillis();

        for(String s : Stream.iterableStream( list)){
            counter.incrementAndGet();
            if(counter.get()%1000000==0)
                System.out.println( s );
        }

        long end = System.currentTimeMillis();

        System.out.println(end-start+" millis");
    }
}
