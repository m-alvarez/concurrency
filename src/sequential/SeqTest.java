package sequential;/* Generates the sequential.SetList
   Generates n random numbers between 2 and m. Default n=100, m=1000.
   For each generated number, inserts its multiples <= m.
   Then it removes its proper multiples <=m and
   the number itself if it is not prime.
   Prints the final set which should contain exactly the prime numbers less 
   or equal to m that were generated initially.  */

import java.util.Random;

public class SeqTest{

    private static int n=100;           // number of tests
    private static int m=1000;          // interval for numbers [2,m]
    private static int[] t =new int[n];
    private static SetList setlist = new SetList();
    private static Random g = new Random();

    public static boolean isprime(int a){     
	for (int i=2;i<=a/2;i++){if (a%i==0){return false;}}
        return true;}

    /*if you want an efficient primality test look at BigIntegers, 
      but this is useless for m=1000! */

    public static void test(int a){      
    int h=1;                                  // add multiples <= m
    while (h*a<=m){setlist.add(h*a); h++;}      
    int k=2;                                  // remove proper multiples <=m
    while (k*a<=m){if (setlist.member(k*a)){setlist.remove(k*a);}; k++;} //NB we only remove non-primes
    if (! isprime(a)){setlist.remove(a);}}    // remove a if not prime

 //Utility method to print the expected result. Uses the setlist to have it in growing order.

    public static void expected(){  
	for (int i=0;i<n;i++){if (isprime(t[i])){setlist.add(t[i]);};}
        System.out.println("The expected result is");
        setlist.print();
	System.out.println("The computed result is");
        for (int i=0;i<n;i++){setlist.remove(t[i]);}   // empty the list
    }

    public static void main(String[] args){
    for (int i =0;i<n;i++){t[i]=2+g.nextInt(m-1);}  // generates n random numbers in [2,m]]
    expected();                                     // prints the expected result
    for (int i=0;i<n;i++){test(t[i]);}              // runs a test for every generated number (order irrelevant)
    setlist.print();                                // prints the computed result
    System.out.println("End");
    }          
}
