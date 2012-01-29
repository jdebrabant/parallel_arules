# AMPL model for the computation of the optimal number of samples T and single
# confidence parameter \phi.
# The optimization problem is a MINLP (Mixed Integer Non Linear Program).
# To be solved using the BONMIN solver.
# BONMIN can be used through the NEOS server web interface at
# http://neos.mcs.anl.gov/neos/solvers/minco:Bonmin/AMPL.html

param delta > 0 < 0.5;			# global confidence parameter
param epsilon > 0 < 1;			# accuracy parameter
param d >= 1 integer;			# VC-dimension parameter
param w >= 2*(d+log(2))/(epsilon^2) integer;# maximum size of a sample.
#the constraint on w comes from the fact that phi must be less than 0.5.
param M >= w integer;			# maximum global sample size

var T integer >= 2;			# number of samples

var phi >= max(delta, exp(-(w*(epsilon^2))/2 +d)) <= 0.5 - 1e-6; # single sample confidence parameter
# phi must be >= delta, and such that the sample size is less than w. phi must also be < 0.5.

minimize samplesize:
	T * ceil((2 / (epsilon^2)) * (d + log(1/phi)));

subject to
	#Respect the maximum specified sample size M
	maxSamSize: T * ceil((2 / (epsilon^2)) * (d + log(1/phi))) <= M;
	# The number of "votes" R for a locally frequent itemset to be deemed
	# globally frequent must be less than the expectation of the number of
	# positive votes (T*(1-phi)).
	Rconstr: max(floor(T/2)+1, T*(1-phi)-sqrt(T*(1-phi)*2*log(1/delta))) - T*(1-phi) <= - 1e-6;

