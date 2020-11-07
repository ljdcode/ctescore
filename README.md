# CTE-score
Java code of CTE-score (Implement based on Banjo)<br>

Run:<br>
java -jar CTE.jar F:\\CTE\\sim1.txt 5 200 50 10 <br>


Parameters Explanation:<br>
Parmeter1: F:\\CTE\\sim1.txt ; File path of fMRI data.<br>
Parmeter2: 5 ;  Number of brain regions (Integers greater than 1).<br>
Parmeter3: 200 ; Length of fMRI timeseries (Integers greater than 1).<br>
Parmeter4: 50 ;  Number of subjects (Integers greater than 0).<br>
Parmeter5: 10 ; Running times (Integers greater than 0).<br>

*************************************************************<br>
Default parameters of CTE:<br>
Parmeter1: 1 ; k <br>
Parmeter2: Maximum;  normal<br>
Parmeter3: 100 ;  omega<br>
Parmeter4: 1  ;  lamda<br>
Parmeter5: Length of fMRI timeseries/2 ; m,n<br>

Default parameters of ACO algorithm:<br>
Parmeter6: 1 ; alpha <br>
Parmeter7: 2;  beta<br>
Parmeter8: 0.2 ;  rou<br>
Parmeter9: 0.8  ;  theta<br>
Parmeter10: 100 ; Number of ants<br>
**********************************************************************<br>

Note:<br>
More details refer to Manuscript.
Data should seperate by comma (see Smith simulated dataset.zip).<br>


