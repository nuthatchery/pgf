PGF – The Pretty Good Formatter
===============================

This repository contains implementations and examples for PGF:


  pgf-experimental/  – experimental Rascal implementation, with some examples. The interesting stuff is in src/org/nuthatchery/pgf/rascal/engines (a spacer and two line breaking variants). The examples found in our submitted SLE2013 paper can be found by importing org::nuthatchery::pgf::rascal::tests::TestPaper and running testPaper1() and testPaper2()

  pgf-java-formatter/ – experimental Java formatter build using PGF. The main program is in /pgf-java-formatter/src/org/nuthatchery/pgf/javaformatter/JavaFormatter.java
  

There is also a somewhat different experimental implementation in Racket. It can be found from a separate repository [on GitHub](https://github.com/bldl/pgf-racket "PGF in Racket").


Dependencies:

In order to use the Java formatter, you need  Nuthatch, Nuthatch/Stratego, Nuthatch/JavaFront and Stratego/XT.

    cd $MYGITDIR

    git clone git@github.com:nuthatchery/pgf.git

    git clone git@github.com:nuthatchery/nuthatch.git
    git clone git@github.com:nuthatchery/nuthatch-stratego.git
    git clone git@github.com:nuthatchery/nuthatch-javafront.git

    
    # Also get the JAR file of Stratego/XT
    wget http://hydra.nixos.org/job/strategoxt-java/strc-java-trunk/build/latest/download-by-type/file/jar
    
    # Import them all in Eclipse and build. You may have to remove the "plugin dependencies" on Stratego,
    # and use the strategoxt.jar file instead.
    
    # Run stuff using this CLASSPATH:
    cd $MYGITDIR/pgf/pgf-java-formatter/bin
    java -cp $MYGITDIR/strategoxt.jar:$MYGITDIR/nuthatch-javafront/bin:$MYGITDIR/pgf/pgf-experimental/bin:$MYGITDIR/nuthatch-stratego/bin:$MYGITDIR/nuthatch/bin:. org.nuthatchery.pgf.javaformatter.JavaFormatter
    
    
