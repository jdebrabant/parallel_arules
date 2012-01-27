% Support code for computing optimum number of samples (T) and confidence parameter (phi).

% delta is the global confidence parameter
delta = 0.02;
% epsilon is the accuracy parameter
epsilon = 0.02;
% d is the VC-dimension parameter
d = 20;
% w is maximum local sample size
w = 110000;
% lower and upper bounds to the variables
minphi = max(exp(-(w*(epsilon^2))/2 +d), delta);
lb = [1, minphi];
ub = [inf, 0.5];
%starting point (must (?) be feasible)
x0 = [10,0.04];
%Optimization options. Algorithm can be 'active-set', 'interior-point' or 'sqp'.
options = optimset('Algorithm','active-set', 'Display', 'iter');
% Run optimization. 
fmincon(@samplesizesum,x0,[],[],[],[],lb,ub,@confun,options)
