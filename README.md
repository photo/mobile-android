Test coverage
===========

     cd &lt;inside the app directory&gt;
     android update project --path .
     android update test-project -m &lt;full path to app directory (not just .)&gt; -p ../test/
     cd ../test/
     ant coverage
