import java.util.*;
import java.io.*;
import java.math.*;

/**
* Auto-generated code below aims at helping you parse
* the standard input according to the problem statement.
* The attraction contains a limited number L of places.
* The attraction can only function C number of times per day.
* The queue contains a number N of groups.
* Each group contains a number Pi of people.
*
* L : 1.0E7
* C : 9000000.0
* N : 1000
* PI : 71670
**/
class Solution {



  public static void main(String args[]) {
    Scanner in = new Scanner(System.in);
    double L = in.nextInt();
    System.err.println("L : " + L );
    double C = in.nextInt();
    System.err.println("C: " + C );
    int N = in.nextInt();
    System.err.println("N : " + N );

    int cursor = 0;
    int[] queue = new int[N];
    int[] nexts = new int[N];

    for (int i = 0; i < N; i++) {
      int pi = in.nextInt();
      //System.err.println("Pi : " + pi );
      queue[i]  = pi;
    }

    // Compute nexts
    for ( int i = 0; i < N ; i ++ ) {
      int people = 0;
      int index = i;
      int next = i;
      int count = 0;
      while( people + queue[ index ] <= L // limit of people in roller
      && count < N ) { // limit of existing people

        next = index;
        people += queue[ index ];
        //  System.err.println("PEOPLE " + index + " : " + people );
        // increment
        if( index != N-1 )
        index ++;
        else
        index = 0;

        count ++;
      }

      nexts[ i ] = next;

    }

    System.err.println("Done " );
    // Loop
    int round = 0;
    double money = 0;
    boolean startcapture = false;
    boolean endcapture = false;
    int moneycapture = 0;
    int roundcapture = 0;


    while( round < C ){

      int oldcursor = cursor;
      cursor = nexts[ cursor ];

      if( ! endcapture && startcapture  && cursor == 1 ){
        endcapture = true;
        System.err.println("Optimization, loop detected !! " );
        System.err.println("roundcapture : " + roundcapture );
        System.err.println("moneycapture : " + moneycapture );
        // optimization
        int earningrounds = 0;
        while( round + roundcapture < C ){
          round += roundcapture;
          earningrounds += roundcapture;
          money += moneycapture;
        }
        System.err.println( "Earning : " + earningrounds + " Rounds");
      }

      if( ! endcapture && ! startcapture  && cursor == 1 ){
        System.err.println("Start Capture :) !! " );
        startcapture = true;
        endcapture = false;
      }

      double people = 0;
      if( oldcursor == cursor ){
        // One Group !
        people += queue[ oldcursor ];
      }
      else if( cursor > oldcursor ){
        for( int i = oldcursor; i <= cursor; i ++ )
        people += queue[ i ];
      } else{
        for( int i = oldcursor; i < N; i ++ )
          people += queue[ i ];
        for( int i = 0; i <= cursor; i ++ )
          people += queue[ i ];
      }

      // increment
      if( cursor != N-1 )
      cursor ++;
      else
      cursor = 0;

      if( startcapture && ! endcapture ){
        moneycapture += people;
        roundcapture ++;
      }

      money += people;
      round ++;
    }

    // Write an action using System.out.println()
    // To debug: System.err.println("Debug messages...");

    System.out.printf("%.0f\n", money);
  }
}
