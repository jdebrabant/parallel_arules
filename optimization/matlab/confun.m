function [c, ceq] = confun(x)
% Speficy non linear constraints for the optimization of samplesizesum

%x(1) is the number of samples (T)
%x(2) is the local onfidence parameter (phi)
% delta is the global confidence parameter
delta = 0.02;
% epsilon is the accuracy parameter
epsilon = 0.02;
% d is the VC-dimension parameter
d = 20;
% M (maximum sample size (i.e., maximum value for T*m)
M = 10000000;
% w is maximum local sample size
w = 110000;
% Nonlinear inequality constraints
c = [floor(x(1)/2)+1-x(1)*(1-x(2));
     sqrt(x(1)*(1-x(2))*2*log(1/delta))-x(1)*(1-x(2));
     x(1)*ceil((2/(epsilon^2)) * (d + log(1/x(2))))-M];
% Nonlinear equality constraints
ceq = [];