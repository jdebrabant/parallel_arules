function Tm = samplesizesum(x)
% Computes the total sample size (in transactions)

%x(1) is the number of samples (T)
%x(2) is the confidence parameter (phi)

% epsilon is the accuracy parameter
epsilon = 0.02;
% d is the VC-dimension parameter.
d = 20;
Tm = x(1)* ceil((2/(epsilon^2)) * (d + log(1/x(2))));