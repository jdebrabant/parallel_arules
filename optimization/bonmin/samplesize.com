# AMPL commands
solve;
display T;
display phi;
display samplesize;
# print single sample size
display samplesize / T;
# print the sample size we would need to achieve the same accuracy and
# confidence using a single sample
display (2/(epsilon^2))*(d+log(1/delta));
