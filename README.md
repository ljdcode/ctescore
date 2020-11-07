# ctescore
Java code of CTE-score (Implement based on Banjo)

Run:
java -jar acoecfd.jar F:\\CTE\\sim1.txt 5 200 50 10 


Parameters Explanation:
Parmeter1: F:\\CTE\\sim1.txt ; File path of fMRI data.
Parmeter2: 5 ;  Number of brain regions (Integers greater than 1).
Parmeter3: 200 ; Length of fMRI timeseries (Integers greater than 1).
Parmeter4: 50 ;  Number of subjects (Integers greater than 0).
Parmeter5: 10 ; Running times (Integers greater than 0).


*************************************************************
Default parameters of CTE:
Parmeter1: 1 ; k 
Parmeter2: Maximum;  normal
Parmeter3: 100 ;  omega
Parmeter4: 1  ;  lamda
Parmeter5: Length of fMRI timeseries/2 ; m,n

Default parameters of ACO algorithm:
Parmeter6: 1 ; alpha 
Parmeter7: 2;  beta
Parmeter8: 0.2 ;  rou
Parmeter9: 0.8  ;  theta
Parmeter10: 100 ; Number of ants
**********************************************************************

Note:
Data should seperate by comma (see Smith simulated dataset.zip).


